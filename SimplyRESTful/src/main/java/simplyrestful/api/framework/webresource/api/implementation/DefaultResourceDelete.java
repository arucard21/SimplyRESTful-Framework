package simplyrestful.api.framework.webresource.api.implementation;

import java.util.Optional;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
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
    @ApiResponse(
    		responseCode = "204",
    		description = "The API resource is successfully deleted")
    default Response deleteAPIResource(
    		@PathParam("id")
    	    @NotNull
    	    @Parameter(description = "The UUID part of the identifier for the resource", required = true)
            UUID id) {
        return Optional.ofNullable(this.delete(id))
                .map(resource -> Response.noContent().build())
                .orElseThrow(NotFoundException::new);
    }
}
