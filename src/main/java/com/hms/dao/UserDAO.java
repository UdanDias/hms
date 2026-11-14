package com.hms.dao;

import com.hms.dao.support.MutableHibernateDAO;
import com.hms.entities.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** Persistence access for UserEntity; business decisions are made by the service layer. */
@Repository
public class UserDAO extends MutableHibernateDAO<UserEntity> {
  public UserDAO() {
    super(UserEntity.class, "userId", UserEntity::getUserId);
  }

  public List<UserEntity> findByRoleId(Long roleId) {
    positiveId(roleId);
    return select("e.role.roleId = :p0", roleId);
  }

  public Optional<UserEntity> findByUsername(String username) {
    return selectOne("e.username = :p0", username);
  }

  public Optional<UserEntity> findByEmail(String email) {
    return selectOne("e.email = :p0", email);
  }

  public List<UserEntity> findAdministrators() {
    return select("e.role.type = 'ADMIN'");
  }

  public List<UserEntity> findAccountsByUsername(String p0) {
    return select("e.username = :p0", p0);
  }
}
