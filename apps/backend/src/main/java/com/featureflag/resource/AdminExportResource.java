package com.featureflag.resource;

import com.featureflag.dto.FullDataExportDto;
import com.featureflag.service.ExportService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/admin/export")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("super-admin")
public class AdminExportResource {

    @Inject
    ExportService exportService;

    @GET
    public FullDataExportDto exportAll() {
        return exportService.exportAll();
    }
}
