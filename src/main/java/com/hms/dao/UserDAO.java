package com.hms.dao;

import com.hms.dao.support.MutableHibernateDAO;
import com.hms.entities.UserEntity;
import com.hms.entities.enums.*;
import java.util.*;
import java.time.*;
import java.math.BigDecimal;
import org.springframework.stereotype.Repository;

/** Persistence access for UserEntity; business decisions are made by the service layer. */
@Repository
public class UserDAO extends MutableHibernateDAO<UserEntity> {
    public UserDAO() {
        super(UserEntity.class, "userId", UserEntity::getUserId);
    }

    public List<UserEntity> findByRoleId(Long roleId) {
        positiveId(roleId); return select("e.role.roleId = :p0", roleId);
    }

    public Optional<UserEntity> findByUsername(String username) {
        return selectOne("e.username = :p0", username);
    }

    public Optional<UserEntity> findByEmail(String email) {
        return selectOne("e.email = :p0", email);
    }

}
