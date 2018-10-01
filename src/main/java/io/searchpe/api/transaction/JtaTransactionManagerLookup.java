package io.searchpe.api.transaction;

import javax.transaction.TransactionManager;

public interface JtaTransactionManagerLookup {

    TransactionManager getTransactionManager();

}
