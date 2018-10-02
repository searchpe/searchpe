package io.searchpe.api.provider;

import io.searchpe.api.transaction.SearchpeTransactionManager;

public interface SearchpeSession {

    SearchpeTransactionManager getTransactionManager();

    void close();

    enum EventType {
        SESSION_CLOSE
    }

}
