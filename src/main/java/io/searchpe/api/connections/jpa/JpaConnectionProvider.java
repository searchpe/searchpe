package io.searchpe.api.connections.jpa;

import io.searchpe.api.provider.Provider;

import javax.persistence.EntityManager;

public interface JpaConnectionProvider extends Provider {

    EntityManager getEntityManager();

}
