package com.hms.service.impl;

import static com.hms.util.MoneyUtil.money;

import com.hms.dao.AccommodationDAO;
import com.hms.dao.HotelDAO;
import com.hms.dao.InvoiceDAO;
import com.hms.dao.InvoiceItemDAO;
import com.hms.dao.ReservationRoomDAO;
import com.hms.dao.ServiceChargeDAO;
import com.hms.dto.InvoiceItemDTO;
import com.hms.entities.AccommodationEntity;
import com.hms.entities.InvoiceEntity;
import com.hms.entities.InvoiceItemEntity;
import com.hms.entities.ReservationRoomEntity;
import com.hms.entities.ServiceChargeEntity;
import com.hms.exception.AccommodationNotFoundException;
import com.hms.exception.InvoiceItemNotFoundException;
import com.hms.exception.InvoiceNotFoundException;
import com.hms.exception.ReservationRoomNotFoundException;
import com.hms.exception.ServiceChargeNotFoundException;
import com.hms.security.ServiceSecurity;
import com.hms.service.InvoiceItemService;
import com.hms.util.AuditRecorder;
import com.hms.util.EntityDTOConvert;
import com.hms.util.MoneyUtil;
import com.hms.util.ValidationUtil;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class InvoiceItemServiceImpl implements InvoiceItemService {
  private final InvoiceDAO invoiceDao;
  private final InvoiceItemDAO invoiceItemDao;
  private final HotelDAO hotelDao;
  private final ServiceChargeDAO serviceChargeDao;
  private final AccommodationDAO accommodationDao;
  private final ReservationRoomDAO reservationRoomDao;
  private final EntityDTOConvert entityDTOConvert;
  private final ServiceSecurity serviceSecurity;
  private final ValidationUtil validationUtil;
  private final AuditRecorder auditRecorder;

  @Override
  @Transactional(readOnly = true)
  public InvoiceItemDTO getSelectedInvoiceItem(Long invoiceItemId) {
    serviceSecurity.getCurrentUser();
    InvoiceItemEntity invoiceItemEntity =
        invoiceItemDao
            .findById(invoiceItemId)
            .orElseThrow(() -> new InvoiceItemNotFoundException("InvoiceItem not found"));
    serviceSecurity.checkRead(invoiceItemEntity);
    return entityDTOConvert.convertInvoiceItemEntityToInvoiceItemDTO(invoiceItemEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public List<InvoiceItemDTO> getAllInvoiceItems() {
    serviceSecurity.getCurrentUser();
    List<InvoiceItemEntity> entities =
        invoiceItemDao.findAll().stream().filter(serviceSecurity::canRead).toList();
    return entityDTOConvert.convertInvoiceItemEntityListToInvoiceItemDTOList(entities);
  }

  @Override
  public InvoiceItemDTO addInvoiceItem(InvoiceItemDTO invoiceItemDTO) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "ACCOUNTANT");
    hotelDao.lockAllForWrite();
    InvoiceItemEntity invoiceItemEntity =
        entityDTOConvert.convertInvoiceItemDTOToInvoiceItemEntity(invoiceItemDTO);
    if (invoiceItemDTO.getInvoiceId() != null) {
      InvoiceEntity invoice =
          invoiceDao
              .findById(invoiceItemDTO.getInvoiceId())
              .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));
      invoiceItemEntity.setInvoice(invoice);
    }
    if (invoiceItemDTO.getReservationRoomId() != null) {
      ReservationRoomEntity reservationRoom =
          reservationRoomDao
              .findById(invoiceItemDTO.getReservationRoomId())
              .orElseThrow(() -> new ReservationRoomNotFoundException("ReservationRoom not found"));
      invoiceItemEntity.setReservationRoom(reservationRoom);
    }
    if (invoiceItemDTO.getServiceChargeId() != null) {
      ServiceChargeEntity serviceCharge =
          serviceChargeDao
              .findById(invoiceItemDTO.getServiceChargeId())
              .orElseThrow(() -> new ServiceChargeNotFoundException("ServiceCharge not found"));
      invoiceItemEntity.setServiceCharge(serviceCharge);
    }
    if (invoiceItemDTO.getAccommodationId() != null) {
      AccommodationEntity accommodation =
          accommodationDao
              .findById(invoiceItemDTO.getAccommodationId())
              .orElseThrow(() -> new AccommodationNotFoundException("Accommodation not found"));
      invoiceItemEntity.setAccommodation(accommodation);
    }

    validateInvoiceItemForSave(invoiceItemEntity, null);
    validationUtil.validate(invoiceItemEntity);
    invoiceItemDao.save(invoiceItemEntity);
    invoiceItemDao.flush();
    recalculateInvoiceTotals(invoiceItemEntity.getInvoice());
    validationUtil.validate(invoiceItemEntity);
    auditRecorder.recordAction("CREATE", "InvoiceItem", invoiceItemEntity.getId());
    invoiceItemDao.flush();
    return entityDTOConvert.convertInvoiceItemEntityToInvoiceItemDTO(invoiceItemEntity);
  }

  @Override
  public InvoiceItemDTO updateInvoiceItem(Long invoiceItemId, InvoiceItemDTO invoiceItemDTO) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "ACCOUNTANT");
    hotelDao.lockAllForWrite();
    InvoiceItemEntity invoiceItemEntity =
        invoiceItemDao
            .findById(invoiceItemId)
            .orElseThrow(() -> new InvoiceItemNotFoundException("InvoiceItem not found"));
    InvoiceItemEntity requestedInvoiceItem =
        entityDTOConvert.convertInvoiceItemDTOToInvoiceItemEntity(invoiceItemDTO);
    if (invoiceItemDTO.getInvoiceId() != null) {
      InvoiceEntity invoice =
          invoiceDao
              .findById(invoiceItemDTO.getInvoiceId())
              .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));
      requestedInvoiceItem.setInvoice(invoice);
    }
    if (invoiceItemDTO.getReservationRoomId() != null) {
      ReservationRoomEntity reservationRoom =
          reservationRoomDao
              .findById(invoiceItemDTO.getReservationRoomId())
              .orElseThrow(() -> new ReservationRoomNotFoundException("ReservationRoom not found"));
      requestedInvoiceItem.setReservationRoom(reservationRoom);
    }
    if (invoiceItemDTO.getServiceChargeId() != null) {
      ServiceChargeEntity serviceCharge =
          serviceChargeDao
              .findById(invoiceItemDTO.getServiceChargeId())
              .orElseThrow(() -> new ServiceChargeNotFoundException("ServiceCharge not found"));
      requestedInvoiceItem.setServiceCharge(serviceCharge);
    }
    if (invoiceItemDTO.getAccommodationId() != null) {
      AccommodationEntity accommodation =
          accommodationDao
              .findById(invoiceItemDTO.getAccommodationId())
              .orElseThrow(() -> new AccommodationNotFoundException("Accommodation not found"));
      requestedInvoiceItem.setAccommodation(accommodation);
    }

    validateInvoiceItemForSave(requestedInvoiceItem, invoiceItemEntity);
    validationUtil.validate(requestedInvoiceItem);
    invoiceItemEntity.setInvoice(requestedInvoiceItem.getInvoice());
    invoiceItemEntity.setReservationRoom(requestedInvoiceItem.getReservationRoom());
    invoiceItemEntity.setServiceCharge(requestedInvoiceItem.getServiceCharge());
    invoiceItemEntity.setDescription(requestedInvoiceItem.getDescription());
    invoiceItemEntity.setQuantity(requestedInvoiceItem.getQuantity());
    invoiceItemEntity.setUnitPrice(requestedInvoiceItem.getUnitPrice());
    invoiceItemEntity.setDiscountAmount(requestedInvoiceItem.getDiscountAmount());
    invoiceItemEntity.setTaxRate(requestedInvoiceItem.getTaxRate());
    invoiceItemEntity.setTaxAmount(requestedInvoiceItem.getTaxAmount());
    invoiceItemEntity.setServiceChargeAmount(requestedInvoiceItem.getServiceChargeAmount());
    invoiceItemEntity.setLineTotal(requestedInvoiceItem.getLineTotal());
    invoiceItemEntity.setAccommodation(requestedInvoiceItem.getAccommodation());
    recalculateInvoiceTotals(invoiceItemEntity.getInvoice());
    validationUtil.validate(invoiceItemEntity);
    invoiceItemDao.save(invoiceItemEntity);
    auditRecorder.recordAction("UPDATE", "InvoiceItem", invoiceItemEntity.getId());
    invoiceItemDao.flush();
    return entityDTOConvert.convertInvoiceItemEntityToInvoiceItemDTO(invoiceItemEntity);
  }

  @Override
  public void deleteInvoiceItem(Long invoiceItemId) {
    serviceSecurity.requireRoles("ADMIN", "MANAGER", "ACCOUNTANT");
    hotelDao.lockAllForWrite();
    InvoiceItemEntity invoiceItemEntity =
        invoiceItemDao
            .findById(invoiceItemId)
            .orElseThrow(() -> new InvoiceItemNotFoundException("InvoiceItem not found"));
    requireDraftInvoice(invoiceItemEntity.getInvoice());
    var invoice = invoiceItemEntity.getInvoice();
    auditRecorder.recordAction("DELETE", "InvoiceItem", invoiceItemId);
    invoiceItemDao.delete(invoiceItemEntity);
    invoiceItemDao.flush();
    recalculateInvoiceTotals(invoice);
  }

  private void validateInvoiceItemForSave(
      InvoiceItemEntity invoiceItemEntity, InvoiceItemEntity existingInvoiceItem) {
    validationUtil.require(invoiceItemEntity.getInvoice() != null, "Invoice is required");
    requireDraftInvoice(invoiceItemEntity.getInvoice());
    if (existingInvoiceItem != null) {
      requireDraftInvoice(existingInvoiceItem.getInvoice());
      validationUtil.same(
          invoiceItemEntity.getInvoice().getId(),
          existingInvoiceItem.getInvoice().getId(),
          "Invoice");
    }
    validationUtil.require(
        invoiceItemEntity.getReservationRoom() == null
            || invoiceItemEntity.getServiceCharge() == null,
        "An item cannot bill both a room and a service");
    if (invoiceItemEntity.getReservationRoom() != null) {
      var room = invoiceItemEntity.getReservationRoom();
      validationUtil.same(
          room.getReservation().getId(),
          invoiceItemEntity.getInvoice().getReservation().getId(),
          "Room reservation");
      validationUtil.require(
          !"pending".equals(room.getStatus()) && !"cancelled".equals(room.getStatus()),
          "Confirm the reservation before billing a room");
      ensureSourceNotBilled(true, room.getId(), existingInvoiceItem);
      invoiceItemEntity.setQuantity(BigDecimal.ONE);
      invoiceItemEntity.setUnitPrice(
          money(
              room.getTotalAmount()
                  .subtract(room.getTaxAmount())
                  .subtract(room.getServiceChargeAmount())
                  .add(room.getDiscountAmount())));
      invoiceItemEntity.setDiscountAmount(room.getDiscountAmount());
      invoiceItemEntity.setTaxRate(BigDecimal.ZERO);
      invoiceItemEntity.setTaxAmount(room.getTaxAmount());
      invoiceItemEntity.setServiceChargeAmount(room.getServiceChargeAmount());
      invoiceItemEntity.setAccommodation(room.getAccommodation());
      invoiceItemEntity.setDescription(
          "Stay "
              + room.getCheckInDate()
              + " to "
              + room.getCheckOutDate()
              + " — "
              + room.getAccommodation().getType().getValue());
    } else if (invoiceItemEntity.getServiceCharge() != null) {
      var charge = invoiceItemEntity.getServiceCharge();
      validationUtil.same(
          charge.getReservation().getId(),
          invoiceItemEntity.getInvoice().getReservation().getId(),
          "Service charge");
      validationUtil.require(
          "active".equals(charge.getStatus()), "Only active service charges can be billed");
      ensureSourceNotBilled(false, charge.getId(), existingInvoiceItem);
      invoiceItemEntity.setQuantity(charge.getQuantity());
      invoiceItemEntity.setUnitPrice(charge.getUnitPrice());
      invoiceItemEntity.setDiscountAmount(charge.getDiscountAmount());
      invoiceItemEntity.setTaxAmount(charge.getTaxAmount());
      invoiceItemEntity.setTaxRate(BigDecimal.ZERO);
      invoiceItemEntity.setServiceChargeAmount(BigDecimal.ZERO);
      invoiceItemEntity.setAccommodation(null);
      invoiceItemEntity.setDescription(charge.getDescriptionSnapshot());
    } else {
      validationUtil.require(
          invoiceItemEntity.getAccommodation() == null,
          "Accommodation is included through a room line, not billed independently");
      validationUtil.text(invoiceItemEntity.getDescription(), "Item description");
      invoiceItemEntity.setQuantity(validateQuantity(invoiceItemEntity.getQuantity()));
      invoiceItemEntity.setUnitPrice(MoneyUtil.nonnegative(invoiceItemEntity.getUnitPrice()));
      invoiceItemEntity.setDiscountAmount(
          MoneyUtil.nonnegative(invoiceItemEntity.getDiscountAmount()));
      var base = money(invoiceItemEntity.getQuantity().multiply(invoiceItemEntity.getUnitPrice()));
      validationUtil.require(
          invoiceItemEntity.getDiscountAmount().compareTo(base) <= 0,
          "Discount exceeds line amount");
      invoiceItemEntity.setTaxRate(validateTaxRate(invoiceItemEntity.getTaxRate()));
      invoiceItemEntity.setTaxAmount(
          money(
              base.subtract(invoiceItemEntity.getDiscountAmount())
                  .multiply(invoiceItemEntity.getTaxRate())
                  .divide(BigDecimal.valueOf(100))));
      invoiceItemEntity.setServiceChargeAmount(
          MoneyUtil.nonnegative(invoiceItemEntity.getServiceChargeAmount()));
    }
    invoiceItemEntity.setLineTotal(
        money(
            invoiceItemEntity
                .getQuantity()
                .multiply(invoiceItemEntity.getUnitPrice())
                .subtract(invoiceItemEntity.getDiscountAmount())
                .add(invoiceItemEntity.getTaxAmount())
                .add(invoiceItemEntity.getServiceChargeAmount())));
  }

  private void requireDraftInvoice(InvoiceEntity invoice) {
    validationUtil.require(
        "draft".equals(invoice.getStatus()), "Only draft invoices may be edited");
  }

  private void ensureSourceNotBilled(boolean roomSource, Long id, InvoiceItemEntity old) {
    List<InvoiceItemEntity> existingItems =
        roomSource
            ? invoiceItemDao.findNonVoidByReservationRoomId(id)
            : invoiceItemDao.findNonVoidByServiceChargeId(id);
    validationUtil.require(
        existingItems.stream().noneMatch(item -> old == null || !item.getId().equals(old.getId())),
        "This source is already billed on a non-void invoice");
  }

  private void recalculateInvoiceTotals(InvoiceEntity invoice) {
    invoiceItemDao.flush();
    resetInvoiceTotals(invoice);
    for (var item : invoiceItemDao.findByInvoiceId(invoice.getId())) {
      invoice.setSubtotal(
          invoice.getSubtotal().add(money(item.getQuantity().multiply(item.getUnitPrice()))));
      invoice.setDiscountTotal(invoice.getDiscountTotal().add(item.getDiscountAmount()));
      invoice.setTaxTotal(invoice.getTaxTotal().add(item.getTaxAmount()));
      invoice.setServiceChargeTotal(
          invoice.getServiceChargeTotal().add(item.getServiceChargeAmount()));
      invoice.setGrandTotal(invoice.getGrandTotal().add(item.getLineTotal()));
    }
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

  private void resetInvoiceTotals(InvoiceEntity e) {
    e.setSubtotal(money(null));
    e.setDiscountTotal(money(null));
    e.setTaxTotal(money(null));
    e.setServiceChargeTotal(money(null));
    e.setGrandTotal(money(null));
  }
}
