package simplyrestful.api.framework.webresource.api;

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import simplyrestful.api.framework.resources.HALResource;

public interface ResourcePut<T extends HALResource> {
    /**
     * Update a resource (or create it with the given identifier)
     * <p>
     * The resource may contain a self-link. This self-link must match the
     * provided id. If a resource with that id does not exist yet, a "404 Not found"
     * error is returned.
     * </p>
     * @param id is the UUID part from the entire URI identifier of the resource.
     * @param resource is the updated resource.
     * @return "200 OK" if the resource was created, "404 Not Found" if the resource
     * does not exist yet.
     */
    @Path("/{id}")
    @PUT
    Response putHALResource(
            @Context
            ResourceInfo resourceInfo,
    	    @Context
    	    UriInfo uriInfo,
    	    @PathParam("id")
    	    @NotNull
    	    UUID id,
    	    @NotNull
    	    @Valid
    	    T resource);
}
