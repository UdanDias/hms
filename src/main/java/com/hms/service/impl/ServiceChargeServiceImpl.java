package com.hms.service.impl;

import static com.hms.util.MoneyUtil.money;

import com.hms.dao.HotelDAO;
import com.hms.dao.HotelServiceDAO;
import com.hms.dao.InvoiceItemDAO;
import com.hms.dao.ReservationDAO;
import com.hms.dao.ReservationRoomDAO;
import com.hms.dao.ServiceChargeDAO;
import com.hms.dto.ServiceChargeDTO;
import com.hms.entities.HotelServiceEntity;
import com.hms.entities.ReservationEntity;
import com.hms.entities.ReservationRoomEntity;
import com.hms.entities.ServiceChargeEntity;
import com.hms.exception.HotelServiceNotFoundException;
import com.hms.exception.ReservationNotFoundException;
import com.hms.exception.ReservationRoomNotFoundException;
import com.hms.exception.ServiceChargeNotFoundException;
import com.hms.security.ServiceSecurity;
import com.hms.service.ServiceChargeService;
import com.hms.util.AuditRecorder;
import com.hms.util.EntityDTOConvert;
import com.hms.util.MoneyUtil;
import com.hms.util.UtilData;
import com.hms.util.ValidationUtil;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class ServiceChargeServiceImpl implements ServiceChargeService {
  private final InvoiceItemDAO invoiceItemDao;
  private final ReservationDAO reservationDao;
  private final HotelDAO hotelDao;
  private final HotelServiceDAO hotelServiceDao;
  private final ServiceChargeDAO serviceChargeDao;
  private final ReservationRoomDAO reservationRoomDao;
  private final EntityDTOConvert entityDTOConvert;
  private final ServiceSecurity serviceSecurity;
  private final ValidationUtil validationUtil;
  private final AuditRecorder auditRecorder;

  @Override
  @Transactional(readOnly = true)
  public ServiceChargeDTO getSelectedServiceCharge(Long serviceChargeId) {
    serviceSecurity.getCurrentUser();
    ServiceChargeEntity serviceChargeEntity =
        serviceChargeDao
            .findById(serviceChargeId)
            .orElseThrow(() -> new ServiceChargeNotFoundException("ServiceCharge not found"));
    serviceSecurity.checkRead(serviceChargeEntity);
    return entityDTOConvert.convertServiceChargeEntityToServiceChargeDTO(serviceChargeEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ServiceChargeDTO> getAllServiceCharges() {
    serviceSecurity.getCurrentUser();
    List<ServiceChargeEntity> entities =
        serviceChargeDao.findAll().stream().filter(serviceSecurity::canRead).toList();
    return entityDTOConvert.convertServiceChargeEntityListToServiceChargeDTOList(entities);
  }

  @Override
  public ServiceChargeDTO addServiceCharge(ServiceChargeDTO serviceChargeDTO) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "RECEPTIONIST");
    hotelDao.lockAllForWrite();
    ServiceChargeEntity serviceChargeEntity =
        entityDTOConvert.convertServiceChargeDTOToServiceChargeEntity(serviceChargeDTO);
    if (serviceChargeDTO.getReservationId() != null) {
      ReservationEntity reservation =
          reservationDao
              .findById(serviceChargeDTO.getReservationId())
              .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));
      serviceChargeEntity.setReservation(reservation);
    }
    if (serviceChargeDTO.getReservationRoomId() != null) {
      ReservationRoomEntity reservationRoom =
          reservationRoomDao
              .findById(serviceChargeDTO.getReservationRoomId())
              .orElseThrow(() -> new ReservationRoomNotFoundException("ReservationRoom not found"));
      serviceChargeEntity.setReservationRoom(reservationRoom);
    }
    if (serviceChargeDTO.getServiceId() != null) {
      HotelServiceEntity service =
          hotelServiceDao
              .findById(serviceChargeDTO.getServiceId())
              .orElseThrow(() -> new HotelServiceNotFoundException("HotelService not found"));
      serviceChargeEntity.setService(service);
    }

    validateServiceChargeForSave(serviceChargeEntity, null);
    validationUtil.validate(serviceChargeEntity);
    serviceChargeDao.save(serviceChargeEntity);
    auditRecorder.recordAction("CREATE", "ServiceCharge", serviceChargeEntity.getId());
    serviceChargeDao.flush();
    return entityDTOConvert.convertServiceChargeEntityToServiceChargeDTO(serviceChargeEntity);
  }

  @Override
  public ServiceChargeDTO updateServiceCharge(
      Long serviceChargeId, ServiceChargeDTO serviceChargeDTO) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "RECEPTIONIST");
    hotelDao.lockAllForWrite();
    ServiceChargeEntity serviceChargeEntity =
        serviceChargeDao
            .findById(serviceChargeId)
            .orElseThrow(() -> new ServiceChargeNotFoundException("ServiceCharge not found"));
    ServiceChargeEntity requestedServiceCharge =
        entityDTOConvert.convertServiceChargeDTOToServiceChargeEntity(serviceChargeDTO);
    if (serviceChargeDTO.getReservationId() != null) {
      ReservationEntity reservation =
          reservationDao
              .findById(serviceChargeDTO.getReservationId())
              .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));
      requestedServiceCharge.setReservation(reservation);
    }
    if (serviceChargeDTO.getReservationRoomId() != null) {
      ReservationRoomEntity reservationRoom =
          reservationRoomDao
              .findById(serviceChargeDTO.getReservationRoomId())
              .orElseThrow(() -> new ReservationRoomNotFoundException("ReservationRoom not found"));
      requestedServiceCharge.setReservationRoom(reservationRoom);
    }
    if (serviceChargeDTO.getServiceId() != null) {
      HotelServiceEntity service =
          hotelServiceDao
              .findById(serviceChargeDTO.getServiceId())
              .orElseThrow(() -> new HotelServiceNotFoundException("HotelService not found"));
      requestedServiceCharge.setService(service);
    }

    validateServiceChargeForSave(requestedServiceCharge, serviceChargeEntity);
    validationUtil.validate(requestedServiceCharge);
    serviceChargeEntity.setReservation(requestedServiceCharge.getReservation());
    serviceChargeEntity.setReservationRoom(requestedServiceCharge.getReservationRoom());
    serviceChargeEntity.setService(requestedServiceCharge.getService());
    serviceChargeEntity.setDescriptionSnapshot(requestedServiceCharge.getDescriptionSnapshot());
    serviceChargeEntity.setQuantity(requestedServiceCharge.getQuantity());
    serviceChargeEntity.setUnitPrice(requestedServiceCharge.getUnitPrice());
    serviceChargeEntity.setDiscountAmount(requestedServiceCharge.getDiscountAmount());
    serviceChargeEntity.setTaxAmount(requestedServiceCharge.getTaxAmount());
    serviceChargeEntity.setTotalAmount(requestedServiceCharge.getTotalAmount());
    serviceChargeEntity.setProvidedAt(requestedServiceCharge.getProvidedAt());
    serviceChargeEntity.setStatus(requestedServiceCharge.getStatus());
    serviceChargeEntity.setRecordedBy(requestedServiceCharge.getRecordedBy());
    validationUtil.validate(serviceChargeEntity);
    serviceChargeDao.save(serviceChargeEntity);
    auditRecorder.recordAction("UPDATE", "ServiceCharge", serviceChargeEntity.getId());
    serviceChargeDao.flush();
    return entityDTOConvert.convertServiceChargeEntityToServiceChargeDTO(serviceChargeEntity);
  }

  private void validateServiceChargeForSave(
      ServiceChargeEntity serviceChargeEntity, ServiceChargeEntity existingServiceCharge) {
    validationUtil.require(
        serviceChargeEntity.getReservation() != null && serviceChargeEntity.getService() != null,
        "Reservation and service are required");
    validationUtil.same(
        serviceChargeEntity.getReservation().getHotel().getId(),
        serviceChargeEntity.getService().getHotel().getId(),
        "Service");
    validationUtil.require(
        Set.of("confirmed", "checked_in")
            .contains(serviceChargeEntity.getReservation().getStatus()),
        "Service charges require a confirmed or checked-in reservation");
    validationUtil.require(
        Boolean.TRUE.equals(serviceChargeEntity.getService().getIsActive()), "Service is inactive");
    if (serviceChargeEntity.getReservationRoom() != null)
      validationUtil.same(
          serviceChargeEntity.getReservationRoom().getReservation().getId(),
          serviceChargeEntity.getReservation().getId(),
          "Room reservation");
    if (existingServiceCharge != null) {
      requireUnbilledServiceCharge(existingServiceCharge);
      validationUtil.require(
          "active".equals(existingServiceCharge.getStatus()), "Voided charges cannot be edited");
      validationUtil.same(
          existingServiceCharge.getReservation().getId(),
          serviceChargeEntity.getReservation().getId(),
          "Reservation");
    }
    serviceChargeEntity.setQuantity(validateQuantity(serviceChargeEntity.getQuantity()));
    serviceChargeEntity.setUnitPrice(
        MoneyUtil.nonnegative(
            serviceChargeEntity.getUnitPrice() == null
                ? serviceChargeEntity.getService().getDefaultPrice()
                : serviceChargeEntity.getUnitPrice()));
    serviceChargeEntity.setDiscountAmount(
        MoneyUtil.nonnegative(serviceChargeEntity.getDiscountAmount()));
    var base =
        money(serviceChargeEntity.getQuantity().multiply(serviceChargeEntity.getUnitPrice()));
    validationUtil.require(
        serviceChargeEntity.getDiscountAmount().compareTo(base) <= 0,
        "Discount exceeds service amount");
    serviceChargeEntity.setTaxAmount(
        money(
            base.subtract(serviceChargeEntity.getDiscountAmount())
                .multiply(validateTaxRate(serviceChargeEntity.getService().getDefaultTaxRate()))
                .divide(BigDecimal.valueOf(100))));
    serviceChargeEntity.setTotalAmount(
        money(
            base.subtract(serviceChargeEntity.getDiscountAmount())
                .add(serviceChargeEntity.getTaxAmount())));
    serviceChargeEntity.setDescriptionSnapshot(serviceChargeEntity.getService().getName());
    serviceChargeEntity.setStatus("active");
    serviceChargeEntity.setProvidedAt(
        existingServiceCharge == null
            ? UtilData.generateNow()
            : existingServiceCharge.getProvidedAt());
    serviceChargeEntity.setRecordedBy(
        existingServiceCharge == null
            ? serviceSecurity.getCurrentUser()
            : existingServiceCharge.getRecordedBy());
  }

  @Override
  public ServiceChargeDTO voidServiceCharge(Long serviceChargeId) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "RECEPTIONIST");
    hotelDao.lockAllForWrite();
    ServiceChargeEntity serviceChargeEntity =
        serviceChargeDao
            .findById(serviceChargeId)
            .orElseThrow(() -> new ServiceChargeNotFoundException("ServiceCharge not found"));
    requireUnbilledServiceCharge(serviceChargeEntity);
    validationUtil.require(
        !"checked_out".equals(serviceChargeEntity.getReservation().getStatus()),
        "Cannot change charges after checkout");
    validationUtil.require(
        "active".equals(serviceChargeEntity.getStatus()), "Charge is already void");
    serviceChargeEntity.setStatus("void");
    validationUtil.validate(serviceChargeEntity);
    auditRecorder.recordAction("VOID", "ServiceCharge", serviceChargeEntity.getId());
    serviceChargeDao.flush();
    return entityDTOConvert.convertServiceChargeEntityToServiceChargeDTO(serviceChargeEntity);
  }

  private BigDecimal validateQuantity(BigDecimal value) {
    validationUtil.require(
        value != null && value.signum() > 0 && value.scale() <= 3,
        "Quantity must be positive with at most 3 decimal places");
    return value;
  }

  private BigDecimal validateTaxRate(BigDecimal value) {
    var x = value == null ? BigDecimal.ZERO : value;
    validationUtil.require(
        x.signum() >= 0 && x.compareTo(BigDecimal.valueOf(100)) <= 0,
        "Tax percentage must be between 0 and 100");
    return x;
  }

  private void requireUnbilledServiceCharge(ServiceChargeEntity charge) {
    validationUtil.require(
        invoiceItemDao.findNonVoidByServiceChargeId(charge.getId()).isEmpty(),
        "Service charge is already billed");
  }
}
