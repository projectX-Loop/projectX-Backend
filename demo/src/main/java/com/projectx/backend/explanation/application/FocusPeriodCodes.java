package com.projectx.backend.explanation.application;

import com.projectx.backend.plan.domain.entity.FocusPeriod;

/** Plan.focusPeriod(enum) ↔ ai-service가 쓰는 M/Q/H 코드. FocusPeriodConverter(plan 패키지 소유)와 같은
 * 매핑이지만 그 클래스는 JPA 컬럼 변환 전용이라 여기서 별도로 둔다. */
final class FocusPeriodCodes {

	private FocusPeriodCodes() {
	}

	static String toCode(FocusPeriod focusPeriod) {
		return switch (focusPeriod) {
			case MONTHLY -> "M";
			case QUARTERLY -> "Q";
			case HALF_YEARLY -> "H";
		};
	}

}
