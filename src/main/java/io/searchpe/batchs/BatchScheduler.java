package io.searchpe.batchs;

import io.searchpe.api.provider.SearchpeSession;
import io.searchpe.api.transaction.SearchpeTransactionManager;
import io.searchpe.model.Version;
import io.searchpe.services.VersionService;
import io.searchpe.utils.DateUtils;
import io.searchpe.utils.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jboss.logging.Logger;
import org.wildfly.swarm.spi.runtime.annotations.ConfigurationValue;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.batch.runtime.BatchRuntime;
import javax.ejb.*;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Startup
@Singleton
public class BatchScheduler {

    private static final Logger logger = Logger.getLogger(BatchScheduler.class);

    private static final String DEFAULT_WORKING_DIRECTORY = "searchpe_working_directory";
    private static final String DEFAULT_EXECUTION_TIME = "00:00:00";
    private static final String SUNAT_ZIP_FILE_NAME = "padron_reducido_ruc.zip";
    private static final int READ_TIMEOUT = 10_000;
    private static final int CONNECTION_TIMEOUT = 10_000;

    @Resource
    private TimerService timerService;

    @Inject
    private VersionService versionService;

    @Inject
    @ConfigurationValue("searchpe.scheduler.enabled")
    private Optional<Boolean> enabled;

    @Inject
    @ConfigurationValue("searchpe.scheduler.time")
    private Optional<String> time;

    @Inject
    @ConfigurationValue("searchpe.scheduler.timeZone")
    private Optional<String> timeZone;

    @Inject
    @ConfigurationValue("searchpe.scheduler.intervalDuration")
    private Optional<Long> intervalDuration;

    @Inject
    @ConfigurationValue("searchpe.scheduler.workingDirectory")
    private String workingDirectory;

    @Inject
    @ConfigurationValue("searchpe.scheduler.sunatZipURL")
    private String sunatZipURL;

    @Inject
    @ConfigurationValue("searchpe.scheduler.maxVersions")
    private Optional<Integer> maxVersions;

    @Inject
    private SearchpeSession session;

    @PostConstruct
    public void initialize() {
        try {
            session.getTransactionManager().begin();


            Optional<Version> lastCompletedVersion = versionService.getLastCompletedVersion();
            if (!lastCompletedVersion.isPresent()) {
                initBatchExecution();
            }

            if (enabled.isPresent() && enabled.get()) {
                ZoneId zoneId = ZoneId.systemDefault();
                if (timeZone.isPresent()) {
                    zoneId = ZoneId.of(timeZone.get());
                }

                ZonedDateTime currentDateTime = ZonedDateTime.now(zoneId);
                LocalTime executionTime = LocalTime.parse(time.orElse(DEFAULT_EXECUTION_TIME));
                ZonedDateTime nextExecutionDateTime = DateUtils.getNextDate(currentDateTime, executionTime);

                Timer timer = timerService.createTimer(
                        Date.from(nextExecutionDateTime.toInstant()),
                        intervalDuration.orElse(86_400_000L),
                        null);

                long timeRemaining = timer.getTimeRemaining();
                logger.infof("Timer Next Timeout at %s", timer.getNextTimeout());
                logger.infof("Time remaining %s milliseconds [%s hours %s minutes %s seconds]",
                        timeRemaining,
                        TimeUnit.MILLISECONDS.toHours(timeRemaining),
                        TimeUnit.MILLISECONDS.toMinutes(timeRemaining),
                        TimeUnit.MILLISECONDS.toSeconds(timeRemaining)
                );
            } else {
                logger.infof("Scheduler disabled, this node will not execute schedulers");
            }


            session.getTransactionManager().commit();
        } catch (Exception e) {
            if (session.getTransactionManager().isActive()) {
                session.getTransactionManager().rollback();
            }
            throw e;
        } finally {
            session.close();
        }
    }

    @Timeout
    public void programmaticTimeout(Timer timer) {
        initBatchExecution();
    }

    private void initBatchExecution() {
        try {
            Properties batchProperties = startBatch();
            BatchRuntime.getJobOperator().start("update_database", batchProperties);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private Properties startBatch() throws IOException {
        File fileWorkingDirectory;
        if (workingDirectory != null) {
            fileWorkingDirectory = new File(workingDirectory);
        } else {
            fileWorkingDirectory = new File(DEFAULT_WORKING_DIRECTORY);
        }
        if (!fileWorkingDirectory.exists()) {
            fileWorkingDirectory.mkdir();
        }
        Path workingDirectoryPath = fileWorkingDirectory.toPath();

        // Cleaning
        logger.infof("Cleaning %s", fileWorkingDirectory.getAbsolutePath());
        org.apache.commons.io.FileUtils.cleanDirectory(fileWorkingDirectory);

        // Downloading
        File downloadedFile = workingDirectoryPath.resolve(SUNAT_ZIP_FILE_NAME).toFile();
        logger.infof("Downloading %s into %s", sunatZipURL, downloadedFile);
        org.apache.commons.io.FileUtils.copyURLToFile(new URL(sunatZipURL), downloadedFile, CONNECTION_TIMEOUT, READ_TIMEOUT);

        // Unzip
        Path unzipFolderPath = workingDirectoryPath.resolve("unzipFolder");
        FileUtils.unzipFile(downloadedFile, unzipFolderPath);

        File txtFile = null;
        File[] files = unzipFolderPath.toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                String extension = FilenameUtils.getExtension(file.getName());
                if (extension.equalsIgnoreCase("txt")) {
                    txtFile = file;
                    break;
                }
            }
        }
        if (txtFile == null) {
            throw new IllegalStateException("Could not find any *.txt file to read");
        }

        // Create version
        Version version = versionService.createNextVersion();

        // Config properties
        Properties properties = new Properties();
        properties.put("resource", txtFile.toURL().toString());
        properties.put("versionId", version.getId());
        properties.put("maxVersions", String.valueOf(maxVersions.orElse(0)));
        return properties;
    }

}
