package io.searchpe.producers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@ApplicationScoped
public class EntityManagerProducer {

//    @PersistenceContext(unitName = "SearchpePU")
    private EntityManager em;

    @Produces
    public EntityManager createEntityManager() {
//        return em;
        return null;
    }

    @Produces
    @ContainerEntityManager
    public EntityManager createContainerEntityManager() {
//        return em;
        return null;
    }

}
