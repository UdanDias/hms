package com.hms.service.impl;

import com.hms.dao.HotelDAO;
import com.hms.dao.RoleDAO;
import com.hms.dto.RoleDTO;
import com.hms.entities.RoleEntity;
import com.hms.exception.RoleNotFoundException;
import com.hms.security.ServiceSecurity;
import com.hms.service.RoleService;
import com.hms.util.AuditRecorder;
import com.hms.util.EntityDTOConvert;
import com.hms.util.ValidationUtil;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class RoleServiceImpl implements RoleService {
  private final RoleDAO roleDao;
  private final HotelDAO hotelDao;
  private final EntityDTOConvert entityDTOConvert;
  private final ServiceSecurity serviceSecurity;
  private final ValidationUtil validationUtil;
  private final AuditRecorder auditRecorder;

  @Override
  @Transactional(readOnly = true)
  public RoleDTO getSelectedRole(Long roleId) {
    serviceSecurity.getCurrentUser();
    RoleEntity roleEntity =
        roleDao.findById(roleId).orElseThrow(() -> new RoleNotFoundException("Role not found"));
    serviceSecurity.checkRead(roleEntity);
    return entityDTOConvert.convertRoleEntityToRoleDTO(roleEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public List<RoleDTO> getAllRoles() {
    serviceSecurity.getCurrentUser();
    List<RoleEntity> entities =
        roleDao.findAll().stream().filter(serviceSecurity::canRead).toList();
    return entityDTOConvert.convertRoleEntityListToRoleDTOList(entities);
  }

  @Override
  public RoleDTO addRole(RoleDTO roleDTO) {
    serviceSecurity.requireRoles("ADMIN");
    hotelDao.lockAllForWrite();
    RoleEntity roleEntity = entityDTOConvert.convertRoleDTOToRoleEntity(roleDTO);

    validateRoleForSave(roleEntity, null);
    validationUtil.validate(roleEntity);
    roleDao.save(roleEntity);
    auditRecorder.recordAction("CREATE", "Role", roleEntity.getRoleId());
    roleDao.flush();
    return entityDTOConvert.convertRoleEntityToRoleDTO(roleEntity);
  }

  private void validateRoleForSave(RoleEntity roleEntity, RoleEntity existingRole) {
    validationUtil.text(roleEntity.getType(), "Role type");
    roleEntity.setType(roleEntity.getType().toUpperCase(Locale.ROOT));
    validationUtil.require(
        Set.of("ADMIN", "MANAGER", "RECEPTIONIST", "ACCOUNTANT", "HOUSEKEEPING", "GUEST")
            .contains(roleEntity.getType()),
        "Unsupported role type");
  }
}
