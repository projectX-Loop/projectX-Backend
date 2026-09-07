package com.projectx.backend.explanation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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
import com.projectx.backend.plan.api.PlanResponse;
import com.projectx.backend.plan.application.PlanQueryService;
import com.projectx.backend.plan.domain.entity.FocusPeriod;
import com.projectx.backend.plan.domain.entity.Plan;
import com.projectx.backend.plan.domain.repository.PlanRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@ExtendWith(MockitoExtension.class)
class ExplanationServiceTest {

	@Mock
	private PlanRepository planRepository;

	@Mock
	private PlanExplanationRepository planExplanationRepository;

	@Mock
	private PlanQueryService planQueryService;

	@Mock
	private PlanResponse planResponse;

	@Mock
	private AiServiceClient aiServiceClient;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private ExplanationService service;
	private Plan plan;

	@BeforeEach
	void setUp() {
		service = new ExplanationService(planRepository, planExplanationRepository, planQueryService,
				aiServiceClient, objectMapper);

		plan = Plan.create(1L, 50_000_000L, (short) 60, 10_000_000L, 600_000L, (short) 70, (short) 30, (short) 0,
				(short) 50, (short) 40, (short) 10, FocusPeriod.QUARTERLY);
		ReflectionTestUtils.setField(plan, "id", 42L);
		lenient().when(planQueryService.get(plan.getPublicId())).thenReturn(planResponse);
		lenient().when(planResponse.calculation()).thenReturn("{\"status\":\"OK\"}");
	}

	@Test
	void explainReturnsOkAndPersistsExplanationRow() {
		when(planRepository.findByPublicId(plan.getPublicId())).thenReturn(Optional.of(plan));

		ObjectNode ragResult = objectMapper.createObjectNode();
		ragResult.put("status", "OK");
		ragResult.putObject("explanation").put("summary", "요약");
		ragResult.putArray("retrieved_refs").add("concept/rebalancing#0");
		when(aiServiceClient.explain(any())).thenReturn(ragResult);

		ExplanationResponse response = service.explain(plan.getPublicId());

		assertThat(response.status()).isEqualTo("OK");
		assertThat(response.explanation().get("summary").asString()).isEqualTo("요약");
		assertThat(response.message()).isNull();
		verify(planExplanationRepository).save(argThat(row -> row.getPlanId().equals(42L) && row.isOk()));
	}

