package com.projectx.backend.explanation.application;

import org.springframework.stereotype.Component;

import com.projectx.backend.asset.domain.entity.InvAsset;
import com.projectx.backend.asset.domain.repository.InvAssetRepository;
import com.projectx.backend.global.exception.BusinessException;
import com.projectx.backend.global.exception.ErrorCode;
import com.projectx.backend.plan.domain.entity.InvHolding;
import com.projectx.backend.plan.domain.entity.Plan;
import com.projectx.backend.plan.domain.repository.InvHoldingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Plan(+InvHolding) → ai-service {@code POST /calculate} 요청 본문(Kan-9 §2, 8필드) 변환.
 * 필드명·구조는 ai-service {@code explainer/public_api.PlanInputs}와 같아야 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class CalculationRequestFactory {

	private final InvHoldingRepository invHoldingRepository;
	private final InvAssetRepository invAssetRepository;
	private final ObjectMapper objectMapper;

	ObjectNode build(Plan plan) {
		ObjectNode root = objectMapper.createObjectNode();

		ObjectNode goal = root.putObject("goal");
		goal.put("amount", plan.getGoalAmount());
		goal.put("horizon_months", plan.getHorizonMonths());

		ObjectNode funds = root.putObject("funds");
		funds.put("initial", plan.getFundsInitial());
		funds.put("monthly", plan.getFundsMonthly());

		ObjectNode alloc = root.putObject("alloc");
		ObjectNode allocInitial = alloc.putObject("initial");
		allocInitial.put("invest", plan.getAllocInitialInvestPct());
		allocInitial.put("safe", plan.getAllocInitialSafePct());
		allocInitial.put("other", plan.getAllocInitialOtherPct());
		ObjectNode allocMonthly = alloc.putObject("monthly");
		allocMonthly.put("invest", plan.getAllocMonthlyInvestPct());
		allocMonthly.put("safe", plan.getAllocMonthlySafePct());
		allocMonthly.put("other", plan.getAllocMonthlyOtherPct());

		ObjectNode portfolio = root.putObject("portfolio");
		ArrayNode assets = portfolio.putArray("assets");
		for (InvHolding holding : invHoldingRepository.findByIdPlanId(plan.getId())) {
			InvAsset asset = invAssetRepository.findById(holding.getId().getAssetId())
					.orElseThrow(() -> {
						log.error("inv_holding이 참조하는 자산을 찾을 수 없음: assetId={}", holding.getId().getAssetId());
						return new BusinessException(ErrorCode.CALCULATION_FAILED);
					});
			ObjectNode assetNode = assets.addObject();
			assetNode.put("code", asset.getCode());
			assetNode.put("weight", holding.getWeightPct());
		}

		ObjectNode rebalancing = root.putObject("rebalancing");
		rebalancing.put("focus", FocusPeriodCodes.toCode(plan.getFocusPeriod()));

		return root;
	}

}
