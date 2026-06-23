package com.featureflag.resource;

import com.featureflag.dto.*;
import com.featureflag.service.FeatureFlagService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

@Path("/api/admin/applications/{applicationId}/flags")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("super-admin")
public class AdminFeatureFlagResource {

    @Inject
    FeatureFlagService featureFlagService;

    @GET
    public List<FeatureFlagDto> list(@PathParam("applicationId") UUID applicationId) {
        return featureFlagService.listForAdmin(applicationId);
    }

    @POST
    public Response create(
            @PathParam("applicationId") UUID applicationId,
            @Valid CreateFeatureFlagRequest request) {
        FeatureFlagDto created = featureFlagService.create(applicationId, request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    @Path("/{key}")
    public FeatureFlagDto get(
            @PathParam("applicationId") UUID applicationId,
            @PathParam("key") String key) {
        return featureFlagService.getForAdmin(applicationId, key);
    }

    @PUT
    @Path("/{key}")
    public FeatureFlagDto update(
            @PathParam("applicationId") UUID applicationId,
            @PathParam("key") String key,
            @Valid UpdateFeatureFlagRequest request) {
        return featureFlagService.update(applicationId, key, request);
    }

    @DELETE
    @Path("/{key}")
    public Response delete(
            @PathParam("applicationId") UUID applicationId,
            @PathParam("key") String key) {
        featureFlagService.delete(applicationId, key);
        return Response.noContent().build();
    }
}
