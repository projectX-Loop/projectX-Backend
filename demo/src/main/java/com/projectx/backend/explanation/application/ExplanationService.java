package com.projectx.backend.explanation.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.projectx.backend.aiservice.infrastructure.AiServiceClient;
import com.projectx.backend.explanation.api.ExplanationResponse;
import com.projectx.backend.explanation.api.QuestionHistoryItem;
import com.projectx.backend.explanation.api.QuestionRequest;
import com.projectx.backend.explanation.api.QuestionResponse;
import com.projectx.backend.explanation.domain.entity.ExplanationKind;
import com.projectx.backend.explanation.domain.entity.PlanExplanation;
import com.projectx.backend.explanation.domain.repository.PlanExplanationRepository;
import com.projectx.backend.global.exception.BusinessException;
import com.projectx.backend.global.exception.ErrorCode;
import com.projectx.backend.plan.domain.entity.Plan;
import com.projectx.backend.plan.domain.repository.PlanRepository;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * 계산과 설명은 분리해서 부른다(KAN-4 결정) — 계산 결과는 저장하지 않으므로 설명·질문 때마다
 * ai-service {@code /calculate}를 다시 불러 결과를 재구성한 뒤 {@code /rag/answer}·{@code /rag/ask}에 넘긴다.
 * 응답은 매번 plan_explanation 한 행으로 남긴다(감사 로그, KAN-23·24 통합).
 *
 * <p>질문 문맥(멀티턴)은 클라이언트가 보낸 {@code history}가 아니라 <b>서버에 저장된 plan_explanation에서
 * 직접 재구성</b>한다(9/6 도윤 {@code docs/plan-rag-design.md} 결정). 이 plan에서 성공(status=OK)한 질문만
 * 최근 5개를 오래된 순으로 가져와 ai-service에 넘긴다 — 프론트가 history를 정확히 들고 있어야 하는 부담이
 * 없어지고, 새로고침해도(프론트 localStorage가 날아가도) 서버가 기억하는 대화 맥락은 그대로 유지된다.
 *
 * <p>의도적으로 {@code @Transactional}을 메서드 전체에 걸지 않는다 — {@code /rag/answer}는 생성에 20~90초가
 * 걸리는데(ai-service 실측), 그 시간 내내 DB 트랜잭션·커넥션을 붙잡고 있으면 동시 요청 몇 건만으로 커넥션
 * 풀이 고갈된다. Plan 조회·PlanExplanation 저장은 각각 Spring Data JPA repository 메서드 자체가
 * 트랜잭션을 열고 닫으므로(save 1건, 짧은 조회 1건) 별도 트랜잭션 경계가 없어도 정합성엔 문제없다.
 */
@Service
@RequiredArgsConstructor
public class ExplanationService {

	/** ai-service README 「질문답변 스트레치」의 알려진 한계(매 호출 전체 이력 재전송 → 페이로드 제곱 증가)에
	 * 대한 개선 ① — 최근 N턴만 ai-service에 넘긴다. plan_explanation 저장 자체는 전부 남긴다(감사 로그,
	 * KAN-23 취지) — 이 상수는 조회·전송 범위만 제한한다. */
	private static final int MAX_HISTORY_TURNS = 5;

	private final PlanRepository planRepository;
	private final PlanExplanationRepository planExplanationRepository;
	private final CalculationRequestFactory calculationRequestFactory;
	private final AiServiceClient aiServiceClient;
	private final ObjectMapper objectMapper;

	public ExplanationResponse explain(UUID publicId) {
		Plan plan = findPlan(publicId);
		JsonNode result = aiServiceClient.explain(buildRagPayload(plan));

		String status = textOrNull(result, "status");
		JsonNode explanation = nodeOrNull(result, "explanation");
		String message = textOrNull(result, "message");

		planExplanationRepository.save(PlanExplanation.explanation(plan.getId(), status,
				toJson(explanation != null ? explanation : message), toJson(result.path("retrieved_refs"))));

		return new ExplanationResponse(status, explanation, message);
	}

	public QuestionResponse ask(UUID publicId, QuestionRequest request) {
		Plan plan = findPlan(publicId);

		ObjectNode payload = buildRagPayload(plan);
		payload.put("question", request.question());
		var history = payload.putArray("history");
		for (QuestionHistoryItem item : loadRecentHistory(plan.getId())) {
			ObjectNode node = history.addObject();
			node.put("question", item.question());
			node.put("answer", item.answer());
		}

		JsonNode result = aiServiceClient.ask(payload);

		String status = textOrNull(result, "status");
		JsonNode answer = nodeOrNull(result, "answer");
		String message = textOrNull(result, "message");

		planExplanationRepository.save(PlanExplanation.question(plan.getId(), request.question(), status,
				toJson(answer != null ? answer : message), toJson(result.path("retrieved_refs"))));

		return new QuestionResponse(status, answer, message);
	}

	/** plan에 저장된 성공(status=OK) 질문 중 최근 {@value #MAX_HISTORY_TURNS}개를 오래된 순으로 재구성한다.
	 * 답변 텍스트는 payload(성공 시 저장된 Claim JSON)의 {@code text} 필드에서 꺼낸다. */
	private List<QuestionHistoryItem> loadRecentHistory(Long planId) {
		List<PlanExplanation> rows = planExplanationRepository
				.findByPlanIdAndKindAndStatusOrderByCreatedAtAsc(planId, ExplanationKind.QUESTION, "OK");
		List<PlanExplanation> recent = rows.size() > MAX_HISTORY_TURNS
				? rows.subList(rows.size() - MAX_HISTORY_TURNS, rows.size())
				: rows;

		List<QuestionHistoryItem> history = new ArrayList<>();
		for (PlanExplanation row : recent) {
			String answerText = objectMapper.readTree(row.getPayload()).path("text").asString("");
			history.add(new QuestionHistoryItem(row.getQuestion(), answerText));
		}
		return history;
	}

	private Plan findPlan(UUID publicId) {
		return planRepository.findByPublicId(publicId).orElseThrow(() -> new BusinessException(ErrorCode.PLAN_NOT_FOUND));
	}

	/** 계산 결과(§5) + focus + goal_amount — /rag/answer, /rag/ask 공통 본문(KAN-17). */
	private ObjectNode buildRagPayload(Plan plan) {
		JsonNode calculation = aiServiceClient.calculate(calculationRequestFactory.build(plan));
		ObjectNode payload = (ObjectNode) calculation.deepCopy();
		payload.put("focus", FocusPeriodCodes.toCode(plan.getFocusPeriod()));
		payload.put("goal_amount", plan.getGoalAmount());
		return payload;
	}

	private static String textOrNull(JsonNode node, String field) {
		JsonNode value = node.get(field);
		return (value == null || value.isNull()) ? null : value.asString();
	}

	private static JsonNode nodeOrNull(JsonNode node, String field) {
		JsonNode value = node.get(field);
		return (value == null || value.isNull()) ? null : value;
	}

	private String toJson(Object value) {
		return objectMapper.writeValueAsString(value);
	}

}
