package io.searchpe.api.connections.jpa;

import io.searchpe.api.provider.ProviderFactory;

import java.sql.Connection;

public interface JpaConnectionProviderFactory extends ProviderFactory<JpaConnectionProvider> {

    // Caller is responsible for closing connection
    Connection getConnection();

    String getSchema();

}
