package com.hms.web;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.hms.exception.BusinessRuleException;

/** Website supplements are per person/night; legacy accommodation.price is NOT used. */
@Component
public class WebMealPricing {
  @Value("${app.web.meals.bb.adult:2500}") private BigDecimal bbAdult;
  @Value("${app.web.meals.bb.child:1250}") private BigDecimal bbChild;
  @Value("${app.web.meals.half-board.adult:5500}") private BigDecimal hbAdult;
  @Value("${app.web.meals.half-board.child:2750}") private BigDecimal hbChild;
  @Value("${app.web.meals.full-board.adult:8000}") private BigDecimal fbAdult;
  @Value("${app.web.meals.full-board.child:4000}") private BigDecimal fbChild;
  public BigDecimal rate(String plan, boolean child) {
    BigDecimal value = switch(plan) {
      case "room-only" -> BigDecimal.ZERO;
      case "bb" -> child ? bbChild : bbAdult;
      case "half-board" -> child ? hbChild : hbAdult;
      case "full-board" -> child ? fbChild : fbAdult;
      default -> throw new BusinessRuleException("Select a valid meal plan");
    };
    if(value.signum()<0 || value.scale()>2) throw new BusinessRuleException("Meal rates must be non-negative amounts with at most two decimal places");
    return value.setScale(2);
  }
  public String name(String plan) { return switch(plan) {
    case "room-only" -> "Room Only"; case "bb" -> "Bed & Breakfast";
    case "half-board" -> "Half Board"; case "full-board" -> "Full Board";
    default -> throw new BusinessRuleException("Select a valid meal plan");
  }; }
  public String description(String plan) { return switch(plan) {
    case "bb" -> "Breakfast each morning."; case "half-board" -> "Breakfast and dinner.";
    case "full-board" -> "Breakfast, lunch and dinner."; default -> "Meals are not included.";
  }; }
}
