package com.hotelmanagement.entity.enums;

public enum RoomStatusValue {
    AVAILABLE("available"),
    UNAVAILABLE("unavailable"),
    SERVICING("servicing");
    private final String value;
    RoomStatusValue(String value) { this.value = value; }
    public String getValue() { return value; }
    public static RoomStatusValue fromValue(String value) {
        for (RoomStatusValue item : values()) {
            if (item.value.equals(value)) return item;
        }
        throw new IllegalArgumentException("Unknown RoomStatusValue: " + value);
    }
}
