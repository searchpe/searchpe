package io.searchpe.api.transaction;

import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.TransactionManager;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class DefaultSearchpeTransactionManager implements SearchpeTransactionManager {

    private static final Logger logger = Logger.getLogger(DefaultSearchpeTransactionManager.class);

    private ThreadLocal<List<SearchpeTransaction>> prepare = new ThreadLocal<>();
    private ThreadLocal<List<SearchpeTransaction>> transactions = new ThreadLocal<>();
    private ThreadLocal<List<SearchpeTransaction>> afterCompletion = new ThreadLocal<>();
    private ThreadLocal<Boolean> active = new ThreadLocal<>();
    private ThreadLocal<Boolean> rollback = new ThreadLocal<>();

    private ThreadLocal<JTAPolicy> jtaPolicy = new ThreadLocal<>();

    // Used to prevent double committing/rollback if there is an uncaught exception
    private ThreadLocal<Boolean> completed = new ThreadLocal<>();

    @Inject
    private JtaTransactionManagerLookup jtaLookup;

    @Override
    public void enlist(SearchpeTransaction transaction) {
        if (active.get() != null && active.get() && !transaction.isActive()) {
            transaction.begin();
        }

        if (transactions.get() == null) {
            transactions.set(new ArrayList<>());
        }
        transactions.get().add(transaction);
    }

    @Override
    public void enlistAfterCompletion(SearchpeTransaction transaction) {
        if (active.get() != null && active.get() && !transaction.isActive()) {
            transaction.begin();
        }

        if (afterCompletion.get() == null) {
            afterCompletion.set(new ArrayList<>());
        }
        afterCompletion.get().add(transaction);
    }

    @Override
    public void enlistPrepare(SearchpeTransaction transaction) {
        if (active.get() != null && active.get() && !transaction.isActive()) {
            transaction.begin();
        }

        if (prepare.get() == null) {
            prepare.set(new ArrayList<>());
        }
        prepare.get().add(transaction);
    }

    @Override
    public JTAPolicy getJTAPolicy() {
        if (jtaPolicy.get() == null) {
            jtaPolicy.set(JTAPolicy.REQUIRES_NEW);
        }
        return jtaPolicy.get();
    }

    @Override
    public void setJTAPolicy(JTAPolicy policy) {
        jtaPolicy.set(policy);

    }

    @Override
    public void begin() {
        if (active.get() != null && active.get()) {
            throw new IllegalStateException("Transaction already active");
        }

        completed.set(false);

        if (getJTAPolicy().equals(JTAPolicy.REQUIRES_NEW)) {
            if (jtaLookup != null) {
                TransactionManager tm = jtaLookup.getTransactionManager();
                if (tm != null) {
                    enlist(new JtaTransactionWrapper(tm));
                }
            }
        }

        if (transactions.get() != null) {
            for (SearchpeTransaction tx : transactions.get()) {
                tx.begin();
            }
        }

        active.set(true);
    }

    @Override
    public void commit() {
        if (completed.get() != null && completed.get()) {
            return;
        } else {
            completed.set(true);
        }

        RuntimeException exception = null;
        if (prepare.get() != null) {
            for (SearchpeTransaction tx : prepare.get()) {
                try {
                    tx.commit();
                } catch (RuntimeException e) {
                    exception = exception == null ? e : exception;
                }
            }
        }
        if (exception != null) {
            rollback(exception);
            return;
        }
        if (transactions.get() != null) {
            for (SearchpeTransaction tx : transactions.get()) {
                try {
                    tx.commit();
                } catch (RuntimeException e) {
                    exception = exception == null ? e : exception;
                }
            }
        }

        // Don't commit "afterCompletion" if commit of some main transaction failed
        if (exception == null) {
            if (afterCompletion.get() != null) {
                for (SearchpeTransaction tx : afterCompletion.get()) {
                    try {
                        tx.commit();
                    } catch (RuntimeException e) {
                        exception = exception == null ? e : exception;
                    }
                }
            }
        } else {
            if (afterCompletion.get() != null) {
                for (SearchpeTransaction tx : afterCompletion.get()) {
                    try {
                        tx.rollback();
                    } catch (RuntimeException e) {
                        logger.error("Exception during rollback");
                    }
                }
            }
        }

        active.set(false);
        if (exception != null) {
            throw exception;
        }
    }

    @Override
    public void rollback() {
        if (completed.get()!= null && completed.get()) {
            return;
        } else {
            completed.set(true);
        }

        RuntimeException exception = null;
        rollback(exception);
    }

    protected void rollback(RuntimeException exception) {
        if (transactions.get() != null) {
            for (SearchpeTransaction tx : transactions.get()) {
                try {
                    tx.rollback();
                } catch (RuntimeException e) {
                    exception = exception != null ? e : exception;
                }
            }
        }
        if (afterCompletion.get() != null) {
            for (SearchpeTransaction tx : afterCompletion.get()) {
                try {
                    tx.rollback();
                } catch (RuntimeException e) {
                    exception = exception != null ? e : exception;
                }
            }
        }
        active.set(false);
        if (exception != null) {
            throw exception;
        }
    }

    @Override
    public void setRollbackOnly() {
        rollback.set(true);
    }

    @Override
    public boolean getRollbackOnly() {
        if (rollback.get() != null && rollback.get()) {
            return true;
        }

        if (transactions.get() != null) {
            for (SearchpeTransaction tx : transactions.get()) {
                if (tx.getRollbackOnly()) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean isActive() {
        return active.get() != null && active.get();
    }

}
