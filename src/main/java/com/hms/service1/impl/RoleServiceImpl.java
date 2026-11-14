package com.hms.service.impl;

import com.hms.dao.*;
import com.hms.dto.RoleDTO;
import com.hms.entities.*;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.RoleService;
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
 * HMS business service for Role.
 * Maps entities at the service boundary; the DAO performs real SQL-level pagination.
 * All updates behave as partial updates (null DTO properties are left unchanged).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {
    private final RoleDao roleDao;

    private Role getEntity(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid Role ID");
        return roleDao.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + id));
    }

    private RoleDTO toDto(Role entity) {
        RoleDTO dto = new RoleDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setType(readEnumOrString(entity, "type"));
        return dto;
    }

    private void apply(RoleDTO dto, Role entity) {
        if (dto.getType() != null) writeEnumOrString(entity, dto.getType());
    }

    @Override
    public RoleDTO getSelectedRole(Long id) {
        return toDto(getEntity(id));
    }

    @Override
    public Page<RoleDTO> getAllRoles(Pageable pageable) {
        if (pageable == null || pageable.getPageSize() > 100) throw new IllegalArgumentException("Page size cannot exceed 100");
        return roleDao.findAll(pageable).map(this::toDto);
    }

    @Override
    @Transactional
    public RoleDTO addRole(RoleDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Role DTO is required");
        // Nothing extra.
        Role entity = new Role();
        apply(dto, entity);
        // Nothing extra.
        entity = roleDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public RoleDTO updateRole(Long id, RoleDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Role DTO is required");
        Role entity = getEntity(id);
        // Nothing extra.
        apply(dto, entity);
        // Nothing extra.
        entity = roleDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role entity = getEntity(id);
        // Nothing extra.
        roleDao.delete(entity);
        roleDao.flush();
        // Nothing extra.
    }



    private String readEnumOrString(Role entity, String property) {
        try {
            Object value = new PropertyDescriptor(property,Role.class).getReadMethod().invoke(entity);
            if (value == null) return null;
            try {return String.valueOf(value.getClass().getMethod("getValue").invoke(value));}
            catch (NoSuchMethodException ignored) {return value.toString();}
        } catch (ReflectiveOperationException | IntrospectionException ex) {
            throw new IllegalStateException("Cannot map role type",ex);
        }
    }

    private void writeEnumOrString(Role entity, String raw) {
        try {
            PropertyDescriptor prop = new PropertyDescriptor("type",Role.class);
            Class<?> target = prop.getPropertyType();
            Object value = raw;
            if (target.isEnum()) {
                try {value = target.getMethod("fromValue",String.class).invoke(null,raw.toLowerCase(Locale.ROOT));}
                catch (NoSuchMethodException ex) {value = Enum.valueOf((Class)target.asSubclass(Enum.class),raw.toUpperCase(Locale.ROOT));}
            }
            prop.getWriteMethod().invoke(entity,value);
        } catch (ReflectiveOperationException | IntrospectionException ex) {
            throw new IllegalArgumentException("Invalid role type: " + raw,ex);
        }
    }
}
