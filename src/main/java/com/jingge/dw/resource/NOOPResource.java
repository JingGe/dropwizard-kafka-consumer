package com.jingge.dw.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * DW needs at least one resource
 */

@Path("/noop")
@Produces(MediaType.APPLICATION_JSON)
public class NOOPResource {

    public NOOPResource() {
    }

    @GET
    @Path("/status")
    public Response getStatus() {
        return Response
                .ok()
                .build();
    }
}
