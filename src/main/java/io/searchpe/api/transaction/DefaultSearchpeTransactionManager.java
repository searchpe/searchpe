package io.searchpe.api.transaction;

import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.TransactionManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class DefaultSearchpeTransactionManager implements SearchpeTransactionManager {

    private static final Logger logger = Logger.getLogger(DefaultSearchpeTransactionManager.class);

    private ThreadLocal<List<SearchpeTransaction>> prepare = new ThreadLocal<>();
    private ThreadLocal<List<SearchpeTransaction>> transactions = new ThreadLocal<>();
    private ThreadLocal<List<SearchpeTransaction>> afterCompletion = new ThreadLocal<>();
    private ThreadLocal<Optional<Boolean>> active = new ThreadLocal<>();
    private ThreadLocal<Optional<Boolean>> rollback = new ThreadLocal<>();

    private ThreadLocal<Optional<JTAPolicy>> jtaPolicy = new ThreadLocal<>();

    // Used to prevent double committing/rollback if there is an uncaught exception
    private ThreadLocal<Optional<Boolean>> completed = new ThreadLocal<>();

    @Inject
    private JtaTransactionManagerLookup jtaLookup;

    @Override
    public void enlist(SearchpeTransaction transaction) {
        if (active.get().orElse(false) && !transaction.isActive()) {
            transaction.begin();
        }

        if (transactions.get() == null) {
            transactions.set(new ArrayList<>());
        }
        transactions.get().add(transaction);
    }

    @Override
    public void enlistAfterCompletion(SearchpeTransaction transaction) {
        if (active.get().orElse(false) && !transaction.isActive()) {
            transaction.begin();
        }

        if (afterCompletion.get() == null) {
            afterCompletion.set(new ArrayList<>());
        }
        afterCompletion.get().add(transaction);
    }

    @Override
    public void enlistPrepare(SearchpeTransaction transaction) {
        if (active.get().orElse(false) && !transaction.isActive()) {
            transaction.begin();
        }

        if (prepare.get() == null) {
            prepare.set(new ArrayList<>());
        }
        prepare.get().add(transaction);
    }

    @Override
    public JTAPolicy getJTAPolicy() {
        if (!jtaPolicy.get().isPresent()) {
            jtaPolicy.set(Optional.of(JTAPolicy.REQUIRES_NEW));
        }
        return jtaPolicy.get().get();
    }

    @Override
    public void setJTAPolicy(JTAPolicy policy) {
        jtaPolicy.set(Optional.of(policy));

    }

    @Override
    public void begin() {
        if (active.get().orElse(false)) {
            throw new IllegalStateException("Transaction already active");
        }

        completed.set(Optional.of(false));

        if (getJTAPolicy().equals(JTAPolicy.REQUIRES_NEW)) {
            if (jtaLookup != null) {
                TransactionManager tm = jtaLookup.getTransactionManager();
                if (tm != null) {
                    enlist(new JtaTransactionWrapper(tm));
                }
            }
        }

        for (SearchpeTransaction tx : transactions.get()) {
            tx.begin();
        }

        active.set(Optional.of(true));
    }

    @Override
    public void commit() {
        if (completed.get().orElse(false)) {
            return;
        } else {
            completed.set(Optional.of(true));
        }

        RuntimeException exception = null;
        for (SearchpeTransaction tx : prepare.get()) {
            try {
                tx.commit();
            } catch (RuntimeException e) {
                exception = exception == null ? e : exception;
            }
        }
        if (exception != null) {
            rollback(exception);
            return;
        }
        for (SearchpeTransaction tx : transactions.get()) {
            try {
                tx.commit();
            } catch (RuntimeException e) {
                exception = exception == null ? e : exception;
            }
        }

        // Don't commit "afterCompletion" if commit of some main transaction failed
        if (exception == null) {
            for (SearchpeTransaction tx : afterCompletion.get()) {
                try {
                    tx.commit();
                } catch (RuntimeException e) {
                    exception = exception == null ? e : exception;
                }
            }
        } else {
            for (SearchpeTransaction tx : afterCompletion.get()) {
                try {
                    tx.rollback();
                } catch (RuntimeException e) {
                    logger.error("Exception during rollback");
                }
            }
        }

        active.set(Optional.of(false));
        if (exception != null) {
            throw exception;
        }
    }

    @Override
    public void rollback() {
        if (completed.get().orElse(false)) {
            return;
        } else {
            completed.set(Optional.of(true));
        }

        RuntimeException exception = null;
        rollback(exception);
    }

    protected void rollback(RuntimeException exception) {
        for (SearchpeTransaction tx : transactions.get()) {
            try {
                tx.rollback();
            } catch (RuntimeException e) {
                exception = exception != null ? e : exception;
            }
        }
        for (SearchpeTransaction tx : afterCompletion.get()) {
            try {
                tx.rollback();
            } catch (RuntimeException e) {
                exception = exception != null ? e : exception;
            }
        }
        active.set(Optional.of(false));
        if (exception != null) {
            throw exception;
        }
    }

    @Override
    public void setRollbackOnly() {
        rollback.set(Optional.of(true));
    }

    @Override
    public boolean getRollbackOnly() {
        if (rollback.get().orElse(false)) {
            return true;
        }

        for (SearchpeTransaction tx : transactions.get()) {
            if (tx.getRollbackOnly()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isActive() {
        return active.get().orElse(false);
    }

}
