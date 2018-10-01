package io.searchpe.api.jpa;

import io.searchpe.api.connections.jpa.JpaConnectionProviderFactory;
import io.searchpe.api.core.exceptions.StorageException;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.RollbackException;

public abstract class AbstractJpaStorage {

    private static final Logger logger = Logger.getLogger(AbstractJpaStorage.class);

    @Inject
    private JpaConnectionProviderFactory emfAccessor;

    private static ThreadLocal<EntityManager> activeEM = new ThreadLocal<>();

    public static boolean isTxActive() {
        return activeEM.get() != null;
    }

    protected void beginTx() throws StorageException {
        if (activeEM.get() != null) {
            throw new StorageException("Transaction already active."); //$NON-NLS-1$
        }
        EntityManager entityManager = emfAccessor.getEntityManagerFactory().createEntityManager();
        activeEM.set(entityManager);
        entityManager.getTransaction().begin();
    }

    protected void commitTx() throws StorageException {
        if (activeEM.get() == null) {
            throw new StorageException("Transaction not active."); //$NON-NLS-1$
        }

        try {
            activeEM.get().getTransaction().commit();
            activeEM.get().close();
            activeEM.set(null);
        } catch (EntityExistsException e) {
            throw new StorageException(e);
        } catch (RollbackException e) {
            logger.error(e.getMessage(), e);
            throw new StorageException(e);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    protected void rollbackTx() {
        if (activeEM.get() == null) {
            throw new RuntimeException("Transaction not active."); //$NON-NLS-1$
        }
        try {
            JpaUtil.rollbackQuietly(activeEM.get());
        } finally {
            activeEM.get().close();
            activeEM.set(null);
        }
    }
}
