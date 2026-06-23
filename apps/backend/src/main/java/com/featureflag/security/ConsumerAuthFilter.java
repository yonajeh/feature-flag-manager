package com.featureflag.security;

import com.featureflag.exception.UnauthorizedException;
import com.featureflag.service.TokenService;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
@ConsumerAuth
@Priority(Priorities.AUTHENTICATION)
public class ConsumerAuthFilter implements ContainerRequestFilter {

    public static final String AUTH_CONTEXT_PROPERTY = "authenticatedApplication";

    @Inject
    TokenService tokenService;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String appName = requestContext.getHeaderString("X-App-Name");
        String appToken = requestContext.getHeaderString("X-App-Token");
        try {
            AuthenticatedApplication auth = tokenService.authenticate(appName, appToken);
            requestContext.setProperty(AUTH_CONTEXT_PROPERTY, auth);
        } catch (UnauthorizedException e) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new com.featureflag.dto.ErrorResponse("UNAUTHORIZED", e.getMessage()))
                    .build());
        }
    }
}
