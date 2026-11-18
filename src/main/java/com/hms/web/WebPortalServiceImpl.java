package com.hms.web;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hms.dao.*;
import com.hms.dto.*;
import com.hms.entities.*;
import com.hms.entities.enums.*;
import com.hms.exception.*;
import com.hms.security.ServiceSecurity;
import com.hms.service.*;
import com.hms.util.*;
import jakarta.persistence.*;
import java.math.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class WebPortalServiceImpl implements WebPortalService {
  @PersistenceContext private EntityManager em;
  private final HotelDAO hotels;
  private final RoomRateDAO rates;
  private final ReservationRoomDAO stays;
  private final CustomerDAO customers;
  private final UserDAO users;
  private final ServiceSecurity security;
  private final ReservationService reservations;
  private final InvoiceService invoices;
  private final InvoiceItemService invoiceItems;
  private final PaymentService paymentService;
  private final ReviewService reviewService;
  private final ObjectMapper json;
  private final WebMealPricing mealPricing;

  @Value("${app.web.hotel-id:0}")
  private long hotelId;

  private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);
  private static final Set<String> STAFF = Set.of("ADMIN", "MANAGER", "RECEPTIONIST", "ACCOUNTANT");
  private static final String SUITE = "Suite Double Room", DELUXE = "Deluxe Double Room";

  private Map<String, Object> m(Object... values) {
    var out = new LinkedHashMap<String, Object>();
    for (int i = 0; i < values.length; i += 2) out.put((String) values[i], values[i + 1]);
    return out;
  }

  private void need(boolean condition, String text) {
    if (!condition) throw new BusinessRuleException(text);
  }

  private BigDecimal money(BigDecimal n) {
    return n.setScale(2, RoundingMode.HALF_UP);
  }

  private <T> List<T> all(Class<T> type) {
    return em.createQuery("from " + type.getSimpleName(), type).getResultList();
  }

  private <T> T get(Class<T> type, Long id) {
    if (id == null) throw new IllegalArgumentException("ID required");
    var item = em.find(type, id);
    if (item == null) throw new ResourceNotFoundException("Record not found");
    return item;
  }

  private HotelEntity h() {
    need(hotelId > 0, "Set app.web.hotel-id to the active hotel ID in application.properties");
    var value = get(HotelEntity.class, hotelId);
    need(Boolean.TRUE.equals(value.getIsActive()), "Configured hotel is inactive");
    return value;
  }

  private void lock() {
    hotels.lockAllForWrite();
    h();
  }

  private boolean hotel(HotelEntity hotel) {
    return hotel != null && hotel.getId() == hotelId;
  }

  private UserEntity actor() {
    var a = SecurityContextHolder.getContext().getAuthentication();
    return a != null && a.isAuthenticated() && !"anonymousUser".equals(a.getName())
        ? security.getCurrentUser()
        : null;
  }

  private String role(UserEntity u) {
    return u == null ? "" : u.getRole().getType().toUpperCase(Locale.ROOT);
  }

  private CustomerEntity customer(UserEntity u) {
    return customers.findByUserId(u.getUserId()).stream().findFirst().orElse(null);
  }

  private String category(RoomTypeEntity t) {
    return SUITE.equals(t.getName()) ? "suite" : DELUXE.equals(t.getName()) ? "deluxe" : "unmapped";
  }

  private List<RoomEntity> inventory() {
    return all(RoomEntity.class).stream()
        .filter(r -> hotel(r.getHotel()) && !category(r.getRoomType()).equals("unmapped"))
        .sorted(
            Comparator.comparingInt(
                r -> {
                  try {
                    return Integer.parseInt(r.getRoomNumber());
                  } catch (Exception e) {
                    return 999;
                  }
                }))
        .toList();
  }

  private boolean sellable(RoomEntity r) {
    return "active".equals(r.getOperationalStatus())
        && r.getRoomStatus().getStatus() == RoomStatusValue.AVAILABLE
        && Boolean.TRUE.equals(r.getRoomType().getIsActive());
  }

  private Object room(RoomEntity r) {
    return m(
        "id",
        r.getId(),
        "number",
        r.getRoomNumber(),
        "category",
        category(r.getRoomType()),
        "status",
        sellable(r) ? "available" : "servicing",
        "housekeeping",
        "clean".equals(r.getHousekeepingStatus()) ? "clean" : "dirty",
        "location",
        r.getFloor());
  }

  private String plan(AccommodationType p) {
    return switch (p) {
      case ROOM_ONLY -> "room-only";
      case BED_AND_BREAKFAST -> "bb";
      case HALF_BOARD -> "half-board";
      case FULL_BOARD -> "full-board";
    };
  }

  private AccommodationEntity plan(String key) {
    return all(AccommodationEntity.class).stream()
        .filter(a -> hotel(a.getHotel()) && plan(a.getType()).equals(key))
        .findFirst()
        .orElseThrow(() -> new BusinessRuleException("Meal plan not configured"));
  }

  private void history(
      ReservationEntity r, ReservationRoomEntity room, String oldStatus, String status) {
    var event = new BookingStatusHistoryEntity();
    event.setReservation(r);
    event.setReservationRoom(room);
    event.setOldStatus(oldStatus);
    event.setNewStatus(status);
    event.setChangedBy(actor());
    event.setChangedAt(UtilData.generateNow());
    em.persist(event);
  }

  private void audit(String action, Long id) {
    var a = new AuditLogEntity();
    a.setHotel(h());
    a.setActorUser(actor());
    a.setAction(action);
    a.setEntityType("WebPortal");
    a.setEntityId(id);
    a.setOccurredAt(LocalDateTime.now(ZoneOffset.UTC));
    em.persist(a);
  }

  private String encode(Object value) {
    try {
      return json.writeValueAsString(value);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private String hash(String value) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private WebBookingEntity web(Long reservationId) {
    return em.createQuery(
            "from WebBookingEntity w where w.reservation.id=:id", WebBookingEntity.class)
        .setParameter("id", reservationId)
        .getResultStream()
        .findFirst()
        .orElseThrow(() -> new ResourceNotFoundException("Website booking not found"));
  }

  private void access(WebBookingEntity w, String token) {
    need(hotel(w.getReservation().getHotel()), "Wrong hotel");
    var u = actor();
    if (u != null
        && (STAFF.contains(role(u))
            || (w.getReservation().getBookerCustomer().getUser() != null
                && w.getReservation()
                    .getBookerCustomer()
                    .getUser()
                    .getUserId()
                    .equals(u.getUserId())))) return;
    if (token != null
        && token.length() >= 32
        && token.length() <= 200
        && w.getAccessExpires().isAfter(Instant.now())
        && MessageDigest.isEqual(
            hash(token).getBytes(StandardCharsets.UTF_8),
            w.getAccessHash().getBytes(StandardCharsets.UTF_8))) return;
    throw new AccessDeniedException("Booking access required");
  }

  private BigDecimal paid(ReservationEntity r) {
    return all(PaymentEntity.class).stream()
        .filter(
            p -> p.getReservation().getId().equals(r.getId()) && "completed".equals(p.getStatus()))
        .map(
            p ->
                p.getAmount()
                    .subtract(
                        all(RefundEntity.class).stream()
                            .filter(
                                f ->
                                    f.getPayment().getId().equals(p.getId())
                                        && "completed".equals(f.getStatus()))
                            .map(RefundEntity::getAmount)
                            .reduce(ZERO, BigDecimal::add)))
        .reduce(ZERO, BigDecimal::add);
  }

  private JsonNode view(WebBookingEntity w) {
    try {
      var n = (ObjectNode) json.readTree(w.getSnapshotJson());
      var r = w.getReservation();
      n.put(
          "status",
          switch (r.getStatus()) {
            case "checked_in" -> "checked-in";
            case "checked_out" -> "completed";
            default -> r.getStatus();
          });
      n.put("paid", paid(r));
      return n;
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public Object hotel() {
    var h = h();
    return m(
        "id",
        h.getId(),
        "name",
        h.getName(),
        "phone",
        h.getPhone(),
        "address",
        h.getAddress(),
        "checkInWindow",
        h.getCheckInTime().toString(),
        "checkOutWindow",
        h.getCheckOutTime().toString());
  }

  @Override
  public Object hotel(WebRequests.Hotel input) {
    security.requireRoles("ADMIN", "MANAGER");
    lock();
    var h = h();
    h.setName(input.name());
    h.setPhone(input.phone());
    h.setAddress(input.address());
    h.setCheckInTime(LocalTime.parse(input.checkInWindow()));
    h.setCheckOutTime(LocalTime.parse(input.checkOutWindow()));
    audit("HOTEL_UPDATE", h.getId());
    return hotel();
  }

  @Override
  public Object rooms() {
    h();
    return inventory().stream().map(this::room).toList();
  }

  @Override
  public Object roomTypes() {
    var now = UtilData.todayAtHotel(h());
    return all(RoomTypeEntity.class).stream()
        .filter(
            t ->
                hotel(t.getHotel())
                    && !category(t).equals("unmapped")
                    && Boolean.TRUE.equals(t.getIsActive()))
        .map(
            t ->
                m(
                    "id",
                    category(t),
                    "databaseId",
                    t.getId(),
                    "name",
                    t.getName(),
                    "description",
                    t.getDescription(),
                    "nightlyRate",
                    rates
                        .findByRoomTypeIdAndStayDate(t.getId(), now)
                        .map(RoomRateEntity::getNightlyRate)
                        .orElse(ZERO),
                    "area",
                    17,
                    "buildingStyle",
                    category(t).equals("suite") ? "Detached garden villa" : "Adjoining rooms",
                    "kitchen",
                    category(t).equals("suite"),
                    "maxAdults", t.getMaxAdults(), "maxChildren", t.getMaxChildren(), "maxOccupancy", t.getMaxOccupancy(),
                    "images",
                    List.of(),
                    "amenities",
                    List.of(
                        "King-sized bed",
                        "Air conditioning",
                        "Attached bathroom",
                        "Hot water",
                        "Workspace",
                        category(t).equals("suite") ? "Kitchen & electric kettle" : "Balcony",
                        category(t).equals("suite") ? "Ground-floor veranda / upper-floor balcony" : "No kitchen")))
        .toList();
  }

  @Override
  public Object plans() {
    h();
    return all(AccommodationEntity.class).stream().filter(a -> hotel(a.getHotel()))
      .map(a -> { String key = plan(a.getType()); return m("id", key,
        "name", mealPricing.name(key), "description", mealPricing.description(key),
        "adultSupplement", mealPricing.rate(key, false), "childSupplement", mealPricing.rate(key, true)); })
      .toList();
  }

  private void dates(LocalDate in, LocalDate out) {
    need(
        in != null
            && out != null
            && out.isAfter(in)
            && !in.isBefore(UtilData.todayAtHotel(h()))
            && ChronoUnit.DAYS.between(in, out) <= 90,
        "Choose future dates for a stay of 1–90 nights");
  }

  private List<RoomEntity> available(LocalDate in, LocalDate out) {
    dates(in, out);
    var candidates =
        inventory().stream()
            .filter(this::sellable)
            .filter(r -> stays.findBlockingOverlapsByRoomId(r.getId(), in, out).isEmpty())
            .toList();
    var result = new ArrayList<RoomEntity>();
    for (String c : List.of("suite", "deluxe")) {
      var group = candidates.stream().filter(r -> category(r.getRoomType()).equals(c)).toList();
      if (group.isEmpty()) continue;
      var t = group.get(0).getRoomType();
      int free = group.size();
      var blocked = stays.findBlockingOverlapsByRoomTypeId(t.getId(), in, out);
      for (LocalDate day = in; day.isBefore(out); day = day.plusDays(1)) {
        final LocalDate d = day;
        long unassigned =
            blocked.stream()
                .filter(
                    s ->
                        s.getRoom() == null
                            && !d.isBefore(s.getCheckInDate())
                            && d.isBefore(s.getCheckOutDate()))
                .count();
        free = Math.min(free, group.size() - (int) unassigned);
      }
      result.addAll(group.subList(0, Math.max(0, free)));
    }
    return result;
  }

  @Override
  public Object availability(WebRequests.Search s) {
    var rooms = available(s.checkIn(), s.checkOut());
    return m(
        "rooms",
        rooms.stream().map(this::room).toList(),
        "suite",
        rooms.stream().filter(r -> category(r.getRoomType()).equals("suite")).count(),
        "deluxe",
        rooms.stream().filter(r -> category(r.getRoomType()).equals("deluxe")).count(),
        "total",
        rooms.size());
  }

  private List<RoomEntity> selected(WebRequests.Draft d) {
    need(d.allocations()!=null && d.rooms()>=1 && d.rooms()<=6 && d.allocations().size()==d.rooms(), "Choose between 1 and 6 rooms");
    int adults=0, children=0, suites=0;
    for (var a : d.allocations()) {
      need(a!=null && Set.of("suite", "deluxe").contains(a.category()), "Select a valid room type");
      need(a.adults()>=1 && a.adults()<=2 && a.children()>=0 && a.children()<=2 && a.adults()+a.children()<=3,
        "Each room needs 1–2 adults and allows at most 3 guests in total");
      adults+=a.adults(); children+=a.children(); if(a.category().equals("suite")) suites++;
    }
    need(adults==d.adults() && children==d.children() && suites==d.suite() && d.deluxe()==d.rooms()-suites,
      "Guest totals must match the individual rooms");
    plan(d.mealPlan());
    var free = available(d.checkIn(), d.checkOut());
    need(d.selectedRoomIds()!=null, "Room selection is required");
    if(d.selectRooms()) need(d.selectedRoomIds().size()==d.rooms() && new HashSet<>(d.selectedRoomIds()).size()==d.rooms(),
      "Select distinct rooms for the whole booking");
    var chosen = new ArrayList<RoomEntity>();
    for (int i=0; i<d.allocations().size(); i++) {
      var allocation=d.allocations().get(i);
      final Long wanted=d.selectRooms() ? d.selectedRoomIds().get(i) : null;
      var room=free.stream().filter(r -> !chosen.contains(r) && category(r.getRoomType()).equals(allocation.category()))
        .filter(r -> wanted==null || r.getId().equals(wanted))
        .filter(r -> allocation.adults()<=r.getRoomType().getMaxAdults()
          && allocation.children()<=r.getRoomType().getMaxChildren()
          && allocation.adults()+allocation.children()<=r.getRoomType().getMaxOccupancy())
        .findFirst().orElseThrow(() -> new BusinessRuleException("A requested room is unavailable or cannot accommodate its guests; update your selection"));
      chosen.add(room);
    }
    return chosen;
  }

  private BigDecimal roomPrice(RoomEntity r, WebRequests.Draft d) {
    BigDecimal total = ZERO;
    long nights = ChronoUnit.DAYS.between(d.checkIn(), d.checkOut());
    for (LocalDate day = d.checkIn(); day.isBefore(d.checkOut()); day = day.plusDays(1)) {
      var rate =
          rates
              .findByRoomTypeIdAndStayDate(r.getRoomType().getId(), day)
              .orElseThrow(
                  () ->
                      new BusinessRuleException(
                          "Rates are not configured for every requested night"));
      need(
          !Boolean.TRUE.equals(rate.getStopSell()) && nights >= rate.getMinimumNights(),
          "Stay restrictions prevent this booking");
      total = total.add(rate.getNightlyRate());
    }
    return money(total);
  }

  private BigDecimal mealTotal(WebRequests.Draft d, int adults, int children) {
    return money(mealPricing.rate(d.mealPlan(), false).multiply(BigDecimal.valueOf(adults))
      .add(mealPricing.rate(d.mealPlan(), true).multiply(BigDecimal.valueOf(children)))
      .multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(d.checkIn(), d.checkOut()))));
  }

  private Map<String, Object> quoteMap(WebRequests.Draft d, List<RoomEntity> chosen) {
    var lines = new ArrayList<Object>();
    BigDecimal total = ZERO;
    long nights=ChronoUnit.DAYS.between(d.checkIn(), d.checkOut());
    for (var r : chosen) {
      var amount = roomPrice(r, d);
      lines.add(m("label", r.getRoomType().getName()+" · Room "+r.getRoomNumber()+" · "+nights+" night(s)", "amount", amount));
      total=total.add(amount);
    }
    if(!d.mealPlan().equals("room-only")) {
      if(d.adults()>0) lines.add(m("label", mealPricing.name(d.mealPlan())+" · "+d.adults()+" adults × "+mealPricing.rate(d.mealPlan(), false)+" × "+nights+" nights", "amount", mealTotal(d,d.adults(),0)));
      if(d.children()>0) lines.add(m("label", mealPricing.name(d.mealPlan())+" · "+d.children()+" children × "+mealPricing.rate(d.mealPlan(), true)+" × "+nights+" nights", "amount", mealTotal(d,0,d.children())));
    }
    total=total.add(mealTotal(d,d.adults(),d.children()));
    return m("nights", nights, "lines", lines, "total", total, "currency", h().getCurrencyCode(),
      "cancellationDeadline", d.checkIn().atStartOfDay(ZoneId.of(h().getTimezone())).toOffsetDateTime().toString());
  }

  @Override
  public Object quote(WebRequests.Draft d) {
    return quoteMap(d, selected(d));
  }

  @Override
  public JsonNode create(WebRequests.Create input, String token) {
    need(token != null && token.matches("[a-zA-Z0-9-]{32,200}"), "A guest access key is required");
    lock();
    var previous =
        em.createQuery("from WebBookingEntity w where w.requestKey=:key", WebBookingEntity.class)
            .setParameter("key", input.idempotencyKey())
            .getResultStream()
            .findFirst();
    String fingerprint = hash(encode(input));
    if (previous.isPresent()) {
      access(previous.get(), token);
      need(
          previous.get().getRequestHash().equals(fingerprint),
          "Request key reused with different details");
      return view(previous.get());
    }
    var d = input.draft();
    var chosen = selected(d);
    var q = quoteMap(d, chosen);
    need(
        ((BigDecimal) q.get("total")).compareTo(input.expectedTotal()) == 0,
        "Price changed; review a fresh quote");
    var u = actor();
    var c = u != null && role(u).equals("GUEST") ? customer(u) : null;
    if (c == null) {
      c = new CustomerEntity();
      if (u != null && role(u).equals("GUEST")) c.setUser(u);
    }
    c.setName(input.guest().name().trim());
    c.setEmail(input.guest().email().trim());
    c.setPhone(input.guest().phone().trim());
    if (c.getId() == null) em.persist(c);
    var r = new ReservationEntity();
    r.setHotel(h());
    r.setBookingReference(UtilData.generateNumber("RES"));
    r.setBookerCustomer(c);
    r.setStatus("confirmed");
    r.setBookingSource("website");
    r.setCurrencyCode(h().getCurrencyCode());
    r.setSpecialRequests(input.guest().notes());
    r.setCreatedBy(u);
    r.setConfirmedAt(UtilData.generateNow());
    em.persist(r);
    history(r, null, null, "confirmed");
    int allocationIndex = 0;
    long nights = ChronoUnit.DAYS.between(d.checkIn(), d.checkOut());
    for (var room : chosen) {
      var allocation = d.allocations().get(allocationIndex++);
      int adults = allocation.adults();
      int children = allocation.children();
      var t = room.getRoomType();
      need(
          adults <= t.getMaxAdults()
              && children <= t.getMaxChildren()
              && adults + children <= t.getMaxOccupancy(),
          "Occupancy exceeds configured room capacity");
      var s = new ReservationRoomEntity();
      s.setReservation(r);
      s.setRoom(room);
      s.setRoomType(t);
      s.setCheckInDate(d.checkIn());
      s.setCheckOutDate(d.checkOut());
      s.setAdults(adults);
      s.setChildren(children);
      var total = roomPrice(room, d).add(mealTotal(d, adults, children));
      var average = total.divide(BigDecimal.valueOf(nights), 2, RoundingMode.DOWN);
      s.setNightlyRate(average);
      s.setExtraPersonTotal(total.subtract(average.multiply(BigDecimal.valueOf(nights))));
      s.setDiscountAmount(ZERO);
      s.setTaxAmount(ZERO);
      s.setServiceChargeAmount(ZERO);
      s.setTotalAmount(total);
      s.setCancellationPolicySnapshot(
          "Unpaid bookings: cancel before check-in day at hotel midnight. Prices include configured"
              + " charges.");
      s.setStatus("confirmed");
      s.setAccommodation(plan(d.mealPlan()));
      em.persist(s);
      history(r, s, null, "confirmed");
      var guest = new ReservationCustomerEntity();
      guest.setReservationRoom(s);
      guest.setCustomer(c);
      guest.setIsPrimaryCustomer(true);
      em.persist(guest);
    }
    em.flush();
    var snapshot =
        m(
            "id",
            r.getId().toString(),
            "reference",
            r.getBookingReference(),
            "userId",
            c.getUser() == null ? null : c.getUser().getUserId(),
            "draft",
            d,
            "roomIds",
            chosen.stream().map(RoomEntity::getId).toList(),
            "roomNumbers",
            chosen.stream().map(RoomEntity::getRoomNumber).toList(),
            "guest",
            m(
                "name",
                input.guest().name(),
                "email",
                input.guest().email(),
                "phone",
                input.guest().phone(),
                "documentType",
                input.guest().documentType(),
                "documentLastFour",
                "",
                "notes",
                input.guest().notes()),
            "quote",
            q,
            "status",
            "confirmed",
            "createdAt",
            r.getCreatedAt().atOffset(ZoneOffset.UTC).toString(),
            "idempotencyKey",
            input.idempotencyKey(),
            "paid",
            ZERO);
    var w = new WebBookingEntity();
    w.setReservation(r);
    w.setRequestKey(input.idempotencyKey());
    w.setRequestHash(fingerprint);
    w.setAccessHash(hash(token));
    w.setAccessExpires(
        d.checkOut().plusDays(30).atStartOfDay(ZoneId.of(h().getTimezone())).toInstant());
    w.setSnapshotJson(encode(snapshot));
    em.persist(w);
    audit("WEBSITE_BOOKING", r.getId());
    em.flush();
    return view(w);
  }

  @Override
  public JsonNode booking(Long id, String access) {
    var w = web(id);
    access(w, access);
    return view(w);
  }

  @Override
  public Object bookings(boolean mine) {
    var u = security.getCurrentUser();
    if (!mine) security.requireRoles("ADMIN", "MANAGER", "RECEPTIONIST", "ACCOUNTANT");
    return all(WebBookingEntity.class).stream()
        .filter(w -> hotel(w.getReservation().getHotel()))
        .filter(
            w ->
                !mine
                    || (w.getReservation().getBookerCustomer().getUser() != null
                        && w.getReservation()
                            .getBookerCustomer()
                            .getUser()
                            .getUserId()
                            .equals(u.getUserId())))
        .sorted(Comparator.comparing(WebBookingEntity::getId).reversed())
        .map(this::view)
        .toList();
  }

  @Override
  public JsonNode cancel(Long id, String token) {
    lock();
    var w = web(id);
    access(w, token);
    var r = w.getReservation();
    need("confirmed".equals(r.getStatus()), "Only confirmed bookings may be cancelled");
    var rows = stays.findByReservationId(id);
    need(
        rows.stream().allMatch(s -> s.getCheckInDate().isAfter(UtilData.todayAtHotel(h()))),
        "Cancellation deadline has passed; contact the hotel");
    need(paid(r).signum() == 0, "Paid bookings require the hotel's refund workflow");
    need(
        all(InvoiceEntity.class).stream()
            .noneMatch(i -> i.getReservation().getId().equals(id) && !"void".equals(i.getStatus())),
        "Resolve invoices before cancellation");
    need(
        all(ServiceChargeEntity.class).stream()
            .noneMatch(
                c -> c.getReservation().getId().equals(id) && "active".equals(c.getStatus())),
        "Resolve service charges first");
    history(r, null, r.getStatus(), "cancelled");
    r.setStatus("cancelled");
    r.setCancelledAt(UtilData.generateNow());
    r.setCancellationReason("Website cancellation");
    for (var s : rows) {
      history(r, s, s.getStatus(), "cancelled");
      s.setStatus("cancelled");
    }
    audit("WEBSITE_CANCEL", id);
    return view(w);
  }

  @Override
  public JsonNode transition(Long id, String status) {
    security.requireRoles("ADMIN", "MANAGER", "RECEPTIONIST");
    lock();
    var w = web(id);
    need(hotel(w.getReservation().getHotel()), "Wrong hotel");
    if ("checked-in".equals(status)) reservations.checkInReservation(id);
    else if ("completed".equals(status)) reservations.checkOutReservation(id);
    else throw new IllegalArgumentException("Invalid transition");
    return view(w);
  }

  @Override
  public Object condition(Long id, WebRequests.Condition input) {
    security.requireRoles("ADMIN", "MANAGER", "RECEPTIONIST", "HOUSEKEEPING");
    lock();
    var r = get(RoomEntity.class, id);
    need(hotel(r.getHotel()), "Wrong hotel");
    if (input.status().equals("servicing"))
      need(
          stays.findByRoomId(id).stream()
              .noneMatch(
                  s ->
                      Set.of("confirmed", "checked_in").contains(s.getStatus())
                          && s.getCheckOutDate().isAfter(UtilData.todayAtHotel(h()))),
          "Reassign active and future bookings before servicing");
    var desired =
        input.status().equals("available") ? RoomStatusValue.AVAILABLE : RoomStatusValue.SERVICING;
    var status =
        all(RoomStatusEntity.class).stream()
            .filter(s -> s.getStatus() == desired)
            .findFirst()
            .orElseThrow(() -> new BusinessRuleException("Room status missing"));
    r.setRoomStatus(status);
    r.setHousekeepingStatus(input.housekeeping());
    audit("ROOM_CONDITION", id);
    return room(r);
  }

  private Object user(UserEntity u) {
    var c = customer(u);
    return m(
        "id",
        u.getUserId(),
        "name",
        c == null ? u.getUsername() : c.getName(),
        "username",
        u.getUsername(),
        "email",
        u.getEmail(),
        "phone",
        c == null ? "" : c.getPhone(),
        "role",
        role(u));
  }

  @Override
  public Object profile() {
    return user(security.getCurrentUser());
  }

  @Override
  public Object profile(WebRequests.Profile p) {
    lock();
    var u = security.getCurrentUser();
    var c = customer(u);
    if (c == null) {
      c = new CustomerEntity();
      c.setUser(u);
      c.setEmail(u.getEmail());
      c.setName(p.name());
      c.setPhone(p.phone());
      em.persist(c);
    } else {
      c.setName(p.name());
      c.setPhone(p.phone());
    }
    audit("PROFILE_UPDATE", u.getUserId());
    return user(u);
  }

  @Override
  public Object customers() {
    security.requireRoles("ADMIN", "MANAGER", "RECEPTIONIST");
    return all(CustomerEntity.class).stream()
        .map(
            c ->
                m(
                    "id",
                    c.getId(),
                    "name",
                    c.getName(),
                    "username",
                    c.getUser() == null ? "Walk-in / guest" : c.getUser().getUsername(),
                    "email",
                    c.getEmail(),
                    "phone",
                    c.getPhone(),
                    "role",
                    "GUEST"))
        .toList();
  }

  private Object review(ReviewEntity r, boolean publicView) {
    return m(
        "id",
        r.getId().toString(),
        "bookingId",
        publicView ? "" : r.getReservation().getId().toString(),
        "userId",
        publicView
            ? 0
            : r.getCustomer().getUser() == null ? 0 : r.getCustomer().getUser().getUserId(),
        "author",
        r.getCustomer().getName().split(" ")[0],
        "rating",
        r.getRating(),
        "title",
        r.getTitle(),
        "comment",
        r.getComment(),
        "status",
        r.getModerationStatus(),
        "createdAt",
        r.getCreatedAt().atOffset(ZoneOffset.UTC).toString());
  }

  @Override
  public Object reviews(String scope) {
    UserEntity u = null;
    if (!"public".equals(scope)) u = security.getCurrentUser();
    if ("all".equals(scope)) security.requireRoles("ADMIN", "MANAGER");
    final var account = u;
    return all(ReviewEntity.class).stream()
        .filter(r -> hotel(r.getReservation().getHotel()))
        .filter(
            r ->
                "public".equals(scope)
                    ? "approved".equals(r.getModerationStatus())
                    : "all".equals(scope)
                        || (r.getCustomer().getUser() != null
                            && r.getCustomer().getUser().getUserId().equals(account.getUserId())))
        .map(r -> review(r, "public".equals(scope)))
        .toList();
  }

  @Override
  public Object review(WebRequests.Review input) {
    lock();
    var u = security.getCurrentUser();
    var w = web(input.bookingId());
    access(w, null);
    var r = w.getReservation();
    need(
        r.getBookerCustomer().getUser() != null
            && r.getBookerCustomer().getUser().getUserId().equals(u.getUserId()),
        "Only the booker may review");
    need(
        all(ReviewEntity.class).stream()
            .noneMatch(v -> v.getReservation().getId().equals(r.getId())),
        "This stay already has a review");
    var d = new ReviewDTO();
    d.setReservationId(r.getId());
    d.setCustomerId(r.getBookerCustomer().getId());
    d.setRating(input.rating());
    d.setTitle(input.title());
    d.setComment(input.comment());
    var saved = reviewService.addReview(d);
    return review(get(ReviewEntity.class, saved.getId()), false);
  }

  @Override
  public Object moderate(Long id, String status) {
    security.requireRoles("ADMIN", "MANAGER");
    need(hotel(get(ReviewEntity.class, id).getReservation().getHotel()), "Wrong hotel");
    reviewService.moderateReview(id, status);
    return review(get(ReviewEntity.class, id), false);
  }

  @Override
  public Object payments() {
    security.requireRoles("ADMIN", "MANAGER", "ACCOUNTANT");
    return all(PaymentEntity.class).stream()
        .filter(p -> hotel(p.getHotel()))
        .map(
            p ->
                m(
                    "id",
                    p.getId().toString(),
                    "bookingId",
                    p.getReservation().getId().toString(),
                    "amount",
                    p.getAmount(),
                    "method",
                    p.getPaymentMethod(),
                    "createdAt",
                    p.getPaidAt().atOffset(ZoneOffset.UTC).toString(),
                    "actorId",
                    p.getReceivedBy().getUserId()))
        .toList();
  }

  @Override
  public Object cash(WebRequests.Cash input) {
    security.requireRoles("ADMIN", "MANAGER", "ACCOUNTANT");
    lock();
    var r = web(input.bookingId()).getReservation();
    need(
        hotel(r.getHotel()) && Set.of("confirmed", "checked_in").contains(r.getStatus()),
        "Active website booking required");
    var previous =
        all(PaymentEntity.class).stream()
            .filter(p -> hotel(p.getHotel()) && input.requestKey().equals(p.getReferenceNumber()))
            .findFirst();
    if (previous.isPresent()) {
      need(
          previous.get().getReservation().getId().equals(r.getId())
              && previous.get().getAmount().compareTo(input.amount()) == 0,
          "Payment request key already used");
      return m("receiptNumber", previous.get().getReceiptNumber());
    }
    var existing =
        all(InvoiceEntity.class).stream()
            .filter(
                i -> i.getReservation().getId().equals(r.getId()) && !"void".equals(i.getStatus()))
            .toList();
    InvoiceDTO invoice;
    if (existing.isEmpty()) {
      var d = new InvoiceDTO();
      d.setHotelId(hotelId);
      d.setReservationId(r.getId());
      d.setBilledCustomerId(r.getBookerCustomer().getId());
      d.setDueDate(UtilData.todayAtHotel(h()));
      invoice = invoices.addInvoice(d);
      for (var s : stays.findNonCancelledByReservationId(r.getId())) {
        var line = new InvoiceItemDTO();
        line.setInvoiceId(invoice.getId());
        line.setReservationRoomId(s.getId());
        invoiceItems.addInvoiceItem(line);
      }
      invoice = invoices.issueInvoice(invoice.getId());
    } else {
      need(
          existing.size() == 1 && "issued".equals(existing.get(0).getStatus()),
          "Resolve multiple or draft invoices using the accounting API");
      invoice = invoices.getSelectedInvoice(existing.get(0).getId());
    }
    var p = new PaymentDTO();
    p.setHotelId(hotelId);
    p.setReservationId(r.getId());
    p.setInvoiceId(invoice.getId());
    p.setAmount(input.amount());
    p.setPaymentMethod("cash");
    p.setReferenceNumber(input.requestKey());
    p.setNotes("Website staff cash collection");
    return paymentService.addPayment(p);
  }

  @Override
  public Object users() {
    security.requireRoles("ADMIN");
    return all(UserEntity.class).stream().map(this::user).toList();
  }

  @Override
  public Object role(Long id, String next) {
    security.requireRoles("ADMIN");
    lock();
    var me = security.getCurrentUser();
    need(!me.getUserId().equals(id), "Cannot change your own role here");
    var u = get(UserEntity.class, id);
    need(
        !role(u).equals("ADMIN") || next.equals("ADMIN") || users.findAdministrators().size() > 1,
        "Keep at least one administrator");
    var r =
        all(RoleEntity.class).stream()
            .filter(v -> v.getType().equals(next))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Role not found"));
    u.setRole(r);
    audit("ROLE_UPDATE", id);
    return user(u);
  }

  @Override
  public Object audit() {
    security.requireRoles("ADMIN", "MANAGER");
    return all(AuditLogEntity.class).stream()
        .filter(a -> hotel(a.getHotel()))
        .sorted(Comparator.comparing(AuditLogEntity::getId).reversed())
        .limit(500)
        .map(
            a ->
                m(
                    "id",
                    a.getId().toString(),
                    "action",
                    a.getAction() + " " + a.getEntityType() + " #" + a.getEntityId(),
                    "actorId",
                    a.getActorUser() == null ? null : a.getActorUser().getUserId(),
                    "createdAt",
                    a.getOccurredAt().atOffset(ZoneOffset.UTC).toString()))
        .toList();
  }

  @Override
  public Object setup(WebRequests.Setup in) {
    security.requireRoles("ADMIN");
    lock();
    need(
        !in.start().isBefore(UtilData.todayAtHotel(h()))
            && in.end().isAfter(in.start())
            && ChronoUnit.DAYS.between(in.start(), in.end()) <= 366,
        "Rate coverage must be 1–366 future days");
    var roomList = all(RoomEntity.class).stream().filter(r -> hotel(r.getHotel())).toList();
    need(
        roomList.isEmpty()
            || (roomList.size() == 18
                && roomList.stream().allMatch(r -> !category(r.getRoomType()).equals("unmapped"))),
        "Existing inventory must be reconciled manually; setup will not overwrite it");
    if (!roomList.isEmpty()) {
      need(
          roomList.stream().filter(r -> category(r.getRoomType()).equals("suite")).count() == 12
              && roomList.stream().filter(r -> category(r.getRoomType()).equals("deluxe")).count()
                  == 6,
          "Inventory must contain exactly 12 Suite and 6 Deluxe rooms");
      need(roomList.stream().allMatch(r -> category(r.getRoomType()).equals(Integer.parseInt(r.getRoomNumber())<=6 ? "deluxe" : "suite")), "Room numbering does not match Deluxe 1–6 and Suite 7–18; reconcile manually without deleting bookings");
      need(
          roomList.stream()
              .map(RoomEntity::getRoomNumber)
              .collect(java.util.stream.Collectors.toSet())
              .equals(
                  java.util.stream.IntStream.rangeClosed(1, 18)
                      .mapToObj(String::valueOf)
                      .collect(java.util.stream.Collectors.toSet())),
          "Existing room numbers must be 1 through 18");
    }
    var types = new ArrayList<RoomTypeEntity>();
    for (String name : List.of(SUITE, DELUXE)) {
      var t =
          all(RoomTypeEntity.class).stream()
              .filter(v -> hotel(v.getHotel()) && v.getName().equals(name))
              .findFirst()
              .orElse(null);
      if (t == null) {
        t = new RoomTypeEntity();
        t.setHotel(h());
        t.setName(name);
        t.setDescription("King bed, air conditioning, attached bathroom, hot water and workspace.");
        t.setMaxAdults(2);
        t.setMaxChildren(2);
        t.setMaxOccupancy(3);
        t.setBedDescription("King-sized bed");
        t.setIsActive(true);
        em.persist(t);
      }
      if(roomList.isEmpty()) { t.setMaxAdults(2); t.setMaxChildren(2); t.setMaxOccupancy(3); }
      types.add(t);
    }
    var status =
        all(RoomStatusEntity.class).stream()
            .filter(s -> s.getStatus() == RoomStatusValue.AVAILABLE)
            .findFirst()
            .orElse(null);
    if (status == null) {
      status = new RoomStatusEntity();
      status.setStatus(RoomStatusValue.AVAILABLE);
      em.persist(status);
    }
    for (var value : RoomStatusValue.values()) {
      if (all(RoomStatusEntity.class).stream().noneMatch(s -> s.getStatus() == value)) {
        var s = new RoomStatusEntity();
        s.setStatus(value);
        em.persist(s);
      }
    }
    if (roomList.isEmpty())
      for (int n = 1; n <= 18; n++) {
        var r = new RoomEntity();
        r.setHotel(h());
        r.setRoomType(types.get(n <= 6 ? 1 : 0));
        r.setRoomNumber(String.valueOf(n));
        r.setFloor(n <= 6 ? (n <= 3 ? "Deluxe · 1st floor · balcony" : "Deluxe · 2nd floor · balcony")
          : "Villa " + ((n-7)/3+1) + " · " + (List.of("Ground floor · veranda", "1st floor · balcony", "2nd floor · balcony").get((n-7)%3)));
        r.setOperationalStatus("active");
        r.setHousekeepingStatus("clean");
        r.setNotes("");
        r.setRoomStatus(status);
        em.persist(r);
      }
    em.flush();
    int added = 0;
    for (int index = 0; index < 2; index++)
      for (LocalDate date = in.start(); date.isBefore(in.end()); date = date.plusDays(1)) {
        if (rates.findByRoomTypeIdAndStayDate(types.get(index).getId(), date).isPresent()) continue;
        var rate = new RoomRateEntity();
        rate.setRoomType(types.get(index));
        rate.setStayDate(date);
        rate.setNightlyRate(money(index == 0 ? in.suiteRate() : in.deluxeRate()));
        rate.setExtraAdultRate(ZERO);
        rate.setExtraChildRate(ZERO);
        rate.setMinimumNights(1);
        rate.setStopSell(false);
        em.persist(rate);
        added++;
      }
    for (var value : AccommodationType.values()) {
      if (all(AccommodationEntity.class).stream()
          .anyMatch(a -> hotel(a.getHotel()) && a.getType() == value)) continue;
      var a = new AccommodationEntity();
      a.setHotel(h());
      a.setType(value);
      a.setPrice(
          money(
              switch (value) {
                case ROOM_ONLY -> ZERO;
                case BED_AND_BREAKFAST -> in.breakfast();
                case HALF_BOARD -> in.halfBoard();
                case FULL_BOARD -> in.fullBoard();
              }));
      em.persist(a);
    }
    audit("INVENTORY_SETUP", hotelId);
    return m(
        "message",
        "18 rooms configured: Deluxe 1–6, Suite villas 7–18. Existing daily rates preserved. Website meal supplements come from app.web.meals properties.",
        "newDailyRates",
        added);
  }
}
