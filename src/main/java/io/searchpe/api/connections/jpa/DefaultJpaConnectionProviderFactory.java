package io.searchpe.api.connections.jpa;

import io.searchpe.api.connections.jpa.util.JpaUtils;
import io.searchpe.api.provider.ServerInfoAwareProviderFactory;
import io.searchpe.api.transaction.JtaTransactionManagerLookup;
import io.searchpe.api.transaction.SearchpeTransactionManager;
import io.searchpe.api.transaction.TransactionUtils;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.hibernate.ejb.AvailableSettings;
import org.hibernate.engine.transaction.jta.platform.internal.AbstractJtaPlatform;
import org.jboss.logging.Logger;
import org.wildfly.swarm.spi.api.config.ConfigView;
import org.wildfly.swarm.spi.runtime.annotations.ConfigurationValue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.SynchronizationType;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class DefaultJpaConnectionProviderFactory implements JpaConnectionProviderFactory, ServerInfoAwareProviderFactory {

    private static final Logger logger = Logger.getLogger(DefaultJpaConnectionProviderFactory.class);

    private volatile EntityManagerFactory emf;
    private Map<String, String> operationalInfo;

    private boolean jtaEnabled;

    @Inject
    private Instance<JtaTransactionManagerLookup> jtaLookup;

    @Inject
    private TransactionUtils transactionUtils;

    @Inject
    private SearchpeTransactionManager transactionManager;

    // Data source

    @Inject
    private ConfigView configView;

    @Inject
    @ConfigurationValue("swarm.datasources.searchpe")
    private String datasource;

    @Inject
    @ConfigurationValue("swarm.datasources.data-sources.searchpe.user-name")
    private String datasourceUser;

    @Inject
    @ConfigurationValue("swarm.datasources.data-sources.searchpe.password")
    private String datasourcePassword;

    @Inject
    @ConfigurationValue("swarm.datasources.data-sources.searchpe.driver-class")
    private String datasourceDriverClass;

    @Inject
    @ConfigurationValue("swarm.datasources.data-sources.searchpe.driver-name")
    private String datasourceDriverName;

    @Inject
    @ConfigurationValue("swarm.datasources.data-sources.searchpe.connection-url")
    private String datasourceConnectionUrl;

    @Inject
    @ConfigurationValue("swarm.datasources.data-sources.searchpe.jta")
    private Optional<Boolean> datasourceJtaEnabled;


    // Hibernate

    @Inject
    @ConfigurationValue("searchpe.hibernate.schema")
    private String hibernateSchema;

    @Inject
    @ConfigurationValue("searchpe.hibernate.show-sql")
    private Optional<Boolean> hibernateShowSql;

    @Inject
    @ConfigurationValue("searchpe.hibernate.format-sql")
    private Optional<Boolean> hibernateFormatSql;

    @Inject
    @ConfigurationValue("searchpe.hibernate.dialect")
    private String hibernateDialect;

    @Inject
    @ConfigurationValue("searchpe.hibernate.global-stats-interval")
    private Optional<Integer> hibernateGlobalStatsInterval;

    @PostConstruct
    public void init() {
        checkJtaEnabled();
        logger.trace("Create JpaConnectionProvider");
    }

    @PreDestroy
    private void close() {
        if (emf != null) {
            emf.close();
        }
    }

    @Override
    public Map<String, String> getOperationalInfo() {
        return operationalInfo;
    }

    @Override
    public JpaConnectionProvider create() {
        logger.trace("Create JpaConnectionProvider");
        lazyInit();

        EntityManager em = null;
        if (!jtaEnabled) {
            logger.trace("enlisting EntityManager in JpaSearchpeTransaction");
            em = emf.createEntityManager();
        } else {
            em = emf.createEntityManager(SynchronizationType.SYNCHRONIZED);
        }
        em = PersistenceExceptionConverter.create(em);
        if (!jtaEnabled) {
            transactionManager.enlist(new JpaSearchpeTransaction(em));
        }
        return new DefaultJpaConnectionProvider(em);
    }

    @Override
    public String getId() {
        return "default";
    }

    private void lazyInit() {
        if (emf == null) {
            synchronized (this) {
                if (emf == null) {
                    transactionUtils.suspendJtaTransaction(() -> {
                        logger.debug("Initializing JPA connections");

                        Map<String, Object> properties = new HashMap<>();

                        String unitName = "searchpe-default";

                        if (datasource != null) {
                            if (datasourceJtaEnabled.orElse(jtaEnabled)) {
                                properties.put(AvailableSettings.JTA_DATASOURCE, datasource);
                            } else {
                                properties.put(AvailableSettings.NON_JTA_DATASOURCE, datasource);
                            }
                        } else {
                            properties.put(AvailableSettings.JDBC_URL, datasourceConnectionUrl);
                            properties.put(AvailableSettings.JDBC_DRIVER, datasourceDriverClass);

                            String user = datasourceUser;
                            if (user != null) {
                                properties.put(AvailableSettings.JDBC_USER, user);
                            }
                            String password = datasourcePassword;
                            if (password != null) {
                                properties.put(AvailableSettings.JDBC_PASSWORD, password);
                            }
                        }

                        String schema = hibernateSchema;
                        if (schema != null) {
                            properties.put(JpaUtils.HIBERNATE_DEFAULT_SCHEMA, schema);
                        }

                        properties.put("hibernate.show_sql", hibernateShowSql.orElse(false));
                        properties.put("hibernate.format_sql", hibernateFormatSql.orElse(true));

                        Connection connection = getConnection();
                        try {
                            prepareOperationalInfo(connection);

                            String driverDialect = detectDialect(connection);
                            if (driverDialect != null) {
                                properties.put("hibernate.dialect", driverDialect);
                            }

                            migration(schema, connection);

                            int globalStatsInterval = hibernateGlobalStatsInterval.orElse(-1);
                            if (globalStatsInterval != -1) {
                                properties.put("hibernate.generate_statistics", true);
                            }

                            logger.trace("Creating EntityManagerFactory");
                            logger.tracef("***** create EMF jtaEnabled {0} ", jtaEnabled);
                            if (jtaEnabled) {
                                properties.put(org.hibernate.cfg.AvailableSettings.JTA_PLATFORM, new AbstractJtaPlatform() {
                                    @Override
                                    protected TransactionManager locateTransactionManager() {
                                        return jtaLookup.get().getTransactionManager();
                                    }

                                    @Override
                                    protected UserTransaction locateUserTransaction() {
                                        return null;
                                    }
                                });
                            }
                            emf = JpaUtils.createEntityManagerFactory(unitName, properties, getClass().getClassLoader(), jtaEnabled);
                            logger.trace("EntityManagerFactory created");

                            if (globalStatsInterval != -1) {
                                startGlobalStats(globalStatsInterval);
                            }
                        } finally {
                            // Close after creating EntityManagerFactory to prevent in-mem databases from closing
                            if (connection != null) {
                                try {
                                    connection.close();
                                } catch (SQLException e) {
                                    logger.warn("Can't close connection", e);
                                }
                            }
                        }
                    });
                }
            }
        }
    }

    protected void checkJtaEnabled() {
        if (!jtaLookup.isUnsatisfied()) {
            if (jtaLookup.get().getTransactionManager() != null) {
                jtaEnabled = true;
            }
        }
    }

    public Connection getConnection() {
        try {
            String dataSourceLookup = datasource;
            if (dataSourceLookup != null) {
                DataSource dataSource = (DataSource) new InitialContext().lookup(dataSourceLookup);
                return dataSource.getConnection();
            } else {
                Class.forName(datasourceDriverClass);
                return DriverManager.getConnection(datasourceConnectionUrl, datasourceUser, datasourcePassword);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    @Override
    public String getSchema() {
        return hibernateSchema;
    }

    public DataSource getDataSource() {
        try {
            String dataSourceLookup = datasource;
            if (dataSourceLookup != null) {
                return (DataSource) new InitialContext().lookup(dataSourceLookup);
            } else {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                return new DriverDataSource(classLoader, datasourceDriverClass, datasourceConnectionUrl, datasourceUser, datasourcePassword);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get datasource", e);
        }
    }

    protected void prepareOperationalInfo(Connection connection) {
        try {
            operationalInfo = new LinkedHashMap<>();
            DatabaseMetaData md = connection.getMetaData();
            operationalInfo.put("databaseUrl", md.getURL());
            operationalInfo.put("databaseUser", md.getUserName());
            operationalInfo.put("databaseProduct", md.getDatabaseProductName() + " " + md.getDatabaseProductVersion());
            operationalInfo.put("databaseDriver", md.getDriverName() + " " + md.getDriverVersion());

            logger.debugf("Database info: %s", operationalInfo.toString());
        } catch (SQLException e) {
            logger.warn("Unable to prepare operational info due database exception: " + e.getMessage());
        }
    }

    protected String detectDialect(Connection connection) {
        String driverDialect = hibernateDialect;
        if (driverDialect != null && driverDialect.length() > 0) {
            return driverDialect;
        } else {
            try {
                String dbProductName = connection.getMetaData().getDatabaseProductName();
                String dbProductVersion = connection.getMetaData().getDatabaseProductVersion();

                // For MSSQL2014, we may need to fix the autodetected dialect by hibernate
                if (dbProductName.equals("Microsoft SQL Server")) {
                    String topVersionStr = dbProductVersion.split("\\.")[0];
                    boolean shouldSet2012Dialect = true;
                    try {
                        int topVersion = Integer.parseInt(topVersionStr);
                        if (topVersion < 12) {
                            shouldSet2012Dialect = false;
                        }
                    } catch (NumberFormatException nfe) {
                    }
                    if (shouldSet2012Dialect) {
                        String sql2012Dialect = "org.hibernate.dialect.SQLServer2012Dialect";
                        logger.debugf("Manually override hibernate dialect to %s", sql2012Dialect);
                        return sql2012Dialect;
                    }
                }
            } catch (SQLException e) {
                logger.warnf("Unable to detect hibernate dialect due database exception : %s", e.getMessage());
            }

            return null;
        }
    }

    protected void startGlobalStats(int globalStatsIntervalSecs) {
//        logger.debugf("Started Hibernate statistics with the interval %s seconds", globalStatsIntervalSecs);
//        TimerProvider timer = session.getProvider(TimerProvider.class);
//        timer.scheduleTask(new HibernateStatsReporter(emf), globalStatsIntervalSecs * 1000, "ReportHibernateGlobalStats");
    }

    public void migration(String schema, Connection connection) {
        DataSource dataSource = getDataSource();

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);

        if (datasourceDriverName.equals("h2")) {
            flyway.setLocations("classpath:db/migration/h2");
        } else if (datasourceDriverName.equals("postgresql")) {
            flyway.setLocations("classpath:db/migration/postgresql");
        } else if (datasourceDriverName.equals("mysql")) {
            flyway.setLocations("classpath:db/migration/mysql");
        } else if (datasourceDriverName.equals("oracle")) {
            flyway.setLocations("classpath:db/migration/oracle");
        } else {
            throw new IllegalStateException("Not supported Dialect");
        }

        MigrationInfo migrationInfo = flyway.info().current();
        if (migrationInfo == null) {
            logger.info("No existing database at the actual DataSource");
        } else {
            logger.infof("Found a database with the version: %s", migrationInfo.getVersion());
        }

        flyway.migrate();
        logger.info("Finished Flyway Migration");
    }

}
