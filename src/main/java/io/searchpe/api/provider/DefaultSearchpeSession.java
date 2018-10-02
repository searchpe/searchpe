package io.searchpe.api.provider;

import io.searchpe.api.transaction.SearchpeTransactionManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

@ApplicationScoped
public class DefaultSearchpeSession implements SearchpeSession {

    @Inject
    private Event<EventType> event;

    @Inject
    private SearchpeTransactionManager transactionManager;

    @Override
    public SearchpeTransactionManager getTransactionManager() {
        return transactionManager;
    }

    @Override
    public void close() {
        event.fire(EventType.SESSION_CLOSE);
    }

}
