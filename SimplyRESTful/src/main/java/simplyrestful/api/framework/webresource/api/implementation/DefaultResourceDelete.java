package simplyrestful.api.framework.webresource.api.implementation;

import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import simplyrestful.api.framework.api.crud.DefaultDelete;
import simplyrestful.api.framework.resources.HALResource;
import simplyrestful.api.framework.webresource.api.ResourceDelete;

public interface DefaultResourceDelete<T extends HALResource> extends ResourceDelete<T>, DefaultDelete<T> {
    @Operation(description = "Delete a single resource")
    default Response deleteHALResource(
            @Parameter(description = "The UUID part of the identifier for the resource", required = true)
            UUID id) {
        return Optional.ofNullable(this.delete(id))
                .map(resource -> Response.noContent().build())
                .orElseThrow(NotFoundException::new);
    }
}
