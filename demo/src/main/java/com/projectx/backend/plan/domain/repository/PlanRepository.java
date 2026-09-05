package com.projectx.backend.plan.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projectx.backend.plan.domain.entity.Plan;

public interface PlanRepository extends JpaRepository<Plan, Long> {

	Optional<Plan> findByPublicId(UUID publicId);

}
