package io.searchpe.api.jpa;

import io.searchpe.api.core.IStorage;
import io.searchpe.api.core.exceptions.StorageException;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JpaStorage extends AbstractJpaStorage implements IStorage {

    @Override
    public void beginTx() throws StorageException {
        super.beginTx();
    }

    @Override
    public void commitTx() throws StorageException {
        super.commitTx();
    }

    @Override
    public void rollbackTx() {
        super.rollbackTx();
    }

    @Override
    public void initialize() {
        // No-Op for JPA
    }

}
