package io.searchpe.api.transaction;

public interface SearchpeTransactionManager extends SearchpeTransaction {

    enum JTAPolicy {
        /**
         * Do not interact with JTA at all
         */
        NOT_SUPPORTED,
        /**
         * A new JTA Transaction will be created when Keycloak TM begin() is called.  If an existing JTA transaction
         * exists, it is suspended and resumed after the Keycloak transaction finishes.
         */
        REQUIRES_NEW,
    }

    JTAPolicy getJTAPolicy();

    void setJTAPolicy(JTAPolicy policy);

    void enlist(SearchpeTransaction transaction);

    void enlistAfterCompletion(SearchpeTransaction transaction);

    void enlistPrepare(SearchpeTransaction transaction);
}
