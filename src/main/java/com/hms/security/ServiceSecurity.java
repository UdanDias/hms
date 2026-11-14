package com.hms.security;

import com.hms.dao.UserDAO;
import com.hms.entities.AccommodationEntity;
import com.hms.entities.AuditLogEntity;
import com.hms.entities.BookingStatusHistoryEntity;
import com.hms.entities.CustomerEntity;
import com.hms.entities.HotelEntity;
import com.hms.entities.HotelServiceEntity;
import com.hms.entities.InvoiceEntity;
import com.hms.entities.InvoiceItemEntity;
import com.hms.entities.PaymentEntity;
import com.hms.entities.RefundEntity;
import com.hms.entities.ReservationCustomerEntity;
import com.hms.entities.ReservationEntity;
import com.hms.entities.ReservationRoomEntity;
import com.hms.entities.ReviewEntity;
import com.hms.entities.RoomEntity;
import com.hms.entities.RoomRateEntity;
import com.hms.entities.RoomStatusEntity;
import com.hms.entities.RoomTypeEntity;
import com.hms.entities.ServiceChargeEntity;
import com.hms.entities.UserEntity;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceSecurity {
  private final UserDAO userDao;

  public UserEntity getCurrentUser() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName()))
      throw new AuthenticationCredentialsNotFoundException("Authentication required");
    var users = userDao.findAccountsByUsername(auth.getName());
    if (users.size() != 1)
      throw new AccessDeniedException("Authenticated account is not linked to a local user");
    return users.get(0);
  }

  public void requireRoles(String... roles) {
    String role = getCurrentUser().getRole().getType().toUpperCase(Locale.ROOT);
    if (Arrays.stream(roles).noneMatch(role::equals))
      throw new AccessDeniedException("This operation is not permitted for your role");
  }

  public boolean canRead(Object entity) {
    var user = getCurrentUser();
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
    if (entity instanceof ReservationRoomEntity value) return canRead(value.getReservation());
    if (entity instanceof ReservationCustomerEntity value)
      return canRead(value.getReservationRoom());
    if (entity instanceof InvoiceEntity value) return canRead(value.getReservation());
    if (entity instanceof InvoiceItemEntity value) return canRead(value.getInvoice());
    if (entity instanceof PaymentEntity value) return canRead(value.getReservation());
    if (entity instanceof RefundEntity value) return canRead(value.getPayment());
    if (entity instanceof ServiceChargeEntity value) return canRead(value.getReservation());
    if (entity instanceof BookingStatusHistoryEntity value) return canRead(value.getReservation());
    if (entity instanceof ReviewEntity value)
      return "approved".equals(value.getModerationStatus()) || owned(value.getCustomer(), user);
    return false;
  }

  private boolean owned(CustomerEntity customer, UserEntity user) {
    return customer.getUser() != null && customer.getUser().getUserId().equals(user.getUserId());
  }

  public void checkRead(Object entity) {
    if (!canRead(entity)) throw new AccessDeniedException("You cannot access this record");
  }

  public void checkReviewOwner(CustomerEntity customer) {
    var user = getCurrentUser();
    if ("GUEST".equalsIgnoreCase(user.getRole().getType()) && !owned(customer, user))
      throw new AccessDeniedException("Reviews must belong to your customer profile");
  }
}
