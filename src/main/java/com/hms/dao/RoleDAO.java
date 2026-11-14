package com.hms.dao;

import com.hms.dao.support.AbstractHibernateDAO;
import com.hms.entities.RoleEntity;
import com.hms.entities.enums.*;
import java.util.*;
import java.time.*;
import java.math.BigDecimal;
import org.springframework.stereotype.Repository;

/** Persistence access for RoleEntity; business decisions are made by the service layer. */
@Repository
public class RoleDAO extends AbstractHibernateDAO<RoleEntity> {
    public RoleDAO() {
        super(RoleEntity.class, "roleId", RoleEntity::getRoleId);
    }

    public Optional<RoleEntity> findByType(String type) {
        return selectOne("e.type = :p0", type);
    }

}
