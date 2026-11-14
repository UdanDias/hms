package com.hms.service.impl;

import com.hms.dao.CustomerDAO;
import com.hms.dao.HotelDAO;
import com.hms.dao.ReservationCustomerDAO;
import com.hms.dao.ReservationRoomDAO;
import com.hms.dto.ReservationCustomerDTO;
import com.hms.entities.CustomerEntity;
import com.hms.entities.ReservationCustomerEntity;
import com.hms.entities.ReservationRoomEntity;
import com.hms.exception.CustomerNotFoundException;
import com.hms.exception.ReservationCustomerNotFoundException;
import com.hms.exception.ReservationRoomNotFoundException;
import com.hms.security.ServiceSecurity;
import com.hms.service.ReservationCustomerService;
import com.hms.util.AuditRecorder;
import com.hms.util.EntityDTOConvert;
import com.hms.util.ValidationUtil;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class ReservationCustomerServiceImpl implements ReservationCustomerService {
  private final CustomerDAO customerDao;
  private final HotelDAO hotelDao;
  private final ReservationRoomDAO reservationRoomDao;
  private final ReservationCustomerDAO reservationCustomerDao;
  private final EntityDTOConvert entityDTOConvert;
  private final ServiceSecurity serviceSecurity;
  private final ValidationUtil validationUtil;
  private final AuditRecorder auditRecorder;

  @Override
  @Transactional(readOnly = true)
  public ReservationCustomerDTO getSelectedReservationCustomer(Long reservationCustomerId) {
    serviceSecurity.getCurrentUser();
    ReservationCustomerEntity reservationCustomerEntity =
        reservationCustomerDao
            .findById(reservationCustomerId)
            .orElseThrow(
                () -> new ReservationCustomerNotFoundException("ReservationCustomer not found"));
    serviceSecurity.checkRead(reservationCustomerEntity);
    return entityDTOConvert.convertReservationCustomerEntityToReservationCustomerDTO(
        reservationCustomerEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReservationCustomerDTO> getAllReservationCustomers() {
    serviceSecurity.getCurrentUser();
    List<ReservationCustomerEntity> entities =
        reservationCustomerDao.findAll().stream().filter(serviceSecurity::canRead).toList();
    return entityDTOConvert.convertReservationCustomerEntityListToReservationCustomerDTOList(
        entities);
  }

  @Override
  public ReservationCustomerDTO addReservationCustomer(
      ReservationCustomerDTO reservationCustomerDTO) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "RECEPTIONIST");
    hotelDao.lockAllForWrite();
    ReservationCustomerEntity reservationCustomerEntity =
        entityDTOConvert.convertReservationCustomerDTOToReservationCustomerEntity(
            reservationCustomerDTO);
    if (reservationCustomerDTO.getReservationRoomId() != null) {
      ReservationRoomEntity reservationRoom =
          reservationRoomDao
              .findById(reservationCustomerDTO.getReservationRoomId())
              .orElseThrow(() -> new ReservationRoomNotFoundException("ReservationRoom not found"));
      reservationCustomerEntity.setReservationRoom(reservationRoom);
    }
    if (reservationCustomerDTO.getCustomerId() != null) {
      CustomerEntity customer =
          customerDao
              .findById(reservationCustomerDTO.getCustomerId())
              .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
      reservationCustomerEntity.setCustomer(customer);
    }

    validateReservationCustomerForSave(reservationCustomerEntity, null);
    validationUtil.validate(reservationCustomerEntity);
    reservationCustomerDao.save(reservationCustomerEntity);
    auditRecorder.recordAction("CREATE", "ReservationCustomer", reservationCustomerEntity.getId());
    reservationCustomerDao.flush();
    return entityDTOConvert.convertReservationCustomerEntityToReservationCustomerDTO(
        reservationCustomerEntity);
  }

  @Override
  public ReservationCustomerDTO updateReservationCustomer(
      Long reservationCustomerId, ReservationCustomerDTO reservationCustomerDTO) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "RECEPTIONIST");
    hotelDao.lockAllForWrite();
    ReservationCustomerEntity reservationCustomerEntity =
        reservationCustomerDao
            .findById(reservationCustomerId)
            .orElseThrow(
                () -> new ReservationCustomerNotFoundException("ReservationCustomer not found"));
    ReservationCustomerEntity requestedReservationCustomer =
        entityDTOConvert.convertReservationCustomerDTOToReservationCustomerEntity(
            reservationCustomerDTO);
    if (reservationCustomerDTO.getReservationRoomId() != null) {
      ReservationRoomEntity reservationRoom =
          reservationRoomDao
              .findById(reservationCustomerDTO.getReservationRoomId())
              .orElseThrow(() -> new ReservationRoomNotFoundException("ReservationRoom not found"));
      requestedReservationCustomer.setReservationRoom(reservationRoom);
    }
    if (reservationCustomerDTO.getCustomerId() != null) {
      CustomerEntity customer =
          customerDao
              .findById(reservationCustomerDTO.getCustomerId())
              .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
      requestedReservationCustomer.setCustomer(customer);
    }

    validateReservationCustomerForSave(requestedReservationCustomer, reservationCustomerEntity);
    validationUtil.validate(requestedReservationCustomer);
    reservationCustomerEntity.setReservationRoom(requestedReservationCustomer.getReservationRoom());
    reservationCustomerEntity.setCustomer(requestedReservationCustomer.getCustomer());
    reservationCustomerEntity.setIsPrimaryCustomer(
        requestedReservationCustomer.getIsPrimaryCustomer());
    validationUtil.validate(reservationCustomerEntity);
    reservationCustomerDao.save(reservationCustomerEntity);
    auditRecorder.recordAction("UPDATE", "ReservationCustomer", reservationCustomerEntity.getId());
    reservationCustomerDao.flush();
    return entityDTOConvert.convertReservationCustomerEntityToReservationCustomerDTO(
        reservationCustomerEntity);
  }

  @Override
  public void deleteReservationCustomer(Long reservationCustomerId) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "RECEPTIONIST");
    hotelDao.lockAllForWrite();
    ReservationCustomerEntity reservationCustomerEntity =
        reservationCustomerDao
            .findById(reservationCustomerId)
            .orElseThrow(
                () -> new ReservationCustomerNotFoundException("ReservationCustomer not found"));
    validationUtil.require(
        Set.of("pending", "confirmed")
            .contains(reservationCustomerEntity.getReservationRoom().getReservation().getStatus()),
        "Guest list is locked after check-in");
    auditRecorder.recordAction("DELETE", "ReservationCustomer", reservationCustomerId);
    reservationCustomerDao.delete(reservationCustomerEntity);
    reservationCustomerDao.flush();
  }

  private void validateReservationCustomerForSave(
      ReservationCustomerEntity reservationCustomerEntity,
      ReservationCustomerEntity existingReservationCustomer) {
    if (reservationCustomerEntity.getReservationRoom() != null)
      validationUtil.require(
          Set.of("pending", "confirmed")
              .contains(reservationCustomerEntity.getReservationRoom().getStatus()),
          "Cannot add guests to an inactive room reservation");
    validationUtil.require(
        reservationCustomerEntity.getReservationRoom() != null
            && reservationCustomerEntity.getCustomer() != null,
        "Room reservation and customer are required");
    validationUtil.require(
        Set.of("pending", "confirmed")
            .contains(reservationCustomerEntity.getReservationRoom().getReservation().getStatus()),
        "Guest list is locked after check-in");
    if (existingReservationCustomer != null)
      validationUtil.same(
          existingReservationCustomer.getReservationRoom().getId(),
          reservationCustomerEntity.getReservationRoom().getId(),
          "Room reservation");
    var others =
        reservationCustomerDao
            .findByReservationRoomId(reservationCustomerEntity.getReservationRoom().getId())
            .stream()
            .filter(
                x ->
                    existingReservationCustomer == null
                        || !x.getId().equals(existingReservationCustomer.getId()))
            .toList();
    validationUtil.require(
        others.stream()
            .noneMatch(
                x ->
                    x.getCustomer()
                        .getId()
                        .equals(reservationCustomerEntity.getCustomer().getId())),
        "Customer is already linked to this room");
    validationUtil.require(
        others.size()
            < reservationCustomerEntity.getReservationRoom().getAdults()
                + reservationCustomerEntity.getReservationRoom().getChildren(),
        "Guest list exceeds booked occupancy");
    if (reservationCustomerEntity.getIsPrimaryCustomer() == null)
      reservationCustomerEntity.setIsPrimaryCustomer(false);
    validationUtil.require(
        !reservationCustomerEntity.getIsPrimaryCustomer()
            || others.stream().noneMatch(x -> Boolean.TRUE.equals(x.getIsPrimaryCustomer())),
        "A primary customer already exists");
  }
}
