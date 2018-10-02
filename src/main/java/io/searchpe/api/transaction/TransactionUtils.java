package io.searchpe.api.transaction;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.InvalidTransactionException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

@ApplicationScoped
public class TransactionUtils {

    @Inject
    private JtaTransactionManagerLookup lookup;

    public void suspendJtaTransaction(Runnable runnable) {
        Transaction suspended = null;
        try {
            if (lookup != null) {
                if (lookup.getTransactionManager() != null) {
                    try {
                        suspended = lookup.getTransactionManager().suspend();
                    } catch (SystemException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            runnable.run();
        } finally {
            if (suspended != null) {
                try {
                    lookup.getTransactionManager().resume(suspended);
                } catch (InvalidTransactionException e) {
                    throw new RuntimeException(e);
                } catch (SystemException e) {
                    throw new RuntimeException(e);
                }
            }

        }

    }

}
