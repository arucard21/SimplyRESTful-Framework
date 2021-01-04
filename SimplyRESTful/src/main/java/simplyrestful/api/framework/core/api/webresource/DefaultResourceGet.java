package simplyrestful.api.framework.core.api.webresource;

import java.util.Optional;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import simplyrestful.api.framework.core.api.crud.DefaultRead;
import simplyrestful.api.framework.resources.HALResource;

public interface DefaultResourceGet<T extends HALResource> extends WebResourceBase<T>, DefaultRead<T> {
    /**
     * Retrieve a resource.
     *
     * @param id is the UUID part from the entire URI identifier of the resource.
     * @return the requested resource.
     */
    @Path("/{id}")
    @GET
    @Operation(description = "Retrieve a single resource")
    @Consumes
    default T getHALResource(
	    @Parameter(description = "The identifier for the resource", required = true) @PathParam("id") @NotNull UUID id) {
	return Optional.ofNullable(this.read(id)).orElseThrow(NotFoundException::new);
    }
}
