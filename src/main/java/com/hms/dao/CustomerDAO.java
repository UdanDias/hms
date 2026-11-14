package com.hms.dao;

import com.hms.dao.support.MutableHibernateDAO;
import com.hms.entities.CustomerEntity;
import java.util.List;
import org.springframework.stereotype.Repository;

/** Persistence access for CustomerEntity; business decisions are made by the service layer. */
@Repository
public class CustomerDAO extends MutableHibernateDAO<CustomerEntity> {
  public CustomerDAO() {
    super(CustomerEntity.class, "id", CustomerEntity::getId);
  }

  public List<CustomerEntity> findByUserId(Long userId) {
    positiveId(userId);
    return select("e.user.userId = :p0", userId);
  }

  public List<CustomerEntity> findByEmail(String email) {
    return select("e.email = :p0", email);
  }

  public List<CustomerEntity> findByNic(String nic) {
    return select("e.nic = :p0", nic);
  }
}
