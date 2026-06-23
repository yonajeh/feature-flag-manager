package com.featureflag.resource;

import com.featureflag.dto.LoginRequest;
import com.featureflag.dto.LoginResponse;
import com.featureflag.service.AdminAuthService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/api/admin/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminAuthResource {

    @Inject
    AdminAuthService adminAuthService;

    @POST
    @Path("/login")
    @PermitAll
    public LoginResponse login(@Valid LoginRequest request) {
        return adminAuthService.login(request);
    }
}
