package com.projectx.backend.plan.domain.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class FocusPeriodConverter implements AttributeConverter<FocusPeriod, String> {

	@Override
	public String convertToDatabaseColumn(FocusPeriod attribute) {
		if (attribute == null) {
			return null;
		}
		return switch (attribute) {
			case MONTHLY -> "M";
			case QUARTERLY -> "Q";
			case HALF_YEARLY -> "H";
		};
	}

	@Override
	public FocusPeriod convertToEntityAttribute(String dbData) {
		if (dbData == null) {
			return null;
		}
		return switch (dbData) {
			case "M" -> FocusPeriod.MONTHLY;
			case "Q" -> FocusPeriod.QUARTERLY;
			case "H" -> FocusPeriod.HALF_YEARLY;
			default -> throw new IllegalArgumentException("Unknown focus_period_enum value: " + dbData);
		};
	}

}
