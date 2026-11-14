package com.hms.util;

import com.hms.dao.HmsDao;
import com.hms.dto.*;
import com.hms.entities.*;
import com.hms.entities.enums.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EntityDTOConvert {
  private final HmsDao dao;

  public RoleDTO toDTO(RoleEntity e) {
    var d = new RoleDTO();
    d.setRoleId(e.getRoleId());
    d.setType(e.getType());
    return d;
  }

  public RoleEntity toEntity(RoleDTO d) {
    if (d == null) throw new IllegalArgumentException("Request body is required");
    var e = new RoleEntity();
    e.setType(d.getType());
    return e;
  }

  public UserDTO toDTO(UserEntity e) {
    var d = new UserDTO();
    d.setUserId(e.getUserId());
    d.setUsername(e.getUsername());
    d.setEmail(e.getEmail());
    d.setPassword(null);
    d.setRoleId(e.getRole() == null ? null : e.getRole().getRoleId());
    return d;
  }

  public UserEntity toEntity(UserDTO d) {
    if (d == null) throw new IllegalArgumentException("Request body is required");
    var e = new UserEntity();
    e.setUsername(d.getUsername());
    e.setEmail(d.getEmail());
    e.setPassword(d.getPassword());
    e.setRole(d.getRoleId() == null ? null : dao.require(RoleEntity.class, d.getRoleId()));
    return e;
  }

  public CustomerDTO toDTO(CustomerEntity e) {
    var d = new CustomerDTO();
    d.setId(e.getId());
    d.setUserId(e.getUser() == null ? null : e.getUser().getUserId());
    d.setName(e.getName());
    d.setPhone(e.getPhone());
    d.setEmail(e.getEmail());
    d.setNic(e.getNic());
    d.setAge(e.getAge());
    d.setAddress(e.getAddress());
    d.setCountryCode(e.getCountryCode());
    d.setDateOfBirth(e.getDateOfBirth());
    d.setIdentityDocumentType(e.getIdentityDocumentType());
    d.setIdentityDocumentNumberEncrypted(null);
    d.setCreatedAt(e.getCreatedAt());
    d.setUpdatedAt(e.getUpdatedAt());
    return d;
  }

  public CustomerEntity toEntity(CustomerDTO d) {
    if (d == null) throw new IllegalArgumentException("Request body is required");
    var e = new CustomerEntity();
    e.setUser(d.getUserId() == null ? null : dao.require(UserEntity.class, d.getUserId()));
    e.setName(d.getName());
    e.setPhone(d.getPhone());
    e.setEmail(d.getEmail());
    e.setNic(d.getNic());
    e.setAge(d.getAge());
    e.setAddress(d.getAddress());
    e.setCountryCode(d.getCountryCode());
    e.setDateOfBirth(d.getDateOfBirth());
    e.setIdentityDocumentType(d.getIdentityDocumentType());
    e.setIdentityDocumentNumberEncrypted(d.getIdentityDocumentNumberEncrypted());
    return e;
  }

  public ReviewDTO toDTO(ReviewEntity e) {
    var d = new ReviewDTO();
    d.setId(e.getId());
    d.setReservationId(e.getReservation() == null ? null : e.getReservation().getId());
    d.setCustomerId(e.getCustomer() == null ? null : e.getCustomer().getId());
    d.setRating(e.getRating());
    d.setTitle(e.getTitle());
    d.setComment(e.getComment());
    d.setModerationStatus(e.getModerationStatus());
    d.setModeratedById(e.getModeratedBy() == null ? null : e.getModeratedBy().getUserId());
    d.setModeratedAt(e.getModeratedAt());
    d.setManagementReply(e.getManagementReply());
    d.setRepliedById(e.getRepliedBy() == null ? null : e.getRepliedBy().getUserId());
    d.setRepliedAt(e.getRepliedAt());
    d.setCreatedAt(e.getCreatedAt());
    d.setUpdatedAt(e.getUpdatedAt());
    return d;
  }

  public ReviewEntity toEntity(ReviewDTO d) {
    if (d == null) throw new IllegalArgumentException("Request body is required");
    var e = new ReviewEntity();
    e.setReservation(
        d.getReservationId() == null
            ? null
            : dao.require(ReservationEntity.class, d.getReservationId()));
    e.setCustomer(
        d.getCustomerId() == null ? null : dao.require(CustomerEntity.class, d.getCustomerId()));
    e.setRating(d.getRating());
    e.setTitle(d.getTitle());
    e.setComment(d.getComment());
    e.setModerationStatus(d.getModerationStatus());
    e.setModeratedBy(null);
    e.setModeratedAt(d.getModeratedAt());
    e.setManagementReply(d.getManagementReply());
    e.setRepliedBy(null);
    e.setRepliedAt(d.getRepliedAt());
    return e;
  }

  public PaymentDTO toDTO(PaymentEntity e) {
    var d = new PaymentDTO();
    d.setId(e.getId());
    d.setHotelId(e.getHotel() == null ? null : e.getHotel().getId());
    d.setReservationId(e.getReservation() == null ? null : e.getReservation().getId());
    d.setInvoiceId(e.getInvoice() == null ? null : e.getInvoice().getId());
    d.setReceiptNumber(e.getReceiptNumber());
    d.setAmount(e.getAmount());
    d.setCurrencyCode(e.getCurrencyCode());
    d.setPaymentMethod(e.getPaymentMethod());
    d.setReferenceNumber(e.getReferenceNumber());
    d.setPaidAt(e.getPaidAt());
    d.setStatus(e.getStatus());
    d.setReceivedById(e.getReceivedBy() == null ? null : e.getReceivedBy().getUserId());
    d.setNotes(e.getNotes());
    d.setCreatedAt(e.getCreatedAt());
    d.setUpdatedAt(e.getUpdatedAt());
    return d;
  }

  public PaymentEntity toEntity(PaymentDTO d) {
    if (d == null) throw new IllegalArgumentException("Request body is required");
    var e = new PaymentEntity();
    e.setHotel(d.getHotelId() == null ? null : dao.require(HotelEntity.class, d.getHotelId()));
    e.setReservation(
        d.getReservationId() == null
            ? null
            : dao.require(ReservationEntity.class, d.getReservationId()));
    e.setInvoice(
        d.getInvoiceId() == null ? null : dao.require(InvoiceEntity.class, d.getInvoiceId()));
    e.setReceiptNumber(d.getReceiptNumber());
    e.setAmount(d.getAmount());
    e.setCurrencyCode(d.getCurrencyCode());
    e.setPaymentMethod(d.getPaymentMethod());
    e.setReferenceNumber(d.getReferenceNumber());
    e.setPaidAt(d.getPaidAt());
    e.setStatus(d.getStatus());
    e.setReceivedBy(null);
    e.setNotes(d.getNotes());
    return e;
  }

  public RefundDTO toDTO(RefundEntity e) {
    var d = new RefundDTO();
    d.setId(e.getId());
    d.setPaymentId(e.getPayment() == null ? null : e.getPayment().getId());
    d.setRefundNumber(e.getRefundNumber());
    d.setAmount(e.getAmount());
    d.setRefundMethod(e.getRefundMethod());
    d.setReferenceNumber(e.getReferenceNumber());
    d.setReason(e.getReason());
    d.setStatus(e.getStatus());
    d.setRefundedAt(e.getRefundedAt());
    d.setProcessedById(e.getProcessedBy() == null ? null : e.getProcessedBy().getUserId());
    d.setCreatedAt(e.getCreatedAt());
    d.setUpdatedAt(e.getUpdatedAt());
    return d;
  }

  public RefundEntity toEntity(RefundDTO d) {
    if (d == null) throw new IllegalArgumentException("Request body is required");
    var e = new RefundEntity();
    e.setPayment(
        d.getPaymentId() == null ? null : dao.require(PaymentEntity.class, d.getPaymentId()));
    e.setRefundNumber(d.getRefundNumber());
    e.setAmount(d.getAmount());
    e.setRefundMethod(d.getRefundMethod());
    e.setReferenceNumber(d.getReferenceNumber());
    e.setReason(d.getReason());
    e.setStatus(d.getStatus());
    e.setRefundedAt(d.getRefundedAt());
    e.setProcessedBy(null);
    return e;
  }

  public InvoiceDTO toDTO(InvoiceEntity e) {
    var d = new InvoiceDTO();
    d.setId(e.getId());
    d.setHotelId(e.getHotel() == null ? null : e.getHotel().getId());
    d.setReservationId(e.getReservation() == null ? null : e.getReservation().getId());
    d.setInvoiceNumber(e.getInvoiceNumber());
    d.setBilledCustomerId(e.getBilledCustomer() == null ? null : e.getBilledCustomer().getId());
    d.setBillingNameSnapshot(e.getBillingNameSnapshot());
    d.setBillingAddressSnapshot(e.getBillingAddressSnapshot());
    d.setCurrencyCode(e.getCurrencyCode());
    d.setIssuedAt(e.getIssuedAt());
    d.setDueDate(e.getDueDate());
    d.setSubtotal(e.getSubtotal());
    d.setDiscountTotal(e.getDiscountTotal());
    d.setTaxTotal(e.getTaxTotal());
    d.setServiceChargeTotal(e.getServiceChargeTotal());
    d.setGrandTotal(e.getGrandTotal());
    d.setStatus(e.getStatus());
    d.setCreatedById(e.getCreatedBy() == null ? null : e.getCreatedBy().getUserId());
    d.setCreatedAt(e.getCreatedAt());
    d.setUpdatedAt(e.getUpdatedAt());
    return d;
  }

  public InvoiceEntity toEntity(InvoiceDTO d) {
    if (d == null) throw new IllegalArgumentException("Request body is required");
    var e = new InvoiceEntity();
    e.setHotel(d.getHotelId() == null ? null : dao.require(HotelEntity.class, d.getHotelId()));
    e.setReservation(
        d.getReservationId() == null
            ? null
            : dao.require(ReservationEntity.class, d.getReservationId()));
    e.setInvoiceNumber(d.getInvoiceNumber());
    e.setBilledCustomer(
        d.getBilledCustomerId() == null
            ? null
            : dao.require(CustomerEntity.class, d.getBilledCustomerId()));
    e.setBillingNameSnapshot(d.getBillingNameSnapshot());
    e.setBillingAddressSnapshot(d.getBillingAddressSnapshot());
    e.setCurrencyCode(d.getCurrencyCode());
    e.setIssuedAt(d.getIssuedAt());
    e.setDueDate(d.getDueDate());
    e.setSubtotal(d.getSubtotal());
    e.setDiscountTotal(d.getDiscountTotal());
    e.setTaxTotal(d.getTaxTotal());
    e.setServiceChargeTotal(d.getServiceChargeTotal());
    e.setGrandTotal(d.getGrandTotal());
    e.setStatus(d.getStatus());
    e.setCreatedBy(null);
    return e;
  }

  public InvoiceItemDTO toDTO(InvoiceItemEntity e) {
    var d = new InvoiceItemDTO();
    d.setId(e.getId());
    d.setInvoiceId(e.getInvoice() == null ? null : e.getInvoice().getId());
    d.setReservationRoomId(e.getReservationRoom() == null ? null : e.getReservationRoom().getId());
    d.setServiceChargeId(e.getServiceCharge() == null ? null : e.getServiceCharge().getId());
    d.setDescription(e.getDescription());
    d.setQuantity(e.getQuantity());
    d.setUnitPrice(e.getUnitPrice());
    d.setDiscountAmount(e.getDiscountAmount());
    d.setTaxRate(e.getTaxRate());
    d.setTaxAmount(e.getTaxAmount());
    d.setServiceChargeAmount(e.getServiceChargeAmount());
    d.setLineTotal(e.getLineTotal());
    d.setAccommodationId(e.getAccommodation() == null ? null : e.getAccommodation().getId());
    d.setCreatedAt(e.getCreatedAt());
    d.setUpdatedAt(e.getUpdatedAt());
    return d;
  }

  public InvoiceItemEntity toEntity(InvoiceItemDTO d) {
    if (d == null) throw new IllegalArgumentException("Request body is required");
    var e = new InvoiceItemEntity();
    e.setInvoice(
        d.getInvoiceId() == null ? null : dao.require(InvoiceEntity.class, d.getInvoiceId()));
    e.setReservationRoom(
        d.getReservationRoomId() == null
            ? null
            : dao.require(ReservationRoomEntity.class, d.getReservationRoomId()));
    e.setServiceCharge(
        d.getServiceChargeId() == null
            ? null
            : dao.require(ServiceChargeEntity.class, d.getServiceChargeId()));
    e.setDescription(d.getDescription());
    e.setQuantity(d.getQuantity());
    e.setUnitPrice(d.getUnitPrice());
    e.setDiscountAmount(d.getDiscountAmount());
    e.setTaxRate(d.getTaxRate());
    e.setTaxAmount(d.getTaxAmount());
    e.setServiceChargeAmount(d.getServiceChargeAmount());
    e.setLineTotal(d.getLineTotal());
    e.setAccommodation(
        d.getAccommodationId() == null
            ? null
            : dao.require(AccommodationEntity.class, d.getAccommodationId()));
    return e;
  }

  public ReservationDTO toDTO(ReservationEntity e) {
    var d = new ReservationDTO();
    d.setId(e.getId());
    d.setHotelId(e.getHotel() == null ? null : e.getHotel().getId());
    d.setBookingReference(e.getBookingReference());
    d.setBookerCustomerId(e.getBookerCustomer() == null ? null : e.getBookerCustomer().getId());
    d.setStatus(e.getStatus());
    d.setBookingSource(e.getBookingSource());
    d.setCurrencyCode(e.getCurrencyCode());
    d.setSpecialRequests(e.getSpecialRequests());
    d.setCreatedById(e.getCreatedBy() == null ? null : e.getCreatedBy().getUserId());
    d.setConfirmedAt(e.getConfirmedAt());
    d.setCancelledAt(e.getCancelledAt());
    d.setCancellationReason(e.getCancellationReason());
    d.setCreatedAt(e.getCreatedAt());
    d.setUpdatedAt(e.getUpdatedAt());
    return d;
  }

  public ReservationEntity toEntity(ReservationDTO d) {
    if (d == null) throw new IllegalArgumentException("Request body is required");
    var e = new ReservationEntity();
    e.setHotel(d.getHotelId() == null ? null : dao.require(HotelEntity.class, d.getHotelId()));
    e.setBookingReference(d.getBookingReference());
    e.setBookerCustomer(
        d.getBookerCustomerId() == null
            ? null
            : dao.require(CustomerEntity.class, d.getBookerCustomerId()));
    e.setStatus(d.getStatus());
    e.setBookingSource(d.getBookingSource());
    e.setCurrencyCode(d.getCurrencyCode());
    e.setSpecialRequests(d.getSpecialRequests());
    e.setCreatedBy(null);
    e.setConfirmedAt(d.getConfirmedAt());
    e.setCancelledAt(d.getCancelledAt());
    e.setCancellationReason(d.getCancellationReason());
    return e;
  }

  public HotelDTO toDTO(HotelEntity e) {
    var d = new HotelDTO();
    d.setId(e.getId());
    d.setName(e.getName());
    d.setAddress(e.getAddress());
    d.setCity(e.getCity());
    d.setCountryCode(e.getCountryCode());
    d.setPhone(e.getPhone());
    d.setEmail(e.getEmail());
    d.setTimezone(e.getTimezone());
    d.setCurrencyCode(e.getCurrencyCode());
    d.setCheckInTime(e.getCheckInTime());
    d.setCheckOutTime(e.getCheckOutTime());
    d.setIsActive(e.getIsActive());
    d.setCreatedAt(e.getCreatedAt());
    d.setUpdatedAt(e.getUpdatedAt());
    return d;
  }

  public HotelEntity toEntity(HotelDTO d) {
    if (d == null) throw new IllegalArgumentException("Request body is required");
    var e = new HotelEntity();
    e.setName(d.getName());
    e.setAddress(d.getAddress());
    e.setCity(d.getCity());
    e.setCountryCode(d.getCountryCode());
    e.setPhone(d.getPhone());
    e.setEmail(d.getEmail());
    e.setTimezone(d.getTimezone());
    e.setCurrencyCode(d.getCurrencyCode());
    e.setCheckInTime(d.getCheckInTime());
    e.setCheckOutTime(d.getCheckOutTime());
    e.setIsActive(d.getIsActive());
    return e;
  }

  public RoomTypeDTO toDTO(RoomTypeEntity e) {
    var d = new RoomTypeDTO();
    d.setId(e.getId());
    d.setHotelId(e.getHotel() == null ? null : e.getHotel().getId());
    d.setName(e.getName());
    d.setDescription(e.getDescription());
    d.setMaxAdults(e.getMaxAdults());
    d.setMaxChildren(e.getMaxChildren());
    d.setMaxOccupancy(e.getMaxOccupancy());
    d.setBedDescription(e.getBedDescription());
    d.setIsActive(e.getIsActive());
    d.setCreatedAt(e.getCreatedAt());
    d.setUpdatedAt(e.getUpdatedAt());
    return d;
  }

  public RoomTypeEntity toEntity(RoomTypeDTO d) {
    if (d == null) throw new IllegalArgumentException("Request body is required");
    var e = new RoomTypeEntity();
    e.setHotel(d.getHotelId() == null ? null : dao.require(HotelEntity.class, d.getHotelId()));
    e.setName(d.getName());
    e.setDescription(d.getDescription());
    e.setMaxAdults(d.getMaxAdults());
    e.setMaxChildren(d.getMaxChildren());
    e.setMaxOccupancy(d.getMaxOccupancy());
    e.setBedDescription(d.getBedDescription());
    e.setIsActive(d.getIsActive());
    return e;
  }

  public RoomDTO toDTO(RoomEntity e) {
    var d = new RoomDTO();
    d.setId(e.getId());
    d.setHotelId(e.getHotel() == null ? null : e.getHotel().getId());
    d.setRoomTypeId(e.getRoomType() == null ? null : e.getRoomType().getId());
    d.setRoomNumber(e.getRoomNumber());
    d.setFloor(e.getFloor());
    d.setOperationalStatus(e.getOperationalStatus());
    d.setHousekeepingStatus(e.getHousekeepingStatus());
    d.setNotes(e.getNotes());
    d.setStatusId(e.getRoomStatus() == null ? null : e.getRoomStatus().getStatusId());
    d.setCreatedAt(e.getCreatedAt());
    d.setUpdatedAt(e.getUpdatedAt());
    return d;
  }

  public RoomEntity toEntity(RoomDTO d) {
    if (d == null) throw new IllegalArgumentException("Request body is required");
    var e = new RoomEntity();
    e.setHotel(d.getHotelId() == null ? null : dao.require(HotelEntity.class, d.getHotelId()));
    e.setRoomType(
        d.getRoomTypeId() == null ? null : dao.require(RoomTypeEntity.class, d.getRoomTypeId()));
    e.setRoomNumber(d.getRoomNumber());
    e.setFloor(d.getFloor());
    e.setOperationalStatus(d.getOperationalStatus());
    e.setHousekeepingStatus(d.getHousekeepingStatus());
    e.setNotes(d.getNotes());
    e.setRoomStatus(
        d.getStatusId() == null ? null : dao.require(RoomStatusEntity.class, d.getStatusId()));
    return e;
  }

  public RoomRateDTO toDTO(RoomRateEntity e) {
    var d = new RoomRateDTO();
    d.setId(e.getId());
    d.setRoomTypeId(e.getRoomType() == null ? null : e.getRoomType().getId());
    d.setStayDate(e.getStayDate());
    d.setNightlyRate(e.getNightlyRate());
    d.setExtraAdultRate(e.getExtraAdultRate());
    d.setExtraChildRate(e.getExtraChildRate());
    d.setMinimumNights(e.getMinimumNights());
    d.setStopSell(e.getStopSell());
    d.setCreatedAt(e.getCreatedAt());
    d.setUpdatedAt(e.getUpdatedAt());
    return d;
  }

  public RoomRateEntity toEntity(RoomRateDTO d) {
    if (d == null) throw new IllegalArgumentException("Request body is required");
    var e = new RoomRateEntity();
    e.setRoomType(
        d.getRoomTypeId() == null ? null : dao.require(RoomTypeEntity.class, d.getRoomTypeId()));
    e.setStayDate(d.getStayDate());
    e.setNightlyRate(d.getNightlyRate());
    e.setExtraAdultRate(d.getExtraAdultRate());
    e.setExtraChildRate(d.getExtraChildRate());
    e.setMinimumNights(d.getMinimumNights());
    e.setStopSell(d.getStopSell());
    return e;
  }

  public HotelServiceDTO toDTO(HotelServiceEntity e) {
    var d = new HotelServiceDTO();
    d.setId(e.getId());
    d.setHotelId(e.getHotel() == null ? null : e.getHotel().getId());
    d.setName(e.getName());
    d.setDescription(e.getDescription());
    d.setUnit(e.getUnit());
    d.setDefaultPrice(e.getDefaultPrice());
    d.setDefaultTaxRate(e.getDefaultTaxRate());
    d.setIsActive(e.getIsActive());
    d.setCreatedAt(e.getCreatedAt());
    d.setUpdatedAt(e.getUpdatedAt());
    return d;
  }

  public HotelServiceEntity toEntity(HotelServiceDTO d) {
    if (d == null) throw new IllegalArgumentException("Request body is required");
    var e = new HotelServiceEntity();
    e.setHotel(d.getHotelId() == null ? null : dao.require(HotelEntity.class, d.getHotelId()));
    e.setName(d.getName());
    e.setDescription(d.getDescription());
    e.setUnit(d.getUnit());
    e.setDefaultPrice(d.getDefaultPrice());
    e.setDefaultTaxRate(d.getDefaultTaxRate());
    e.setIsActive(d.getIsActive());
    return e;
  }

  public ServiceChargeDTO toDTO(ServiceChargeEntity e) {
    var d = new ServiceChargeDTO();
    d.setId(e.getId());
    d.setReservationId(e.getReservation() == null ? null : e.getReservation().getId());
    d.setReservationRoomId(e.getReservationRoom() == null ? null : e.getReservationRoom().getId());
    d.setServiceId(e.getService() == null ? null : e.getService().getId());
    d.setDescriptionSnapshot(e.getDescriptionSnapshot());
    d.setQuantity(e.getQuantity());
    d.setUnitPrice(e.getUnitPrice());
    d.setDiscountAmount(e.getDiscountAmount());
    d.setTaxAmount(e.getTaxAmount());
    d.setTotalAmount(e.getTotalAmount());
    d.setProvidedAt(e.getProvidedAt());
    d.setStatus(e.getStatus());
    d.setRecordedById(e.getRecordedBy() == null ? null : e.getRecordedBy().getUserId());
    d.setCreatedAt(e.getCreatedAt());
    d.setUpdatedAt(e.getUpdatedAt());
    return d;
  }

  public ServiceChargeEntity toEntity(ServiceChargeDTO d) {
    if (d == null) throw new IllegalArgumentException("Request body is required");
    var e = new ServiceChargeEntity();
    e.setReservation(
        d.getReservationId() == null
            ? null
            : dao.require(ReservationEntity.class, d.getReservationId()));
    e.setReservationRoom(
        d.getReservationRoomId() == null
            ? null
            : dao.require(ReservationRoomEntity.class, d.getReservationRoomId()));
    e.setService(
        d.getServiceId() == null ? null : dao.require(HotelServiceEntity.class, d.getServiceId()));
    e.setDescriptionSnapshot(d.getDescriptionSnapshot());
    e.setQuantity(d.getQuantity());
    e.setUnitPrice(d.getUnitPrice());
    e.setDiscountAmount(d.getDiscountAmount());
    e.setTaxAmount(d.getTaxAmount());
    e.setTotalAmount(d.getTotalAmount());
    e.setProvidedAt(d.getProvidedAt());
    e.setStatus(d.getStatus());
    e.setRecordedBy(null);
    return e;
  }

  public BookingStatusHistoryDTO toDTO(BookingStatusHistoryEntity e) {
    var d = new BookingStatusHistoryDTO();
    d.setId(e.getId());
    d.setReservationId(e.getReservation() == null ? null : e.getReservation().getId());
    d.setReservationRoomId(e.getReservationRoom() == null ? null : e.getReservationRoom().getId());
    d.setOldStatus(e.getOldStatus());
    d.setNewStatus(e.getNewStatus());
    d.setReason(e.getReason());
    d.setChangedById(e.getChangedBy() == null ? null : e.getChangedBy().getUserId());
    d.setChangedAt(e.getChangedAt());
    return d;
  }

  public BookingStatusHistoryEntity toEntity(BookingStatusHistoryDTO d) {
    if (d == null) throw new IllegalArgumentException("Request body is required");
    var e = new BookingStatusHistoryEntity();
    e.setReservation(
        d.getReservationId() == null
            ? null
            : dao.require(ReservationEntity.class, d.getReservationId()));
    e.setReservationRoom(
        d.getReservationRoomId() == null
            ? null
            : dao.require(ReservationRoomEntity.class, d.getReservationRoomId()));
    e.setOldStatus(d.getOldStatus());
    e.setNewStatus(d.getNewStatus());
    e.setReason(d.getReason());
    e.setChangedBy(null);
    e.setChangedAt(d.getChangedAt());
    return e;
  }

  public AccommodationDTO toDTO(AccommodationEntity e) {
    var d = new AccommodationDTO();
    d.setId(e.getId());
    d.setHotelId(e.getHotel() == null ? null : e.getHotel().getId());
    d.setType(e.getType() == null ? null : e.getType().getValue());
    d.setPrice(e.getPrice());
    return d;
  }

  public AccommodationEntity toEntity(AccommodationDTO d) {
    if (d == null) throw new IllegalArgumentException("Request body is required");
    var e = new AccommodationEntity();
    e.setHotel(d.getHotelId() == null ? null : dao.require(HotelEntity.class, d.getHotelId()));
    e.setType(d.getType() == null ? null : AccommodationType.fromValue(d.getType()));
    e.setPrice(d.getPrice());
    return e;
  }

  public RoomStatusDTO toDTO(RoomStatusEntity e) {
    var d = new RoomStatusDTO();
    d.setStatusId(e.getStatusId());
    d.setStatus(e.getStatus() == null ? null : e.getStatus().getValue());
    return d;
  }

  public RoomStatusEntity toEntity(RoomStatusDTO d) {
    if (d == null) throw new IllegalArgumentException("Request body is required");
    var e = new RoomStatusEntity();
    e.setStatus(d.getStatus() == null ? null : RoomStatusValue.fromValue(d.getStatus()));
    return e;
  }

  public ReservationRoomDTO toDTO(ReservationRoomEntity e) {
    var d = new ReservationRoomDTO();
    d.setId(e.getId());
    d.setReservationId(e.getReservation() == null ? null : e.getReservation().getId());
    d.setRoomTypeId(e.getRoomType() == null ? null : e.getRoomType().getId());
    d.setRoomId(e.getRoom() == null ? null : e.getRoom().getId());
    d.setCheckInDate(e.getCheckInDate());
    d.setCheckOutDate(e.getCheckOutDate());
    d.setAdults(e.getAdults());
    d.setChildren(e.getChildren());
    d.setNightlyRate(e.getNightlyRate());
    d.setExtraPersonTotal(e.getExtraPersonTotal());
    d.setDiscountAmount(e.getDiscountAmount());
    d.setTaxAmount(e.getTaxAmount());
    d.setServiceChargeAmount(e.getServiceChargeAmount());
    d.setTotalAmount(e.getTotalAmount());
    d.setCancellationPolicySnapshot(e.getCancellationPolicySnapshot());
    d.setStatus(e.getStatus());
    d.setActualCheckInAt(e.getActualCheckInAt());
    d.setActualCheckOutAt(e.getActualCheckOutAt());
    d.setAccommodationId(e.getAccommodation() == null ? null : e.getAccommodation().getId());
    d.setCreatedAt(e.getCreatedAt());
    d.setUpdatedAt(e.getUpdatedAt());
    return d;
  }

  public ReservationRoomEntity toEntity(ReservationRoomDTO d) {
    if (d == null) throw new IllegalArgumentException("Request body is required");
    var e = new ReservationRoomEntity();
    e.setReservation(
        d.getReservationId() == null
            ? null
            : dao.require(ReservationEntity.class, d.getReservationId()));
    e.setRoomType(
        d.getRoomTypeId() == null ? null : dao.require(RoomTypeEntity.class, d.getRoomTypeId()));
    e.setRoom(d.getRoomId() == null ? null : dao.require(RoomEntity.class, d.getRoomId()));
    e.setCheckInDate(d.getCheckInDate());
    e.setCheckOutDate(d.getCheckOutDate());
    e.setAdults(d.getAdults());
    e.setChildren(d.getChildren());
    e.setNightlyRate(d.getNightlyRate());
    e.setExtraPersonTotal(d.getExtraPersonTotal());
    e.setDiscountAmount(d.getDiscountAmount());
    e.setTaxAmount(d.getTaxAmount());
    e.setServiceChargeAmount(d.getServiceChargeAmount());
    e.setTotalAmount(d.getTotalAmount());
    e.setCancellationPolicySnapshot(d.getCancellationPolicySnapshot());
    e.setStatus(d.getStatus());
    e.setActualCheckInAt(d.getActualCheckInAt());
    e.setActualCheckOutAt(d.getActualCheckOutAt());
    e.setAccommodation(
        d.getAccommodationId() == null
            ? null
            : dao.require(AccommodationEntity.class, d.getAccommodationId()));
    return e;
  }

  public ReservationCustomerDTO toDTO(ReservationCustomerEntity e) {
    var d = new ReservationCustomerDTO();
    d.setId(e.getId());
    d.setReservationRoomId(e.getReservationRoom() == null ? null : e.getReservationRoom().getId());
    d.setCustomerId(e.getCustomer() == null ? null : e.getCustomer().getId());
    d.setIsPrimaryCustomer(e.getIsPrimaryCustomer());
    d.setCreatedAt(e.getCreatedAt());
    d.setUpdatedAt(e.getUpdatedAt());
    return d;
  }

  public ReservationCustomerEntity toEntity(ReservationCustomerDTO d) {
    if (d == null) throw new IllegalArgumentException("Request body is required");
    var e = new ReservationCustomerEntity();
    e.setReservationRoom(
        d.getReservationRoomId() == null
            ? null
            : dao.require(ReservationRoomEntity.class, d.getReservationRoomId()));
    e.setCustomer(
        d.getCustomerId() == null ? null : dao.require(CustomerEntity.class, d.getCustomerId()));
    e.setIsPrimaryCustomer(d.getIsPrimaryCustomer());
    return e;
  }

  public AuditLogDTO toDTO(AuditLogEntity e) {
    var d = new AuditLogDTO();
    d.setId(e.getId());
    d.setHotelId(e.getHotel() == null ? null : e.getHotel().getId());
    d.setActorUserId(e.getActorUser() == null ? null : e.getActorUser().getUserId());
    d.setAction(e.getAction());
    d.setEntityType(e.getEntityType());
    d.setEntityId(e.getEntityId());
    d.setOldValues(e.getOldValues());
    d.setNewValues(e.getNewValues());
    d.setRequestId(e.getRequestId());
    d.setOccurredAt(e.getOccurredAt());
    return d;
  }

  public AuditLogEntity toEntity(AuditLogDTO d) {
    if (d == null) throw new IllegalArgumentException("Request body is required");
    var e = new AuditLogEntity();
    e.setHotel(d.getHotelId() == null ? null : dao.require(HotelEntity.class, d.getHotelId()));
    e.setActorUser(null);
    e.setAction(d.getAction());
    e.setEntityType(d.getEntityType());
    e.setEntityId(d.getEntityId());
    e.setOldValues(d.getOldValues());
    e.setNewValues(d.getNewValues());
    e.setRequestId(d.getRequestId());
    e.setOccurredAt(d.getOccurredAt());
    return e;
  }
}
