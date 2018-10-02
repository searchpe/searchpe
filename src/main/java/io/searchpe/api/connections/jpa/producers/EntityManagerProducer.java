package io.searchpe.api.connections.jpa.producers;

import io.searchpe.api.connections.jpa.JpaConnectionProvider;
import io.searchpe.api.connections.jpa.JpaConnectionProviderFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@ApplicationScoped
public class EntityManagerProducer {

    @Inject
    private JpaConnectionProviderFactory jpaConnectionProviderFactory;

    private final ThreadLocal<JpaConnectionProvider> connectionProvider = new ThreadLocal<>();

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

}
