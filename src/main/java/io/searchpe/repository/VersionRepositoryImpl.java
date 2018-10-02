package io.searchpe.repository;

import io.searchpe.model.Version;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;
import java.util.function.Function;

@ApplicationScoped
public class VersionRepositoryImpl extends AbstractRepository implements VersionRepository {

    private final static Function<String, String> fieldsFunction = VersionRepositoryImpl::fieldName;

    @Inject
    private EntityManager em;

    private static String fieldName(String fieldName) {
        return fieldName;
    }

    @Override
    public Version createVersion(Version version) {
        getEntityManager().persist(version);
        getEntityManager().flush();
        return version;
    }

    @Override
    public Optional<Version> getVersion(String id) {
        return Optional.ofNullable(getEntityManager().find(Version.class, id));
    }

    @Override
    public Optional<Version> getLastVersion() {
        TypedQuery<Version> query = getEntityManager().createNamedQuery("getVersions", Version.class);
        query.setMaxResults(1);
        List<Version> resultList = query.getResultList();
        if (resultList.size() == 1) {
            return Optional.of(resultList.get(0));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Version> getLastCompletedVersion() {
        TypedQuery<Version> query = getEntityManager().createNamedQuery("getVersionsByCompleteStatus", Version.class);
        query.setParameter("complete", true);
        query.setMaxResults(1);
        List<Version> resultList = query.getResultList();
        if (resultList.size() == 1) {
            return Optional.of(resultList.get(0));
        }
        return Optional.empty();
    }

    @Override
    public List<Version> getCompleteVersionsBefore(Date date) {
        TypedQuery<Version> query = getEntityManager().createNamedQuery("getCompleteVersionsBefore", Version.class);
        query.setParameter("complete", true);
        query.setParameter("date", date);
        return query.getResultList();
    }

    @Override
    public List<Version> getCompleteVersionsDesc(int skip) {
        TypedQuery<Version> query = getEntityManager().createNamedQuery("getCompleteVersionsDesc", Version.class);
        query.setParameter("complete", true);
        if (skip != -1) {
            query.setFirstResult(skip);
        }
        return query.getResultList();
    }

    @Override
    public boolean deleteVersion(Version version) {
        getEntityManager().createNamedQuery("deleteCompaniesByVersionId")
                .setParameter("versionId", version.getId())
                .executeUpdate();
        getEntityManager().remove(version);
        return true;
    }

    @Override
    public List<Version> getVersionsByParameters(Map<String, Object> parameters) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Version> query = criteriaBuilder.createQuery(Version.class);
        Root<Version> root = query.from(Version.class);

        List<Predicate> predicates = new ArrayList<>();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            String fieldName = fieldsFunction.apply(key);
            Predicate predicate = criteriaBuilder.equal(root.get(fieldName), value);
            predicates.add(predicate);
        }

        CriteriaQuery<Version> criteriaQuery = query.select(root);
        criteriaQuery.where(predicates.toArray(new Predicate[]{}));

        TypedQuery<Version> typedQuery = getEntityManager().createQuery(criteriaQuery);
        return typedQuery.getResultList();
    }

    @Override
    public Version updateVersion(Version version) {
        getEntityManager().merge(version);
        return version;
    }
}
