package io.searchpe.repository;

import io.searchpe.api.connections.jpa.producers.EntityManagerProducer;

import javax.inject.Inject;
import javax.persistence.EntityManager;

public abstract class AbstractRepository {

    @Inject
    EntityManagerProducer entityManagerProducer;

    protected EntityManager getEntityManager() {
        if (EntityManagerProducer.connectionProvider.get() == null) {
            entityManagerProducer.createEntityManager();
        }
        return EntityManagerProducer.connectionProvider.get().getEntityManager();
    }

}
