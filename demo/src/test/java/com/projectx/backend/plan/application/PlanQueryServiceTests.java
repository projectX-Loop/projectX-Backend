package com.projectx.backend.plan.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectx.backend.asset.domain.entity.InvAsset;
import com.projectx.backend.asset.domain.repository.InvAssetRepository;
import com.projectx.backend.global.exception.BusinessException;
import com.projectx.backend.global.exception.ErrorCode;
import com.projectx.backend.plan.api.PlanResponse;
import com.projectx.backend.plan.domain.entity.FocusPeriod;
import com.projectx.backend.plan.domain.entity.InvHolding;
import com.projectx.backend.plan.domain.entity.InvHoldingId;
import com.projectx.backend.plan.domain.entity.Plan;
import com.projectx.backend.plan.domain.repository.InvHoldingRepository;
import com.projectx.backend.plan.domain.repository.PlanRepository;
import com.projectx.backend.plan.infrastructure.CalculatorClient;
import com.projectx.backend.snapshot.domain.entity.DataSnapshot;
import com.projectx.backend.snapshot.domain.repository.DataSnapshotRepository;

import static org.mockito.Mockito.mock;

class PlanQueryServiceTests {

	@Test
	void recreatesPlanWithStoredSnapshotAndHoldings() throws Exception {
		PlanRepository planRepository = mock(PlanRepository.class);
		InvHoldingRepository invHoldingRepository = mock(InvHoldingRepository.class);
		InvAssetRepository invAssetRepository = mock(InvAssetRepository.class);
		DataSnapshotRepository dataSnapshotRepository = mock(DataSnapshotRepository.class);
		CalculatorClient calculatorClient = mock(CalculatorClient.class);
		PlanQueryService service = new PlanQueryService(planRepository, invHoldingRepository, invAssetRepository,
				dataSnapshotRepository, calculatorClient);
		UUID publicId = UUID.randomUUID();
		Plan plan = plan(publicId);
		DataSnapshot snapshot = mock(DataSnapshot.class);
		given(snapshot.getDataHash()).willReturn("sha256:stored");
		given(planRepository.findByPublicId(publicId)).willReturn(Optional.of(plan));
		given(dataSnapshotRepository.findById(9L)).willReturn(Optional.of(snapshot));
		InvHolding usEquity = holding(2L, 40);
		InvHolding koreanEquity = holding(1L, 60);
		InvAsset usEquityAsset = asset(2L, "US_EQ");
		InvAsset koreanEquityAsset = asset(1L, "KR_EQ");
		given(invHoldingRepository.findByIdPlanId(7L)).willReturn(List.of(usEquity, koreanEquity));
		given(invAssetRepository.findAllById(any())).willReturn(List.of(usEquityAsset, koreanEquityAsset));
		given(calculatorClient.calculate(any())).willReturn(
				new ObjectMapper().readTree("{\"status\":\"OK\",\"meta\":{\"data_hash\":\"sha256:stored\"}}"));

		PlanResponse response = service.get(publicId);

		assertThat(response.plan().publicId()).isEqualTo(publicId);
		assertThat(response.plan().dataSnapshotId()).isEqualTo(9L);
		assertThat(response.plan().inputs().portfolio().assets()).extracting(asset -> asset.code())
				.containsExactly("KR_EQ", "US_EQ");
		assertThat(response.plan().inputs().portfolio().assets()).extracting(asset -> asset.weight())
				.containsExactly(60, 40);
		assertThat(response.plan().inputs().rebalancing().focus()).isEqualTo("Q");
		assertThat(response.calculation()).contains("sha256:stored");
	}

	@Test
	void rejectsUnknownPlan() {
		PlanRepository planRepository = mock(PlanRepository.class);
		PlanQueryService service = new PlanQueryService(planRepository, mock(InvHoldingRepository.class),
				mock(InvAssetRepository.class), mock(DataSnapshotRepository.class), mock(CalculatorClient.class));
		UUID publicId = UUID.randomUUID();
		given(planRepository.findByPublicId(publicId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> service.get(publicId))
				.isInstanceOf(BusinessException.class)
				.satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
						.isEqualTo(ErrorCode.PLAN_NOT_FOUND));
	}

	@Test
	void returnsRetryableCalculationFailureWithPublicId() throws Exception {
		PlanRepository planRepository = mock(PlanRepository.class);
		InvHoldingRepository invHoldingRepository = mock(InvHoldingRepository.class);
		InvAssetRepository invAssetRepository = mock(InvAssetRepository.class);
		DataSnapshotRepository dataSnapshotRepository = mock(DataSnapshotRepository.class);
		CalculatorClient calculatorClient = mock(CalculatorClient.class);
		PlanQueryService service = new PlanQueryService(planRepository, invHoldingRepository, invAssetRepository,
				dataSnapshotRepository, calculatorClient);
		UUID publicId = UUID.randomUUID();
		Plan plan = plan(publicId);
		DataSnapshot snapshot = mock(DataSnapshot.class);
		given(snapshot.getDataHash()).willReturn("sha256:stored");
		given(planRepository.findByPublicId(publicId)).willReturn(Optional.of(plan));
		given(dataSnapshotRepository.findById(9L)).willReturn(Optional.of(snapshot));
		given(invHoldingRepository.findByIdPlanId(7L)).willReturn(List.of());
		given(invAssetRepository.findAllById(any())).willReturn(List.of());
		given(calculatorClient.calculate(any())).willReturn(new ObjectMapper().readTree("{\"meta\":{\"data_hash\":\"wrong\"}}"));

		assertThatThrownBy(() -> service.get(publicId))
				.isInstanceOf(BusinessException.class)
				.satisfies(exception -> {
					BusinessException businessException = (BusinessException) exception;
					assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.CALCULATION_FAILED);
					assertThat(businessException.getPublicId()).isEqualTo(publicId);
				});
	}

	private Plan plan(UUID publicId) {
		Plan plan = mock(Plan.class);
		given(plan.getId()).willReturn(7L);
		given(plan.getPublicId()).willReturn(publicId);
		given(plan.getDataSnapshotId()).willReturn(9L);
		given(plan.getCreatedAt()).willReturn(OffsetDateTime.parse("2026-09-06T00:00:00Z"));
		given(plan.getGoalAmount()).willReturn(50_000_000L);
		given(plan.getHorizonMonths()).willReturn((short) 60);
		given(plan.getFundsInitial()).willReturn(10_000_000L);
		given(plan.getFundsMonthly()).willReturn(600_000L);
		given(plan.getAllocInitialInvestPct()).willReturn((short) 70);
		given(plan.getAllocInitialSafePct()).willReturn((short) 30);
		given(plan.getAllocInitialOtherPct()).willReturn((short) 0);
		given(plan.getAllocMonthlyInvestPct()).willReturn((short) 50);
		given(plan.getAllocMonthlySafePct()).willReturn((short) 40);
		given(plan.getAllocMonthlyOtherPct()).willReturn((short) 10);
		given(plan.getFocusPeriod()).willReturn(FocusPeriod.QUARTERLY);
		return plan;
	}

	private InvHolding holding(Long assetId, int weight) {
		InvHolding holding = mock(InvHolding.class);
		given(holding.getId()).willReturn(new InvHoldingId(7L, assetId));
		given(holding.getWeightPct()).willReturn((short) weight);
		return holding;
	}

	private InvAsset asset(Long id, String code) {
		InvAsset asset = mock(InvAsset.class);
		given(asset.getId()).willReturn(id);
		given(asset.getCode()).willReturn(code);
		return asset;
	}

}