	@Test
	void explainThrowsPlanNotFoundWhenPlanMissing() {
		UUID publicId = UUID.randomUUID();
		when(planRepository.findByPublicId(publicId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.explain(publicId)).isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.PLAN_NOT_FOUND));
	}

	@Test
	void explainPersistsMessageOnlyWhenRejected() {
		when(planRepository.findByPublicId(plan.getPublicId())).thenReturn(Optional.of(plan));
		ObjectNode ragResult = objectMapper.createObjectNode();
		ragResult.put("status", "EXPLANATION_REJECTED");
		ragResult.putNull("explanation");
		ragResult.put("message", "AI 설명을 생성하지 못했습니다. 분석 결과는 정상입니다.");
		when(aiServiceClient.explain(any())).thenReturn(ragResult);

		ExplanationResponse response = service.explain(plan.getPublicId());

		assertThat(response.status()).isEqualTo("EXPLANATION_REJECTED");
		assertThat(response.explanation()).isNull();
		assertThat(response.message()).contains("분석 결과는 정상입니다");
		verify(planExplanationRepository).save(argThat(row -> !row.isOk()));
	}

	@Test
	void askIgnoresClientHistoryAndUsesStoredHistoryAndPersistsQuestionRow() {
		when(planRepository.findByPublicId(plan.getPublicId())).thenReturn(Optional.of(plan));
		when(planExplanationRepository.findByPlanIdAndKindAndStatusOrderByCreatedAtAsc(42L, ExplanationKind.QUESTION,
				"OK")).thenReturn(List.of());

		ObjectNode askResult = objectMapper.createObjectNode();
		askResult.put("status", "OK");
		askResult.putObject("answer").put("text", "분기별로 하면 6,975만원입니다.");
		askResult.putArray("retrieved_refs");
		ArgumentCaptor<JsonNode> payloadCaptor = ArgumentCaptor.forClass(JsonNode.class);
		when(aiServiceClient.ask(payloadCaptor.capture())).thenReturn(askResult);

		// 클라이언트가 history를 보내도 무시된다 — 문맥은 서버 저장분(위에서 빈 리스트)에서만 온다.
		QuestionRequest request = new QuestionRequest("반기별로는?",
				List.of(new QuestionHistoryItem("무시돼야 함", "무시돼야 함")));

		QuestionResponse response = service.ask(plan.getPublicId(), request);

		assertThat(response.status()).isEqualTo("OK");
		assertThat(response.answer().get("text").asString()).contains("6,975만원");
		assertThat(payloadCaptor.getValue().get("history")).isEmpty();
		verify(planExplanationRepository)
				.save(argThat(row -> "반기별로는?".equals(row.getQuestion()) && row.getPlanId().equals(42L)));
	}

	@Test
	void askLoadsStoredHistoryAndCapsAtMostRecentFiveTurns() {
		when(planRepository.findByPublicId(plan.getPublicId())).thenReturn(Optional.of(plan));
		// plan_explanation에 성공한 질문 8개가 쌓여 있다고 가정(오래된 순) — 마지막 5개(question_3~7)만 넘어가야 한다.
		List<PlanExplanation> stored = new ArrayList<>();
		for (int i = 0; i < 8; i++) {
			stored.add(PlanExplanation.question(42L, "question_" + i, "OK", "{\"text\":\"answer_" + i + "\"}", "[]"));
		}
		when(planExplanationRepository.findByPlanIdAndKindAndStatusOrderByCreatedAtAsc(42L, ExplanationKind.QUESTION,
				"OK")).thenReturn(stored);

		ObjectNode askResult = objectMapper.createObjectNode();
		askResult.put("status", "OK");
		askResult.putObject("answer").put("text", "답변");
		askResult.putArray("retrieved_refs");
		ArgumentCaptor<JsonNode> payloadCaptor = ArgumentCaptor.forClass(JsonNode.class);
		when(aiServiceClient.ask(payloadCaptor.capture())).thenReturn(askResult);

		service.ask(plan.getPublicId(), new QuestionRequest("최신 질문", List.of()));

		JsonNode sentHistory = payloadCaptor.getValue().get("history");
		assertThat(sentHistory).hasSize(5);
		assertThat(sentHistory.get(0).get("question").asString()).isEqualTo("question_3");
		assertThat(sentHistory.get(0).get("answer").asString()).isEqualTo("answer_3");
		assertThat(sentHistory.get(4).get("question").asString()).isEqualTo("question_7");
	}

	@Test
	void explainNeverDeletesPlanExplanationRows() {
		// KAN-23 취지(감사 로그)대로 plan_explanation은 지우지 않는다 — history 5턴 캡은 AI 호출 쪽에만 적용
		// (9/6 정정: 한때 저장 쪽에도 5행 캡을 걸었다가 감사 목적과 안 맞아 철회함).
		when(planRepository.findByPublicId(plan.getPublicId())).thenReturn(Optional.of(plan));
		ObjectNode ragResult = objectMapper.createObjectNode();
		ragResult.put("status", "OK");
		ragResult.putObject("explanation").put("summary", "요약");
		ragResult.putArray("retrieved_refs");
		when(aiServiceClient.explain(any())).thenReturn(ragResult);

		service.explain(plan.getPublicId());

		verify(planExplanationRepository, never()).deleteAll(any());
		verify(planExplanationRepository, never()).findByPlanIdOrderByCreatedAtAsc(any());
	}

}
