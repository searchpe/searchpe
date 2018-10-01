package io.searchpe.admin.client.resource;

import io.searchpe.model.Company;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import java.util.List;

@Path("/companies")
@Api(value = "/companies", description = "Companies")
public interface CompanyResource {

    @GET
    @Path("/")
    @Produces("application/json")
    @ApiOperation(
            value = "Get companies",
            notes = "Returns companies that match criteria",
            response = Company.class,
            responseContainer = "List"
    )
    List<Company> getCompanies(
            @QueryParam("ruc") String ruc,
            @QueryParam("razonSocial") String razonSocial,
            @QueryParam("filterText") String filterText,
            @QueryParam("first") @DefaultValue("0") int first,
            @QueryParam("max") @DefaultValue("10") int max
    );

    @GET
    @Path("/{id}")
    @Produces("application/json")
    @ApiOperation(
            value = "Get companies",
            notes = "Returns a company",
            response = Company.class
    )
    Company getCompanyById(@PathParam("id") Long id);

}
