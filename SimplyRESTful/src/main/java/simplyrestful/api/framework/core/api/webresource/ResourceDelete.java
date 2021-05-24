package simplyrestful.api.framework.core.api.webresource;

import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import simplyrestful.api.framework.resources.HALResource;

public interface ResourceDelete<T extends HALResource> {
    /**
     * Delete a resource.
     *
     * @param id is the UUID part from the entire URI identifier of the resource.
     * @return a "204 No Content" if the resource was correctly deleted.
     */
    @Path("/{id}")
    @DELETE
    Response deleteHALResource(
	    @PathParam("id")
	    @NotNull
	    UUID id);
}
