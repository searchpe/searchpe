package io.searchpe.controller;

import io.searchpe.api.core.IStorage;
import io.searchpe.api.core.exceptions.StorageException;
import io.searchpe.api.jpa.AbstractJpaStorage;
import io.searchpe.model.Company;
import io.searchpe.model.Version;
import io.searchpe.services.CompanyService;
import io.searchpe.services.VersionService;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CompanyControllerImpl implements CompanyController {

    private static final Logger logger = Logger.getLogger(CompanyControllerImpl.class);

    @Inject
    private IStorage storage;

    @Inject
    private VersionService versionService;

    @Inject
    private CompanyService companyService;

    @Override
    public List<Company> getCompanies(String ruc, String razonSocial, String filterText, int first, int max) {
        try {
            storage.beginTx();
            List<Company> result = Collections.emptyList();


            Version lastVersion = versionService.getLastCompletedVersion().orElseThrow(NotFoundException::new);
            if (ruc != null) {
                Optional<Company> company = companyService.getCompanyByRuc(lastVersion, ruc);
                result = new ArrayList<>();
                company.ifPresent(result::add);
            } else if (razonSocial != null) {
                result = companyService.getCompanyByRazonSocial(lastVersion, razonSocial);
            } else if (filterText != null) {
                result = companyService.getCompanyByFilterText(lastVersion, filterText, first, max);
            } else {
                throw new BadRequestException();
            }


            storage.commitTx();
            return result;

        } catch (StorageException e) {
            storage.rollbackTx();
            throw new InternalServerErrorException();
        }

    }

    public Company getCompanyById(Long id) {
        try {
            storage.beginTx();
            Company company = companyService.getCompany(id).orElseThrow(NotFoundException::new);
            storage.commitTx();
            return company;
        } catch (StorageException e) {
            storage.rollbackTx();
            throw new InternalServerErrorException();
        }
    }

}
