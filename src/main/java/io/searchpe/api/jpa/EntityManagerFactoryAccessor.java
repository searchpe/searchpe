package io.searchpe.api.jpa;

import org.hibernate.jpa.HibernatePersistenceProvider;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class EntityManagerFactoryAccessor implements IEntityManagerFactoryAccessor {

    @Inject
    private IJpaProperties jpaProperties;

    private EntityManagerFactory emf;

    @PostConstruct
    public void postConstruct() {
        Map<String, String> properties = new HashMap<>();

        // Get properties from apiman.properties
        Map<String, String> cp = jpaProperties.getAllHibernateProperties();
        if (cp != null) {
            properties.putAll(cp);
        }

        // Get two specific properties from the System (for backward compatibility only)
        String s = properties.get("hibernate.hbm2ddl.auto"); //$NON-NLS-1$
        if (s == null) {
            s = "validate"; //$NON-NLS-1$
        }
        String autoValue = System.getProperty("apiman.hibernate.hbm2ddl.auto", s); //$NON-NLS-1$
        s = properties.get("hibernate.dialect"); //$NON-NLS-1$
        if (s == null) {
            s = "org.hibernate.dialect.H2Dialect"; //$NON-NLS-1$
        }
        String dialect = System.getProperty("apiman.hibernate.dialect", s); //$NON-NLS-1$
        properties.put("hibernate.hbm2ddl.auto", autoValue); //$NON-NLS-1$
        properties.put("hibernate.dialect", dialect); //$NON-NLS-1$

        // First try using standard JPA to load the persistence unit.  If that fails, then
        // try using hibernate directly in a couple ways (depends on hibernate version and
        // platform we're running on).
        try {
            emf = Persistence.createEntityManagerFactory("apiman-manager-api-jpa", properties); //$NON-NLS-1$
        } catch (Throwable t1) {
            try {
                emf = new HibernatePersistenceProvider().createEntityManagerFactory("apiman-manager-api-jpa", properties); //$NON-NLS-1$
            } catch (Throwable t3) {
                throw t1;
            }
        }
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

}
