package com.hms.entities.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.hotelmanagement.entity.enums.AccommodationType;

@Converter(autoApply = true)
public class AccommodationTypeConverter implements AttributeConverter<AccommodationType, String> {
    @Override
    public String convertToDatabaseColumn(AccommodationType value) {
        return value == null ? null : value.getValue();
    }
    @Override
    public AccommodationType convertToEntityAttribute(String value) {
        return value == null ? null : AccommodationType.fromValue(value);
    }
}
