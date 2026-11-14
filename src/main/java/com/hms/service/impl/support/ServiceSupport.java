package com.hms.service.impl.support;

import com.hms.dao.HmsDao;
import com.hms.entities.*;
import com.hms.exception.*;
import jakarta.validation.Validator;
import java.math.*;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceSupport {
  public final HmsDao dao;
  private final Validator validator;

  public UserEntity actor() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName()))
      throw new AuthenticationCredentialsNotFoundException("Authentication required");
    var users = dao.query(UserEntity.class, "e.username = :p0", auth.getName());
    if (users.size() != 1)
      throw new AccessDeniedException("Authenticated account is not linked to a local user");
    return users.get(0);
  }

  public void allow(String... roles) {
    String role = actor().getRole().getType().toUpperCase(Locale.ROOT);
    if (Arrays.stream(roles).noneMatch(role::equals))
      throw new AccessDeniedException("This operation is not permitted for your role");
  }

  public boolean visible(Object entity) {
    var user = actor();
    String role = user.getRole().getType().toUpperCase(Locale.ROOT);
    if ("ADMIN".equals(role)) return true;
    if (entity instanceof UserEntity value) return value.getUserId().equals(user.getUserId());
    if (entity instanceof AuditLogEntity) return "MANAGER".equals(role);
    boolean catalogue =
        entity instanceof HotelEntity
            || entity instanceof RoomEntity
            || entity instanceof RoomTypeEntity
            || entity instanceof RoomStatusEntity
            || entity instanceof RoomRateEntity
            || entity instanceof AccommodationEntity
            || entity instanceof HotelServiceEntity;
    if (Set.of("MANAGER", "RECEPTIONIST", "ACCOUNTANT").contains(role)) return true;
    if ("HOUSEKEEPING".equals(role)) return catalogue;
    if (!"GUEST".equals(role)) return false;
    if (catalogue) return true;
    if (entity instanceof CustomerEntity value) return owned(value, user);
    if (entity instanceof ReservationEntity value) return owned(value.getBookerCustomer(), user);
    if (entity instanceof ReservationRoomEntity value) return visible(value.getReservation());
    if (entity instanceof ReservationCustomerEntity value)
      return visible(value.getReservationRoom());
    if (entity instanceof InvoiceEntity value) return visible(value.getReservation());
    if (entity instanceof InvoiceItemEntity value) return visible(value.getInvoice());
    if (entity instanceof PaymentEntity value) return visible(value.getReservation());
    if (entity instanceof RefundEntity value) return visible(value.getPayment());
    if (entity instanceof ServiceChargeEntity value) return visible(value.getReservation());
    if (entity instanceof BookingStatusHistoryEntity value) return visible(value.getReservation());
    if (entity instanceof ReviewEntity value)
      return "approved".equals(value.getModerationStatus()) || owned(value.getCustomer(), user);
    return false;
  }

  private boolean owned(CustomerEntity customer, UserEntity user) {
    return customer.getUser() != null && customer.getUser().getUserId().equals(user.getUserId());
  }

  public void checkRead(Object entity) {
    if (!visible(entity)) throw new AccessDeniedException("You cannot access this record");
  }

  public void reviewOwner(CustomerEntity customer) {
    var user = actor();
    if ("GUEST".equalsIgnoreCase(user.getRole().getType()) && !owned(customer, user))
      throw new AccessDeniedException("Reviews must belong to your customer profile");
  }

  public void read() {
    allow("ADMIN", "MANAGER", "RECEPTIONIST", "ACCOUNTANT", "HOUSEKEEPING");
  }

  public void write() {
    allow("ADMIN", "MANAGER", "RECEPTIONIST");
    dao.lockHotels();
  }

  public void admin() {
    allow("ADMIN");
    dao.lockHotels();
  }

  public void finance() {
    allow("ADMIN", "MANAGER", "ACCOUNTANT");
    dao.lockHotels();
  }

  public void require(boolean condition, String message) {
    if (!condition) throw new BusinessRuleException(message);
  }

  public void validate(Object entity) {
    var errors = validator.validate(entity);
    if (!errors.isEmpty())
      throw new IllegalArgumentException(
          errors.stream()
              .map(e -> e.getPropertyPath() + ": " + e.getMessage())
              .sorted()
              .reduce((a, b) -> a + "; " + b)
              .orElse("Invalid request"));
  }

  public static BigDecimal money(BigDecimal value) {
    return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
  }

  public BigDecimal nonnegative(BigDecimal value) {
    var x = money(value);
    require(x.signum() >= 0, "Amounts cannot be negative");
    return x;
  }

  public BigDecimal positive(BigDecimal value) {
    var x = money(value);
    require(x.signum() > 0, "Amount must be greater than zero");
    return x;
  }

  public LocalDateTime now() {
    return LocalDateTime.now(ZoneOffset.UTC);
  }

  public LocalDate today(HotelEntity hotel) {
    return LocalDate.now(ZoneId.of(hotel.getTimezone()));
  }

  public String number(String prefix) {
    return prefix + "-" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
  }

  public void audit(String action, String entity, Long id) {
    var log = new AuditLogEntity();
    log.setActorUser(actor());
    log.setAction(action);
    log.setEntityType(entity);
    log.setEntityId(id);
    log.setOccurredAt(now());
    dao.persist(log);
  }

  public void history(
      ReservationEntity reservation,
      ReservationRoomEntity room,
      String oldStatus,
      String status,
      String reason) {
    var h = new BookingStatusHistoryEntity();
    h.setReservation(reservation);
    h.setReservationRoom(room);
    h.setOldStatus(oldStatus);
    h.setNewStatus(status);
    h.setReason(reason);
    h.setChangedBy(actor());
    h.setChangedAt(now());
    dao.persist(h);
  }

  public void same(Long a, Long b, String what) {
    require(Objects.equals(a, b), what + " must belong to the same hotel/reservation");
  }

  public void text(String value, String what) {
    require(value != null && !value.isBlank(), what + " is required");
  }
}
