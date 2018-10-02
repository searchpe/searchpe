package io.searchpe.api.connections.jpa.producers;

import io.searchpe.api.connections.jpa.JpaConnectionProvider;
import io.searchpe.api.connections.jpa.JpaConnectionProviderFactory;
import io.searchpe.api.provider.SearchpeSession;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@ApplicationScoped
public class EntityManagerProducer {

    private static final Logger logger = Logger.getLogger(EntityManagerProducer.class);

    @Inject
    private JpaConnectionProviderFactory jpaConnectionProviderFactory;

    public static final ThreadLocal<JpaConnectionProvider> connectionProvider = new ThreadLocal<>();

    @Produces
    public EntityManager createEntityManager() {
        if (connectionProvider.get() == null) {
            synchronized (this) {
                if (connectionProvider.get() == null) {
                    connectionProvider.set(jpaConnectionProviderFactory.create());
                }
            }
        }
        return connectionProvider.get().getEntityManager();
    }

    public void watchSessionEvents(@Observes SearchpeSession.EventType event) {
        switch (event) {
            case SESSION_CLOSE:
                if (connectionProvider.get() != null) {
                    JpaConnectionProvider jpaConnectionProvider = connectionProvider.get();
                    jpaConnectionProvider.close();
                }
                break;
            default:
                logger.debug("Nothing to do with event");
        }
    }

}
