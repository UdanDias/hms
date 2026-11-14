package com.hms.dao;

import com.hms.dao.support.MutableHibernateDAO;
import com.hms.entities.HotelEntity;
import java.util.List;
import org.springframework.stereotype.Repository;

/** Persistence access for HotelEntity; business decisions are made by the service layer. */
@Repository
public class HotelDAO extends MutableHibernateDAO<HotelEntity> {
  public HotelDAO() {
    super(HotelEntity.class, "id", HotelEntity::getId);
  }

  public List<HotelEntity> findActive() {
    return select("e.isActive = true");
  }

  /** Keep the established deterministic hotel lock order for all mutation workflows. */
  @org.springframework.transaction.annotation.Transactional(
      propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
  public void lockAllForWrite() {
    entityManager
        .createQuery("select h from HotelEntity h order by h.id", HotelEntity.class)
        .setLockMode(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
        .getResultList();
  }
}
