package simplyrestful.api.framework.core.api.webresource;

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import simplyrestful.api.framework.core.api.crud.DefaultExists;
import simplyrestful.api.framework.core.api.crud.DefaultUpdate;
import simplyrestful.api.framework.resources.HALResource;

public interface DefaultResourcePut<T extends HALResource> extends WebResourceBase<T>, DefaultExists, DefaultUpdate<T> {
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
    @Operation(description = "Create a resource with a specified ID or update that resource. Returns a 201 HTTP status with the UUID of the resource in the Location header, if a new one was created. Otherwise it just returns 200 OK.")
    @Produces
    default Response putHALResource(
	    @Context
	    UriInfo uriInfo,
	    @Parameter(description = "The UUID part of the identifier for the resource", required = true)
	    @PathParam("id")
	    @NotNull
	    UUID id,
	    @Parameter(description = "The resource to be updated", required = true)
	    @NotNull
	    @Valid
	    T resource) {
	if (!this.exists(id)) {
	    throw new NotFoundException(ERROR_RESOURCE_WITH_ID_NOT_EXISTS);
	}
	ensureSelfLinkValid(uriInfo, resource, id);
	this.update(resource, id);
	return Response.ok().build();
    }
}
