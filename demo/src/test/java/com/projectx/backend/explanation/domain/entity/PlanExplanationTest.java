package com.projectx.backend.explanation.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PlanExplanationTest {

	@Test
	void explanationRowHasNoQuestion() {
		PlanExplanation row = PlanExplanation.explanation(1L, "OK", "{\"summary\":\"...\"}", "[]");

		assertThat(row.getKind()).isEqualTo(ExplanationKind.EXPLANATION);
		assertThat(row.getQuestion()).isNull();
		assertThat(row.isOk()).isTrue();
	}

	@Test
	void questionRowKeepsQuestionText() {
		PlanExplanation row = PlanExplanation.question(1L, "분기별로 하면 얼마나 모여?", "OK", "{\"text\":\"...\"}", "[]");

		assertThat(row.getKind()).isEqualTo(ExplanationKind.QUESTION);
		assertThat(row.getQuestion()).isEqualTo("분기별로 하면 얼마나 모여?");
	}

	@Test
	void blankQuestionIsRejected() {
		assertThatThrownBy(() -> PlanExplanation.question(1L, " ", "OK", "{}", "[]"))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void nonOkStatusIsNotOk() {
		PlanExplanation row = PlanExplanation.explanation(1L, "EXPLANATION_REJECTED", "\"message\"", "[]");

		assertThat(row.isOk()).isFalse();
	}

}
