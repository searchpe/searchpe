package io.searchpe.api.connections.jpa;

import io.searchpe.api.transaction.SearchpeTransaction;
import org.jboss.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

public class JpaSearchpeTransaction implements SearchpeTransaction {

    private static final Logger logger = Logger.getLogger(JpaSearchpeTransaction.class);

    protected EntityManager em;

    public JpaSearchpeTransaction(EntityManager em) {
        this.em = em;
    }

    @Override
    public void begin() {
        em.getTransaction().begin();
    }

    @Override
    public void commit() {
        try {
            logger.trace("Committing transaction");
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            throw PersistenceExceptionConverter.convert(e.getCause() != null ? e.getCause() : e);
        }
    }

    @Override
    public void rollback() {
        logger.trace("Rollback transaction");
        em.getTransaction().rollback();
    }

    @Override
    public void setRollbackOnly() {
        em.getTransaction().setRollbackOnly();
    }

    @Override
    public boolean getRollbackOnly() {
        return  em.getTransaction().getRollbackOnly();
    }

    @Override
    public boolean isActive() {
        return em.getTransaction().isActive();
    }
}
