package com.hms.dao;

import com.hms.dao.support.AbstractHibernateDAO;
import com.hms.entities.RoomStatusEntity;
import com.hms.entities.enums.RoomStatusValue;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** Persistence access for RoomStatusEntity; business decisions are made by the service layer. */
@Repository
public class RoomStatusDAO extends AbstractHibernateDAO<RoomStatusEntity> {
  public RoomStatusDAO() {
    super(RoomStatusEntity.class, "statusId", RoomStatusEntity::getStatusId);
  }

  public Optional<RoomStatusEntity> findByStatus(RoomStatusValue status) {
    return selectOne("e.status = :p0", status);
  }
}
