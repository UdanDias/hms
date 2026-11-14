package com.hms.service.impl;

import com.hms.dao.HmsDao;
import com.hms.dto.*;
import com.hms.entities.*;
import com.hms.service.*;
import com.hms.service.impl.support.*;
import com.hms.util.EntityDTOConvert;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {
  private final HmsDao dao;
  private final EntityDTOConvert mapper;
  private final ServiceSupport s;

  @Override
  public RoleDTO getSelectedRole(Long roleId) {
    s.actor();
    var e = dao.require(RoleEntity.class, roleId);
    s.checkRead(e);
    return mapper.toDTO(e);
  }

  @Override
  public List<RoleDTO> getAllRoles() {
    s.actor();
    return dao.all(RoleEntity.class).stream().filter(s::visible).map(mapper::toDTO).toList();
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public RoleDTO addRole(RoleDTO dto) {
    s.admin();
    var e = mapper.toEntity(dto);
    prepare(e, null);
    s.validate(e);
    dao.persist(e);
    dao.flush();
    return finish(e, "CREATE");
  }

  private void prepare(RoleEntity e, RoleEntity old) {
    s.text(e.getType(), "Role type");
    e.setType(e.getType().toUpperCase(Locale.ROOT));
    s.require(
        Set.of("ADMIN", "MANAGER", "RECEPTIONIST", "ACCOUNTANT", "HOUSEKEEPING", "GUEST")
            .contains(e.getType()),
        "Unsupported role type");
  }

  private RoleDTO finish(RoleEntity e, String action) {
    s.validate(e);
    s.audit(action, "Role", e.getRoleId());
    dao.flush();
    return mapper.toDTO(e);
  }
}
