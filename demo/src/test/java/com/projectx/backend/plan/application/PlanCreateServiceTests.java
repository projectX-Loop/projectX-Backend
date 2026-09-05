package com.projectx.backend.plan.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.projectx.backend.global.exception.BusinessException;
import com.projectx.backend.global.exception.ErrorCode;
import com.projectx.backend.plan.api.PlanCreateCommand;
import com.projectx.backend.plan.api.PlanCreateRequest.Allocation;
import com.projectx.backend.plan.api.PlanCreateRequest.AllocationItem;
import com.projectx.backend.plan.api.PlanCreateRequest.Funds;
import com.projectx.backend.plan.api.PlanCreateRequest.Goal;
import com.projectx.backend.plan.api.PlanCreateRequest.Portfolio;
import com.projectx.backend.plan.api.PlanCreateRequest.Rebalancing;

class PlanCreateServiceTests {

	@Test
	void rejectsRequestWhenBothFundsAreZero() {
		PlanCreateService service = new PlanCreateService(null, null, null, null, null, null);
		PlanCreateCommand command = new PlanCreateCommand(new Goal(50_000_000L, 60), new Funds(0L, 0L),
				new Allocation(new AllocationItem(60, 30, 10), new AllocationItem(60, 30, 10)),
				new Portfolio(List.of()), new Rebalancing("M"));

		assertThatThrownBy(() -> service.create(command))
				.isInstanceOf(BusinessException.class)
				.satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode()).isEqualTo(ErrorCode.NO_FUNDS));
	}

	@Test
	void rejectsPortfolioWhenInvestmentFundsExistWithoutAssets() {
		PlanCreateService service = new PlanCreateService(null, null, null, null, null, null);
		PlanCreateCommand command = new PlanCreateCommand(new Goal(50_000_000L, 60), new Funds(10_000_000L, 500_000L),
				new Allocation(new AllocationItem(60, 30, 10), new AllocationItem(60, 30, 10)),
				new Portfolio(List.of()), new Rebalancing("M"));

		assertThatThrownBy(() -> service.create(command))
				.isInstanceOf(BusinessException.class)
				.satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
						.isEqualTo(ErrorCode.PORTFOLIO_REQUIRED));
	}

}
