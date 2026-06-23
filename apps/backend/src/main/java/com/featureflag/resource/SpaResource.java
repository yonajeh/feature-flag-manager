package com.featureflag.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;

/**
 * Serves the Angular SPA for non-API routes.
 */
@Path("/{path:.*}")
public class SpaResource {

    @GET
    public Response serve(@PathParam("path") String path) {
        if (path == null || path.isBlank()) {
            return index();
        }
        if (path.startsWith("api/") || path.startsWith("q/")) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        InputStream asset = getClass().getResourceAsStream("/META-INF/resources/" + path);
        if (asset != null) {
            return Response.ok(asset).build();
        }
        return index();
    }

    private Response index() {
        InputStream index = getClass().getResourceAsStream("/META-INF/resources/index.html");
        if (index == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(index).type("text/html").build();
    }
}
