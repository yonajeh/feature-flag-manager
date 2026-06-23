package com.featureflag.resource;

import com.featureflag.dto.*;
import com.featureflag.service.ApplicationService;
import com.featureflag.service.ExportService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

@Path("/api/admin/applications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("super-admin")
public class AdminApplicationResource {

    @Inject
    ApplicationService applicationService;

    @Inject
    ExportService exportService;

    @GET
    public List<ApplicationDto> list() {
        return applicationService.listAll();
    }

    @POST
    public Response create(@Valid CreateApplicationRequest request) {
        ApplicationDto created = applicationService.create(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    @Path("/{applicationId}")
    public ApplicationDto get(@PathParam("applicationId") UUID applicationId) {
        return applicationService.getById(applicationId);
    }

    @PUT
    @Path("/{applicationId}")
    public ApplicationDto update(
            @PathParam("applicationId") UUID applicationId,
            @Valid UpdateApplicationRequest request) {
        return applicationService.update(applicationId, request);
    }

    @DELETE
    @Path("/{applicationId}")
    public Response delete(@PathParam("applicationId") UUID applicationId) {
        applicationService.delete(applicationId);
        return Response.noContent().build();
    }

    @GET
    @Path("/{applicationId}/export")
    public ApplicationDataExportDto export(@PathParam("applicationId") UUID applicationId) {
        return exportService.exportApplication(applicationId);
    }
}
