package simplyrestful.api.framework.webresource.api.implementation;

import java.util.Optional;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import simplyrestful.api.framework.api.crud.DefaultDelete;
import simplyrestful.api.framework.resources.APIResource;

public interface DefaultResourceDelete<T extends APIResource> extends DefaultDelete<T> {
	/**
     * Delete a resource.
     *
     * @param id is the UUID part from the entire URI identifier of the resource.
     * @return a "204 No Content" if the resource was correctly deleted.
     */
    @Path("/{id}")
    @DELETE
    @Operation(description = "Delete this API resource")
    default Response deleteHALResource(
    		@PathParam("id")
    	    @NotNull
    	    @Parameter(description = "The UUID part of the identifier for the resource", required = true)
            UUID id) {
        return Optional.ofNullable(this.delete(id))
                .map(resource -> Response.noContent().build())
                .orElseThrow(NotFoundException::new);
    }
}
