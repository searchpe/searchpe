package io.searchpe.api.connections.jpa.util;

import org.hibernate.boot.registry.classloading.internal.ClassLoaderServiceImpl;
import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;
import org.hibernate.jpa.boot.internal.PersistenceXmlParser;
import org.hibernate.jpa.boot.spi.Bootstrap;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitTransactionType;
import java.util.List;
import java.util.Map;

public class JpaUtils {

    public static final String HIBERNATE_DEFAULT_SCHEMA = "hibernate.default_schema";

    private JpaUtils() {
        // Just static methods
    }

    public static EntityManagerFactory createEntityManagerFactory(String unitName, Map<String, Object> properties, ClassLoader classLoader, boolean jta) {
        PersistenceUnitTransactionType txType = jta ? PersistenceUnitTransactionType.JTA : PersistenceUnitTransactionType.RESOURCE_LOCAL;
        List<ParsedPersistenceXmlDescriptor> persistenceUnits = PersistenceXmlParser.locatePersistenceUnits(properties);
        for (ParsedPersistenceXmlDescriptor persistenceUnit : persistenceUnits) {
            if (persistenceUnit.getName().equals(unitName)) {
                persistenceUnit.setTransactionType(txType);
                return Bootstrap.getEntityManagerFactoryBuilder(persistenceUnit, properties).build();
            }
        }
        throw new RuntimeException("Persistence unit '" + unitName + "' not found");
    }

}
