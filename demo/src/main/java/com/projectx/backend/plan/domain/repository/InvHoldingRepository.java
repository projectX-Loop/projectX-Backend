package com.projectx.backend.plan.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projectx.backend.plan.domain.entity.InvHolding;
import com.projectx.backend.plan.domain.entity.InvHoldingId;

public interface InvHoldingRepository extends JpaRepository<InvHolding, InvHoldingId> {

	List<InvHolding> findByIdPlanId(Long planId);

}
