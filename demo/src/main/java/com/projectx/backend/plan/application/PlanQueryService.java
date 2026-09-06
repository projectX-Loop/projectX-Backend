package com.projectx.backend.plan.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.projectx.backend.asset.domain.entity.InvAsset;
import com.projectx.backend.asset.domain.repository.InvAssetRepository;
import com.projectx.backend.global.exception.BusinessException;
import com.projectx.backend.global.exception.ErrorCode;
import com.projectx.backend.plan.api.PlanCreateCommand;
import com.projectx.backend.plan.api.PlanCreateRequest.Allocation;
import com.projectx.backend.plan.api.PlanCreateRequest.AllocationItem;
import com.projectx.backend.plan.api.PlanCreateRequest.AssetWeight;
import com.projectx.backend.plan.api.PlanCreateRequest.Funds;
import com.projectx.backend.plan.api.PlanCreateRequest.Goal;
import com.projectx.backend.plan.api.PlanCreateRequest.Portfolio;
import com.projectx.backend.plan.api.PlanCreateRequest.Rebalancing;
import com.projectx.backend.plan.api.PlanResponse;
import com.projectx.backend.plan.domain.entity.FocusPeriod;
import com.projectx.backend.plan.domain.entity.InvHolding;
import com.projectx.backend.plan.domain.entity.Plan;
import com.projectx.backend.plan.domain.repository.InvHoldingRepository;
import com.projectx.backend.plan.domain.repository.PlanRepository;
import com.projectx.backend.plan.infrastructure.CalculatorClient;
import com.projectx.backend.snapshot.domain.entity.DataSnapshot;
import com.projectx.backend.snapshot.domain.repository.DataSnapshotRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanQueryService {

	private final PlanRepository planRepository;
	private final InvHoldingRepository invHoldingRepository;
	private final InvAssetRepository invAssetRepository;
	private final DataSnapshotRepository dataSnapshotRepository;
	private final CalculatorClient calculatorClient;

	@Transactional(readOnly = true)
	public PlanResponse get(UUID publicId) {
		Plan plan = planRepository.findByPublicId(publicId)
				.orElseThrow(() -> new BusinessException(ErrorCode.PLAN_NOT_FOUND));
		try {
			DataSnapshot dataSnapshot = dataSnapshotRepository.findById(plan.getDataSnapshotId())
					.orElseThrow(() -> new BusinessException(ErrorCode.CALCULATION_FAILED, publicId));
			PlanCreateCommand command = toCommand(plan);
			JsonNode calculation = calculatorClient.calculate(command);
			verifyDataHash(calculation, dataSnapshot.getDataHash());
			return PlanResponse.from(plan, command, calculation);
		} catch (BusinessException exception) {
			if (exception.getErrorCode() == ErrorCode.CALCULATION_FAILED && exception.getPublicId() != null) {
				throw exception;
			}
			throw new BusinessException(ErrorCode.CALCULATION_FAILED, publicId);
		}
	}

	private PlanCreateCommand toCommand(Plan plan) {
		List<InvHolding> holdings = invHoldingRepository.findByIdPlanId(plan.getId());
		Map<Long, InvAsset> assetsById = invAssetRepository.findAllById(
				holdings.stream().map(holding -> holding.getId().getAssetId()).toList()).stream()
				.collect(java.util.stream.Collectors.toMap(InvAsset::getId, Function.identity()));
		List<AssetWeight> assets = holdings.stream()
				.map(holding -> new AssetWeight(assetsById.get(holding.getId().getAssetId()).getCode(),
						(int) holding.getWeightPct()))
				.sorted(java.util.Comparator.comparing(AssetWeight::code))
				.toList();
		return new PlanCreateCommand(new Goal(plan.getGoalAmount(), (int) plan.getHorizonMonths()),
				new Funds(plan.getFundsInitial(), plan.getFundsMonthly()),
				new Allocation(new AllocationItem((int) plan.getAllocInitialInvestPct(), (int) plan.getAllocInitialSafePct(),
						(int) plan.getAllocInitialOtherPct()),
						new AllocationItem((int) plan.getAllocMonthlyInvestPct(), (int) plan.getAllocMonthlySafePct(),
								(int) plan.getAllocMonthlyOtherPct())),
				new Portfolio(assets), new Rebalancing(toFocusCode(plan.getFocusPeriod())));
	}

	private String toFocusCode(FocusPeriod focusPeriod) {
		return switch (focusPeriod) {
			case MONTHLY -> "M";
			case QUARTERLY -> "Q";
			case HALF_YEARLY -> "H";
		};
	}

	private void verifyDataHash(JsonNode calculation, String dataHash) {
		if (!dataHash.equals(calculation.path("meta").path("data_hash").asText())) {
			throw new BusinessException(ErrorCode.CALCULATION_FAILED);
		}
	}

}
