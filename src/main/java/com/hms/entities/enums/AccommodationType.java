package com.hotelmanagement.entity.enums;

public enum AccommodationType {
    ROOM_ONLY("Room Only"),
    BED_AND_BREAKFAST("B&B"),
    HALF_BOARD("Half Board"),
    FULL_BOARD("Full Board");
    private final String value;
    AccommodationType(String value) { this.value = value; }
    public String getValue() { return value; }
    public static AccommodationType fromValue(String value) {
        for (AccommodationType item : values()) {
            if (item.value.equals(value)) return item;
        }
        throw new IllegalArgumentException("Unknown AccommodationType: " + value);
    }
}
