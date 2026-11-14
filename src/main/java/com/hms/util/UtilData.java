package com.hms.util;

import com.hms.entities.HotelEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.UUID;

public final class UtilData {
  private UtilData() {}

  public static LocalDateTime generateNow() {
    return LocalDateTime.now(ZoneOffset.UTC);
  }

  public static LocalDate todayAtHotel(HotelEntity hotel) {
    return LocalDate.now(ZoneId.of(hotel.getTimezone()));
  }

  public static String generateNumber(String prefix) {
    return prefix + "-" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
  }
}
