package com.projectx.backend.explanation.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projectx.backend.explanation.domain.entity.ExplanationKind;
import com.projectx.backend.explanation.domain.entity.PlanExplanation;

public interface PlanExplanationRepository extends JpaRepository<PlanExplanation, Long> {

	List<PlanExplanation> findByPlanIdOrderByCreatedAtAsc(Long planId);

	/** 질문 문맥 재구성용(plan-rag-design.md) — 성공한 질문·답변만 오래된 순으로. */
	List<PlanExplanation> findByPlanIdAndKindAndStatusOrderByCreatedAtAsc(Long planId, ExplanationKind kind,
			String status);

}
