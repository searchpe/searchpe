package io.searchpe.batchs.persist;

import io.searchpe.model.Company;
import io.searchpe.model.Version;
import io.searchpe.services.VersionService;
import org.jberet.support.io.JpaItemWriter;

import javax.batch.api.BatchProperty;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
public class JpaCompanyItemWriter extends JpaItemWriter {

    @Inject
    @BatchProperty
    protected String versionId;

    @Inject
    private VersionService versionService;

    @Override
    public void writeItems(List<Object> items) throws Exception {
//        if (entityTransaction) {
//            em.getTransaction().begin();
//        }
//
//        Version version = versionService.getVersion(versionId)
//                .orElseThrow(() -> new IllegalStateException("Version id[" + versionId + "] does not exists"));
//
//        for (final Object e : items) {
//            Company company = (Company) e;
//            company.setVersion(version);
//            em.persist(e);
//        }
//
//        if (entityTransaction) {
//            em.getTransaction().commit();
//        }
    }

}
