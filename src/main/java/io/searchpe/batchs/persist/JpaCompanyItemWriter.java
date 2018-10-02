package io.searchpe.batchs.persist;

import io.searchpe.api.provider.SearchpeSession;
import io.searchpe.model.Company;
import io.searchpe.model.Version;
import io.searchpe.services.VersionService;
import io.searchpe.utils.DateUtils;
import org.jberet.support.io.JpaItemWriter;

import javax.batch.api.BatchProperty;
import javax.ejb.Timer;
import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Named
public class JpaCompanyItemWriter extends JpaItemWriter {

    @Inject
    @BatchProperty
    protected String versionId;

    @Inject
    private VersionService versionService;

    @Inject
    private SearchpeSession session;

    @Override
    public void writeItems(List<Object> items) throws Exception {
        if (entityTransaction) {
            em.getTransaction().begin();
        }

        Version version = versionService.getVersion(versionId)
                .orElseThrow(() -> new IllegalStateException("Version id[" + versionId + "] does not exists"));

        for (final Object e : items) {
            Company company = (Company) e;
            company.setVersion(version);
            em.persist(e);
        }

        if (entityTransaction) {
            em.getTransaction().commit();
        }
    }

}
