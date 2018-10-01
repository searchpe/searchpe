package io.searchpe.api.core;

import io.searchpe.api.core.exceptions.StorageException;

public interface IStorage {

    void beginTx() throws StorageException;

    void commitTx() throws StorageException;

    void rollbackTx();

    void initialize();
}