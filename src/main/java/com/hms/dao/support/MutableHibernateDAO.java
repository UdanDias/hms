package com.hms.dao.support;

import com.hms.exception.InvalidRequestException;
import java.util.function.Function;
import org.springframework.transaction.annotation.*;

/** Low-level update/delete capability. Authorization and business rules belong in services. */
public abstract class MutableHibernateDAO<T> extends AbstractHibernateDAO<T> {
    protected MutableHibernateDAO(Class<T> type, String idAttribute, Function<T, Long> identifier) {
        super(type, idAttribute, identifier);
    }

    /** Normally prefer loading a managed entity and editing it in the service transaction. */
    @Transactional(propagation = Propagation.MANDATORY)
    public T merge(T entity) {
        if (entity == null || identifier.apply(entity) == null) throw new InvalidRequestException("An existing entity ID is required");
        lockById(identifier.apply(entity));
        return entityManager.merge(entity);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void remove(T entity) {
        if (entity == null || !entityManager.contains(entity)) throw new InvalidRequestException("Load the entity in this transaction before removing it");
        entityManager.remove(entity);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteById(Long id) {
        entityManager.remove(lockById(id));
    }
}
