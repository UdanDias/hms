package com.hms.dao.support;

import com.hms.exception.InvalidRequestException;
import com.hms.exception.ResourceNotFoundException;
import jakarta.persistence.*;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.transaction.annotation.*;

/** Shared persistence operations. Query predicates must be developer-owned constants. */
@Transactional(readOnly = true)
public abstract class AbstractHibernateDAO<T> {
  @PersistenceContext protected EntityManager entityManager;
  protected final Class<T> entityType;
  protected final String idAttribute;
  protected final Function<T, Long> identifier;

  protected AbstractHibernateDAO(
      Class<T> entityType, String idAttribute, Function<T, Long> identifier) {
    this.entityType = entityType;
    this.idAttribute = idAttribute;
    this.identifier = identifier;
  }

  protected void positiveId(Long id) {
    if (id == null || id <= 0) throw new InvalidRequestException("A positive ID is required");
  }

  public Optional<T> findById(Long id) {
    positiveId(id);
    return Optional.ofNullable(entityManager.find(entityType, id));
  }

  public T require(Long id) {
    return findById(id)
        .orElseThrow(
            () -> new ResourceNotFoundException(entityType.getSimpleName() + " not found: " + id));
  }

  public boolean existsById(Long id) {
    return findById(id).isPresent();
  }

  public List<T> findAll() {
    return entityManager
        .createQuery(
            "select e from " + entityType.getSimpleName() + " e order by e." + idAttribute,
            entityType)
        .getResultList();
  }

  /** Stable ID-ordered pagination. Offset is zero-based; limit is capped at 500. */
  public List<T> findPage(int offset, int limit) {
    if (offset < 0 || limit < 1 || limit > 500)
      throw new InvalidRequestException(
          "Offset must be nonnegative; limit must be between 1 and 500");
    return entityManager
        .createQuery(
            "select e from " + entityType.getSimpleName() + " e order by e." + idAttribute,
            entityType)
        .setFirstResult(offset)
        .setMaxResults(limit)
        .getResultList();
  }

  public long count() {
    return entityManager
        .createQuery("select count(e) from " + entityType.getSimpleName() + " e", Long.class)
        .getSingleResult();
  }

  /**
   * Insert only. Existing entities must be updated as managed objects inside a service transaction.
   */
  @Transactional(propagation = Propagation.MANDATORY)
  public T persist(T entity) {
    if (entity == null || identifier.apply(entity) != null)
      throw new InvalidRequestException("A new entity without an ID is required");
    entityManager.persist(entity);
    return entity;
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void flush() {
    entityManager.flush();
  }

  /** The caller owns the transaction and its lock order. This method never commits. */
  @Transactional(propagation = Propagation.MANDATORY)
  public T lockById(Long id) {
    positiveId(id);
    T value = entityManager.find(entityType, id, LockModeType.PESSIMISTIC_WRITE);
    if (value == null)
      throw new ResourceNotFoundException(entityType.getSimpleName() + " not found: " + id);
    return value;
  }

  protected List<T> select(String predicate, Object... parameters) {
    return selectOrdered(predicate, "e." + idAttribute, parameters);
  }

  protected List<T> selectOrdered(String predicate, String order, Object... parameters) {
    var query =
        entityManager.createQuery(
            "select e from "
                + entityType.getSimpleName()
                + " e where "
                + predicate
                + " order by "
                + order,
            entityType);
    for (int i = 0; i < parameters.length; i++) query.setParameter("p" + i, parameters[i]);
    return query.getResultList();
  }

  protected Optional<T> selectOne(String predicate, Object... parameters) {
    var query =
        entityManager.createQuery(
            "select e from " + entityType.getSimpleName() + " e where " + predicate, entityType);
    for (int i = 0; i < parameters.length; i++) query.setParameter("p" + i, parameters[i]);
    var results = query.setMaxResults(2).getResultList();
    if (results.size() > 1) throw new IncorrectResultSizeDataAccessException(1, results.size());
    return results.stream().findFirst();
  }

  protected void dateRange(java.time.LocalDate start, java.time.LocalDate end) {
    if (start == null || end == null || !end.isAfter(start))
      throw new InvalidRequestException("End date must be after start date");
  }

  /** Save a new or already-managed entity, matching the service-layer reference style. */
  @Transactional(propagation = Propagation.MANDATORY)
  public T save(T entity) {
    if (entity == null) throw new InvalidRequestException("Entity is required");
    if (identifier.apply(entity) == null) return persist(entity);
    if (!entityManager.contains(entity))
      throw new InvalidRequestException(
          "Load existing records in the service transaction before saving");
    return entity;
  }
}
