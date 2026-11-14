package com.hms.service.impl;

import com.hms.dao.*;
import com.hms.dto.RoomDTO;
import com.hms.entities.*;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;
import java.lang.reflect.*;

/**
 * HMS business service for Room.
 * Maps entities at the service boundary; the DAO performs real SQL-level pagination.
 * All updates behave as partial updates (null DTO properties are left unchanged).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomServiceImpl implements RoomService {
    private final RoomDao roomDao;
    private final HotelDao hotelDao;
    private final RoomTypeDao roomTypeDao;
    private final RoomStatusDao roomStatusDao;

    private Room getEntity(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid Room ID");
        return roomDao.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + id));
    }

    private RoomDTO toDto(Room entity) {
        RoomDTO dto = new RoomDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setHotelId(entity.getHotel() == null ? null : entity.getHotel().getId());
        dto.setRoomTypeId(entity.getRoomType() == null ? null : entity.getRoomType().getId());
        dto.setStatusId(entity.getRoomStatus() == null ? null : entity.getRoomStatus().getStatusId());
        dto.setOperationalStatus(readEnumOrString(entity, "operationalStatus"));
        dto.setHousekeepingStatus(readEnumOrString(entity, "housekeepingStatus"));
        return dto;
    }

    private void apply(RoomDTO dto, Room entity) {
        if (dto.getRoomNumber() != null) entity.setRoomNumber(dto.getRoomNumber());
        if (dto.getFloor() != null) entity.setFloor(dto.getFloor());
        if (dto.getOperationalStatus() != null) writeEnumOrString(entity, "operationalStatus", dto.getOperationalStatus());
        if (dto.getHousekeepingStatus() != null) writeEnumOrString(entity, "housekeepingStatus", dto.getHousekeepingStatus());
        if (dto.getNotes() != null) entity.setNotes(dto.getNotes());
        if (dto.getHotelId() != null) entity.setHotel(
                    hotelDao.findById(dto.getHotelId())
                        .orElseThrow(() -> new ResourceNotFoundException("HotelEntity not found: " + dto.getHotelId())));
        if (dto.getRoomTypeId() != null) entity.setRoomType(
                    roomTypeDao.findById(dto.getRoomTypeId())
                        .orElseThrow(() -> new ResourceNotFoundException("RoomType not found: " + dto.getRoomTypeId())));
        if (dto.getStatusId() != null) entity.setRoomStatus(
                    roomStatusDao.findById(dto.getStatusId())
                        .orElseThrow(() -> new ResourceNotFoundException("RoomStatus not found: " + dto.getStatusId())));
    }

    @Override
    public RoomDTO getSelectedRoom(Long id) {
        return toDto(getEntity(id));
    }

    @Override
    public Page<RoomDTO> getAllRooms(Pageable pageable) {
        if (pageable == null || pageable.getPageSize() > 100) throw new IllegalArgumentException("Page size cannot exceed 100");
        return roomDao.findAll(pageable).map(this::toDto);
    }

    @Override
    @Transactional
    public RoomDTO addRoom(RoomDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Room DTO is required");
        // Nothing extra.
        Room entity = new Room();
        apply(dto, entity);
        // Nothing extra.
        validateRoomRelations(entity);
        entity = roomDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public RoomDTO updateRoom(Long id, RoomDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Room DTO is required");
        Room entity = getEntity(id);
        // Nothing extra.
        apply(dto, entity);
        // Nothing extra.
        validateRoomRelations(entity);
        entity = roomDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public void deleteRoom(Long id) {
        Room entity = getEntity(id);
        // Nothing extra.
        roomDao.delete(entity);
        roomDao.flush();
        // Nothing extra.
    }


    @Override @Transactional
    public RoomDTO updateHousekeepingStatus(Long roomId, String status) {
        if (status == null || status.isBlank()) throw new IllegalArgumentException("Housekeeping status is required");
        Room room = getEntity(roomId);
        writeEnumOrString(room, "housekeepingStatus", status.trim().toUpperCase(Locale.ROOT));
        return toDto(roomDao.saveAndFlush(room));
    }


    private String readEnumOrString(Room entity, String property) {
        try {
            Object value = new PropertyDescriptor(property,Room.class).getReadMethod().invoke(entity);
            if (value == null) return null;
            try {return String.valueOf(value.getClass().getMethod("getValue").invoke(value));}
            catch (NoSuchMethodException ignored) {return value.toString();}
        } catch (ReflectiveOperationException | IntrospectionException ex) {
            throw new IllegalStateException("Cannot map room " + property,ex);
        }
    }

    // Keep this mapper compatible with String statuses and future enum-backed status fields.
    private void writeEnumOrString(Room entity, String property, String raw) {
        try {
            PropertyDescriptor prop = new PropertyDescriptor(property,Room.class);
            Class<?> target = prop.getPropertyType();
            Object value = raw;
            if (target.isEnum()) {
                try {value = target.getMethod("fromValue",String.class).invoke(null,raw.toLowerCase(Locale.ROOT));}
                catch (NoSuchMethodException ex) {value = Enum.valueOf((Class)target.asSubclass(Enum.class),raw.toUpperCase(Locale.ROOT));}
            }
            prop.getWriteMethod().invoke(entity,value);
        } catch (ReflectiveOperationException | IntrospectionException ex) {
            throw new IllegalArgumentException("Invalid room " + property + ": " + raw,ex);
        }
    }
    private void validateRoomRelations(Room room) {
        if (room.getHotel() == null || room.getRoomType() == null || room.getRoomStatus() == null)
            throw new IllegalArgumentException("Room hotel, room type and room status are required");
        if (!room.getHotel().getId().equals(room.getRoomType().getHotel().getId()))
            throw new IllegalArgumentException("Room type belongs to another hotel");
    }


}
