package com.hms.web;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.hms.exception.BusinessRuleException;

/** Website supplements are per person/night; legacy accommodation.price is NOT used. */
@Component
public class WebMealPricing {
  @org.springframework.beans.factory.annotation.Autowired private org.springframework.jdbc.core.JdbcTemplate jdbc;
  @org.springframework.beans.factory.annotation.Autowired private com.hms.security.ServiceSecurity security;
  @Value("${app.web.hotel-id}") private long hotelId;
  @org.springframework.transaction.annotation.Transactional
  public void save(java.util.List<WebRequests.Meal> values) {
    security.requireRoles("ADMIN");
    if(values==null || values.size()!=3 || values.stream().anyMatch(java.util.Objects::isNull) || !values.stream().map(WebRequests.Meal::id).collect(java.util.stream.Collectors.toSet()).equals(java.util.Set.of("bb","half-board","full-board")))
      throw new BusinessRuleException("Supply all three meal plans exactly once");
    for(var v:values) {
      for(var amount:java.util.Arrays.asList(v.adultSupplement(),v.childSupplement()))
        if(amount==null || amount.signum()<0 || amount.scale()>2 || amount.compareTo(new BigDecimal("9999999999.99"))>0)
          throw new BusinessRuleException("Enter non-negative meal prices with at most two decimal places");
    }
    jdbc.queryForObject("SELECT id FROM hotels WHERE id=? FOR UPDATE",Long.class,hotelId);
    for(var v:values) jdbc.update("INSERT INTO web_meal_prices (hotel_id,plan_key,adult_price,child_price) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE adult_price=VALUES(adult_price),child_price=VALUES(child_price)",hotelId,v.id(),v.adultSupplement(),v.childSupplement());
  }
  @Value("${app.web.meals.bb.adult:2500}") private BigDecimal bbAdult;
  @Value("${app.web.meals.bb.child:1250}") private BigDecimal bbChild;
  @Value("${app.web.meals.half-board.adult:5500}") private BigDecimal hbAdult;
  @Value("${app.web.meals.half-board.child:2750}") private BigDecimal hbChild;
  @Value("${app.web.meals.full-board.adult:8000}") private BigDecimal fbAdult;
  @Value("${app.web.meals.full-board.child:4000}") private BigDecimal fbChild;
  public BigDecimal rate(String plan, boolean child) {
    var saved=jdbc.query("SELECT adult_price,child_price FROM web_meal_prices WHERE hotel_id=? AND plan_key=?", (rs,n)->rs.getBigDecimal(child?"child_price":"adult_price"), hotelId,plan);
    if(!saved.isEmpty()) return saved.get(0);
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
