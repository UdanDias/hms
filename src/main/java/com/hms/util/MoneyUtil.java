package com.hms.util;

import com.hms.exception.BusinessRuleException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtil {
  private MoneyUtil() {}

  public static BigDecimal money(BigDecimal value) {
    return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
  }

  public static BigDecimal nonnegative(BigDecimal value) {
    var amount = money(value);
    if (amount.signum() < 0) throw new BusinessRuleException("Amounts cannot be negative");
    return amount;
  }

  public static BigDecimal positive(BigDecimal value) {
    var amount = money(value);
    if (amount.signum() <= 0) throw new BusinessRuleException("Amount must be greater than zero");
    return amount;
  }
}
