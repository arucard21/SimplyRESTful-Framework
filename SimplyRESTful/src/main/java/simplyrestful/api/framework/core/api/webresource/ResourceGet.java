package simplyrestful.api.framework.core.api.webresource;

import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import simplyrestful.api.framework.core.api.crud.DefaultRead;
import simplyrestful.api.framework.resources.HALResource;

public interface ResourceGet<T extends HALResource> extends DefaultRead<T> {
    /**
     * Retrieve a resource.
     *
     * @param resourceInfo is a JAX-RS context object.
     * @param uriInfo is a JAX-RS context object.
     * @param httpHeaders is a JAX-RS context object.
     * @param id is the UUID part from the entire URI identifier of the resource.
     * @return the requested resource.
     */
    @Path("/{id}")
    @GET
    T getHALResource(
	    @Context
	    ResourceInfo resourceInfo,
	    @Context
	    UriInfo uriInfo,
	    @Context
	    HttpHeaders httpHeaders,
	    @PathParam("id")
	    @NotNull
	    UUID id);
}
