package io.searchpe.api.connections.jpa;

import org.jboss.logging.Logger;

import javax.persistence.EntityManager;

public class DefaultJpaConnectionProvider implements JpaConnectionProvider {

    private static final Logger logger = Logger.getLogger(DefaultJpaConnectionProvider.class);
    private final EntityManager em;

    public DefaultJpaConnectionProvider(EntityManager em) {
        this.em = em;
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public void close() {
        logger.trace("DefaultJpaConnectionProvider close()");
        em.close();
    }

}
