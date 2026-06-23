package com.featureflag.resource;

import com.featureflag.dto.*;
import com.featureflag.service.TokenService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

@Path("/api/admin/applications/{applicationId}/tokens")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("super-admin")
public class AdminTokenResource {

    @Inject
    TokenService tokenService;

    @GET
    public List<ApiTokenMetadataDto> list(@PathParam("applicationId") UUID applicationId) {
        return tokenService.listTokens(applicationId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response generate(
            @PathParam("applicationId") UUID applicationId,
            GenerateTokenRequest request) {
        ApiTokenCreatedDto created = tokenService.generateToken(
                applicationId,
                request != null ? request.expiresAt() : null);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @POST
    @Path("/{tokenId}/rotate")
    public Response rotate(
            @PathParam("applicationId") UUID applicationId,
            @PathParam("tokenId") UUID tokenId) {
        ApiTokenCreatedDto created = tokenService.rotateToken(applicationId, tokenId);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @POST
    @Path("/{tokenId}/revoke")
    public ApiTokenMetadataDto revoke(
            @PathParam("applicationId") UUID applicationId,
            @PathParam("tokenId") UUID tokenId) {
        return tokenService.revokeToken(applicationId, tokenId);
    }
}
