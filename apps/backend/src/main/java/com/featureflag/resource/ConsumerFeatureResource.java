package com.featureflag.resource;

import com.featureflag.dto.ConsumerFeatureFlagDto;
import com.featureflag.security.AuthenticatedApplication;
import com.featureflag.security.ConsumerAuth;
import com.featureflag.security.ConsumerAuthFilter;
import com.featureflag.service.FeatureFlagService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/api/v1/features")
@Produces(MediaType.APPLICATION_JSON)
@ConsumerAuth
public class ConsumerFeatureResource {

    @Inject
    FeatureFlagService featureFlagService;

    @GET
    public List<ConsumerFeatureFlagDto> list(@Context jakarta.ws.rs.container.ContainerRequestContext ctx) {
        AuthenticatedApplication auth = (AuthenticatedApplication) ctx.getProperty(ConsumerAuthFilter.AUTH_CONTEXT_PROPERTY);
        return featureFlagService.listForConsumer(auth.getApplicationId());
    }

    @GET
    @Path("/{key}")
    public ConsumerFeatureFlagDto get(
            @Context jakarta.ws.rs.container.ContainerRequestContext ctx,
            @PathParam("key") String key) {
        AuthenticatedApplication auth = (AuthenticatedApplication) ctx.getProperty(ConsumerAuthFilter.AUTH_CONTEXT_PROPERTY);
        return featureFlagService.getForConsumer(auth.getApplicationId(), key);
    }
}
