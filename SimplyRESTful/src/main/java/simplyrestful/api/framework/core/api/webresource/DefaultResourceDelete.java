package simplyrestful.api.framework.core.api.webresource;

import java.util.Optional;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import simplyrestful.api.framework.core.api.crud.DefaultDelete;
import simplyrestful.api.framework.resources.HALResource;

public interface DefaultResourceDelete<T extends HALResource> extends WebResourceBase<T>, DefaultDelete<T> {
    /**
     * Delete a resource.
     *
     * @param id is the UUID part from the entire URI identifier of the resource.
     * @return a "204 No Content" if the resource was correctly deleted.
     */
    @Path("/{id}")
    @DELETE
    @Operation(description = "Delete a single resource")
    @Consumes
    @Produces
    default Response deleteHALResource(
	    @Parameter(description = "The UUID part of the identifier for the resource", required = true)
	    @PathParam("id")
	    @NotNull
	    UUID id) {
	return Optional.ofNullable(this.delete(id)).map(resource -> Response.noContent().build())
		.orElseThrow(NotFoundException::new);
    }
}
