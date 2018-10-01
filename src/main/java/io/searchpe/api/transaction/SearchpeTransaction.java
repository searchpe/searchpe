package io.searchpe.api.transaction;

public interface SearchpeTransaction {
    void begin();
    void commit();
    void rollback();
    void setRollbackOnly();
    boolean getRollbackOnly();
    boolean isActive();
}
