package com.hms.dao;

import com.hms.entities.*;
import com.hms.exception.*;
import jakarta.persistence.*;
import java.util.*;
import org.springframework.stereotype.Repository;

/** Database access only. All query strings are internal constants, never client input. */
@Repository
public class HmsDao {
  @PersistenceContext private EntityManager em;

  public <T> T require(Class<T> type, Long id) {
    if (id == null || id <= 0) throw new InvalidRequestException("A positive ID is required");
    T value = em.find(type, id);
    if (value == null)
      throw new ResourceNotFoundException(type.getSimpleName() + " not found: " + id);
    return value;
  }

  public <T> List<T> all(Class<T> type) {
    return em.createQuery("from " + type.getSimpleName(), type).getResultList();
  }

  public <T> List<T> query(Class<T> type, String predicate, Object... parameters) {
    var q = em.createQuery("from " + type.getSimpleName() + " e where " + predicate, type);
    for (int i = 0; i < parameters.length; i++) q.setParameter("p" + i, parameters[i]);
    return q.getResultList();
  }

  public void persist(Object value) {
    em.persist(value);
  }

  public void remove(Object value) {
    em.remove(value);
  }

  public void flush() {
    em.flush();
  }

  /** Coarse, deterministic inventory/finance mutex. Every application writer must use it. */
  public void lockHotels() {
    em.createQuery("select h from HotelEntity h order by h.id", HotelEntity.class)
        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
        .getResultList();
  }
}
