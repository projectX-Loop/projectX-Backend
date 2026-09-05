package com.projectx.backend.plan.application;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.projectx.backend.asset.domain.entity.InvAsset;
import com.projectx.backend.asset.domain.repository.InvAssetRepository;
import com.projectx.backend.global.api.ValidationError;
import com.projectx.backend.global.exception.BusinessException;
import com.projectx.backend.global.exception.ErrorCode;
import com.projectx.backend.plan.api.PlanCreateCommand;
import com.projectx.backend.plan.api.PlanCreateRequest.AssetWeight;
import com.projectx.backend.plan.api.PlanCreateResponse;
import com.projectx.backend.plan.domain.entity.FocusPeriod;
import com.projectx.backend.plan.domain.entity.InvHolding;
import com.projectx.backend.plan.domain.entity.Plan;
import com.projectx.backend.plan.domain.repository.InvHoldingRepository;
import com.projectx.backend.plan.domain.repository.PlanRepository;
import com.projectx.backend.plan.infrastructure.CalculatorClient;
import com.projectx.backend.snapshot.domain.entity.DataSnapshot;
import com.projectx.backend.snapshot.domain.entity.DataSnapshotAsset;
import com.projectx.backend.snapshot.domain.repository.DataSnapshotAssetRepository;
import com.projectx.backend.snapshot.domain.repository.DataSnapshotRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanCreateService {

	private static final long MIN_GOAL_AMOUNT = 1_000_000L;
	private static final long MAX_GOAL_AMOUNT = 10_000_000_000L;

	private final DataSnapshotRepository dataSnapshotRepository;
	private final DataSnapshotAssetRepository dataSnapshotAssetRepository;
	private final InvAssetRepository invAssetRepository;
	private final PlanRepository planRepository;
	private final InvHoldingRepository invHoldingRepository;
	private final CalculatorClient calculatorClient;

	@Transactional
	public PlanCreateResponse create(PlanCreateCommand command) {
		validate(command);
		DataSnapshot dataSnapshot = dataSnapshotRepository.findByIsCurrentTrue()
				.orElseThrow(() -> new BusinessException(ErrorCode.DATA_SNAPSHOT_UNAVAILABLE,
						"Current data snapshot is unavailable"));

		Map<String, InvAsset> allowedAssets = loadAllowedAssets(dataSnapshot);
		validatePortfolioAssets(command, allowedAssets);

		FocusPeriod focusPeriod = toFocusPeriod(command.rebalancing().focus());
		Plan plan = Plan.create(dataSnapshot.getId(), command.goal().amount(), command.goal().horizonMonths().shortValue(),
				command.funds().initial(), command.funds().monthly(), command.alloc().initial().invest().shortValue(),
				command.alloc().initial().safe().shortValue(), command.alloc().initial().other().shortValue(),
				command.alloc().monthly().invest().shortValue(), command.alloc().monthly().safe().shortValue(),
				command.alloc().monthly().other().shortValue(), focusPeriod);
		Plan savedPlan = planRepository.save(plan);

		if (hasInvestmentFunds(command)) {
			for (AssetWeight assetWeight : command.portfolio().assets()) {
				InvAsset asset = allowedAssets.get(assetWeight.code());
				invHoldingRepository.save(InvHolding.create(savedPlan, asset.getId(), assetWeight.weight().shortValue()));
			}
		}

		JsonNode calculation = calculatorClient.calculate(command);
		verifyDataHash(calculation, dataSnapshot.getDataHash());
		return new PlanCreateResponse(savedPlan.getPublicId(), calculation);
	}

	private void validate(PlanCreateCommand command) {
		validateGoal(command);
		validateFunds(command);
		validateAllocation(command);
		validateFocus(command);
		validatePortfolio(command);
	}

	private void validateGoal(PlanCreateCommand command) {
		if (command.goal().amount() < MIN_GOAL_AMOUNT || command.goal().amount() > MAX_GOAL_AMOUNT) {
			throw validationException(ErrorCode.GOAL_AMOUNT_RANGE, "goal.amount must be between 1000000 and 10000000000",
					"goal.amount");
		}
		if (command.goal().horizonMonths() < 12 || command.goal().horizonMonths() > 120) {
			throw validationException(ErrorCode.GOAL_HORIZON_RANGE, "goal.horizon_months must be between 12 and 120",
					"goal.horizon_months");
		}
	}

	private void validateFunds(PlanCreateCommand command) {
		if (command.funds().initial() < 0) {
			throw validationException(ErrorCode.FUNDS_INITIAL_RANGE, "funds.initial must be non-negative", "funds.initial");
		}
		if (command.funds().monthly() < 0) {
			throw validationException(ErrorCode.FUNDS_MONTHLY_RANGE, "funds.monthly must be non-negative", "funds.monthly");
		}
		if (command.funds().initial() == 0 && command.funds().monthly() == 0) {
			throw validationException(ErrorCode.NO_FUNDS, "funds.initial and funds.monthly cannot both be zero", "funds");
		}
	}

	private void validateAllocation(PlanCreateCommand command) {
		validateAllocationItem(command.alloc().initial(), ErrorCode.ALLOC_SUM_INITIAL, "alloc.initial");
		validateAllocationItem(command.alloc().monthly(), ErrorCode.ALLOC_SUM_MONTHLY, "alloc.monthly");
	}

	private void validateAllocationItem(com.projectx.backend.plan.api.PlanCreateRequest.AllocationItem allocation,
			ErrorCode errorCode, String field) {
		if (allocation.invest() < 0 || allocation.invest() > 100 || allocation.safe() < 0 || allocation.safe() > 100
				|| allocation.other() < 0 || allocation.other() > 100
				|| allocation.invest() + allocation.safe() + allocation.other() != 100) {
			throw validationException(errorCode, field + " values must be between 0 and 100 and sum to 100", field);
		}
	}

	private void validateFocus(PlanCreateCommand command) {
		toFocusPeriod(command.rebalancing().focus());
	}

	private void validatePortfolio(PlanCreateCommand command) {
		List<AssetWeight> assets = command.portfolio().assets();
		if (!hasInvestmentFunds(command)) {
			return;
		}
		if (assets.isEmpty() || assets.size() > 3) {
			throw validationException(ErrorCode.PORTFOLIO_REQUIRED, "portfolio.assets must contain between 1 and 3 assets",
					"portfolio.assets");
		}
		Set<String> codes = new HashSet<>();
		int totalWeight = 0;
		for (AssetWeight asset : assets) {
			if (asset.weight() <= 0 || asset.weight() > 100) {
				throw validationException(ErrorCode.PORTFOLIO_WEIGHT_RANGE,
						"portfolio asset weight must be between 1 and 100", "portfolio.assets");
			}
			if (!codes.add(asset.code())) {
				throw validationException(ErrorCode.PORTFOLIO_ASSET_DUP, "portfolio asset codes must be unique",
						"portfolio.assets");
			}
			totalWeight += asset.weight();
		}
		if (totalWeight != 100) {
			throw validationException(ErrorCode.WEIGHTS_SUM, "portfolio asset weights must sum to 100", "portfolio.assets");
		}
	}

	private Map<String, InvAsset> loadAllowedAssets(DataSnapshot dataSnapshot) {
		List<DataSnapshotAsset> snapshotAssets = dataSnapshotAssetRepository.findByIdDataSnapshotId(dataSnapshot.getId());
		Set<Long> assetIds = new HashSet<>();
		for (DataSnapshotAsset snapshotAsset : snapshotAssets) {
			assetIds.add(snapshotAsset.getId().getInvAssetId());
		}
		Map<String, InvAsset> assetsByCode = new HashMap<>();
		for (InvAsset asset : invAssetRepository.findAllById(assetIds)) {
			assetsByCode.put(asset.getCode(), asset);
		}
		return assetsByCode;
	}

	private void validatePortfolioAssets(PlanCreateCommand command, Map<String, InvAsset> allowedAssets) {
		if (!hasInvestmentFunds(command)) {
			return;
		}
		for (AssetWeight asset : command.portfolio().assets()) {
			if (!allowedAssets.containsKey(asset.code())) {
				throw validationException(ErrorCode.ASSET_NOT_IN_CATALOG, "Asset is not in the current catalog",
						"portfolio.assets");
			}
		}
	}

	private boolean hasInvestmentFunds(PlanCreateCommand command) {
		return command.funds().initial() > 0 && command.alloc().initial().invest() > 0
				|| command.funds().monthly() > 0 && command.alloc().monthly().invest() > 0;
	}

	private FocusPeriod toFocusPeriod(String focus) {
		return switch (focus) {
			case "M" -> FocusPeriod.MONTHLY;
			case "Q" -> FocusPeriod.QUARTERLY;
			case "H" -> FocusPeriod.HALF_YEARLY;
			default -> throw validationException(ErrorCode.FOCUS_INVALID, "rebalancing.focus must be M, Q, or H",
					"rebalancing.focus");
		};
	}

	private void verifyDataHash(JsonNode calculation, String dataHash) {
		String responseDataHash = calculation.path("meta").path("data_hash").asText();
		if (responseDataHash.isBlank()) {
			throw new BusinessException(ErrorCode.CALCULATOR_RESPONSE_INVALID,
					"Calculator response does not contain meta.data_hash");
		}
		if (!dataHash.equals(responseDataHash)) {
			throw new BusinessException(ErrorCode.CALCULATOR_DATA_MISMATCH,
					"Calculator response data hash does not match the plan snapshot");
		}
	}

	private BusinessException validationException(ErrorCode errorCode, String message, String field) {
		return new BusinessException(errorCode, message, List.of(new ValidationError(field, message)));
	}

}
