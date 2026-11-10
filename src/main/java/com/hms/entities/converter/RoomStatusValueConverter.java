package com.hms.entities.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.hotelmanagement.entity.enums.RoomStatusValue;

@Converter(autoApply = true)
public class RoomStatusValueConverter implements AttributeConverter<RoomStatusValue, String> {
    @Override
    public String convertToDatabaseColumn(RoomStatusValue value) {
        return value == null ? null : value.getValue();
    }
    @Override
    public RoomStatusValue convertToEntityAttribute(String value) {
        return value == null ? null : RoomStatusValue.fromValue(value);
    }
}
