package io.searchpe.api.jpa;

import java.util.Map;

public interface IJpaProperties {

    /**
     * @return all configured hibernate properties
     */
    Map<String, String> getAllHibernateProperties();

}
