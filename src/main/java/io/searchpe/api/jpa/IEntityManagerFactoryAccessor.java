package io.searchpe.api.jpa;

import javax.persistence.EntityManagerFactory;

public interface IEntityManagerFactoryAccessor {

    /**
     * @return gets the {@link EntityManagerFactory}
     */
    EntityManagerFactory getEntityManagerFactory();

}
