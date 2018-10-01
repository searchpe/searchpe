package io.searchpe.api.transaction;

import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

@ApplicationScoped
public class JBossJtaTransactionManagerLookup implements JtaTransactionManagerLookup {

    private static final Logger logger = Logger.getLogger(JBossJtaTransactionManagerLookup.class);

    private TransactionManager tm;

    @PostConstruct
    private void init() {
        try {
            InitialContext ctx = new InitialContext();
            tm = (TransactionManager) ctx.lookup("java:jboss/TransactionManager");
            if (tm == null) {
                logger.debug("Could not locate TransactionManager");
            }
        } catch (NamingException e) {
            logger.debug("Could not load TransactionManager", e);
        }
    }

    @Override
    public TransactionManager getTransactionManager() {
        return tm;
    }

}
