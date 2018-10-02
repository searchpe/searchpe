package io.searchpe.repository;

import io.searchpe.model.Company;
import io.searchpe.model.Version;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CompanyRepositoryImpl extends AbstractRepository implements CompanyRepository {

    @Override
    public Company save(Company company) {
        if (company.getId() != null) {
            getEntityManager().persist(company);
        } else {
            getEntityManager().merge(company);
        }
        return company;
    }

    @Override
    public Optional<Company> getCompany(long id) {
        Company company = getEntityManager().find(Company.class, id);
        return Optional.ofNullable(company);
    }

    @Override
    public Optional<Company> getCompanyByRuc(Version version, String ruc) {
        TypedQuery<Company> query = getEntityManager().createNamedQuery("getCompaniesByVersionIdAndRuc", Company.class);
        query.setParameter("versionId", version.getId());
        query.setParameter("ruc", ruc);

        List<Company> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return Optional.empty();
        } else if (resultList.size() == 1) {
            return Optional.of(resultList.get(0));
        } else {
            throw new IllegalStateException("More than one result");
        }
    }

    @Override
    public List<Company> getCompanyByRazonSocial(Version version, String razonSocial) {
        TypedQuery<Company> query = getEntityManager().createNamedQuery("getCompaniesByVersionIdAndRazonSocial", Company.class);
        query.setParameter("versionId", version.getId());
        query.setParameter("razonSocial", "%" + razonSocial.toLowerCase());

        return query.getResultList();
    }

    @Override
    public List<Company> getCompanyByFilterText(Version version, String filterText) {
        return getCompanyByFilterText(version, filterText, -1, -1);
    }

    @Override
    public List<Company> getCompanyByFilterText(Version version, String filterText, int first, int max) {
        TypedQuery<Company> query = getEntityManager().createNamedQuery("getCompaniesByVersionIdAndFilterText", Company.class);
        query.setParameter("versionId", version.getId());
        query.setParameter("filterText", "%" + filterText.toLowerCase() + "%");
        if(first != -1) {
            query.setFirstResult(first);
        }
        if (max != -1) {
            query.setMaxResults(max);
        }
        return query.getResultList();
    }

}
