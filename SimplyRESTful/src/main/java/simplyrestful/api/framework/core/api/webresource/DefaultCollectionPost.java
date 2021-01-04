package simplyrestful.api.framework.core.api.webresource;

import java.net.URI;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import simplyrestful.api.framework.core.api.crud.DefaultCreate;
import simplyrestful.api.framework.core.api.crud.DefaultExists;
import simplyrestful.api.framework.resources.HALResource;

public interface DefaultCollectionPost<T extends HALResource> extends WebResourceBase<T>, DefaultExists, DefaultCreate<T> {
    /**
     * Create a resource.
     *
     * @param resource is a resource that should be created.
     * @return a "201 Created" response for the resource that was created,
     *         containing its URI identifier in the Location header, if the resource
     *         was correctly created.
     */
    @POST
    @Operation(description = "Create a new resource which can already have a self-link containing a URI as identifier or one will be generated")
    @Produces
    default Response postHALResource(
	    @Context
	    UriInfo uriInfo,
	    @Parameter(description = "resource", required = true)
	    @NotNull
	    @Valid
	    T resource) {
	UUID resourceId = ensureSelfLinkValid(uriInfo, resource, null);
	if (this.exists(resourceId)) {
	    throw new ClientErrorException(
		    ERROR_RESOURCE_WITH_ID_EXISTS,
		    Response.Status.CONFLICT);
	}
	T updatedResource = this.create(resource, resourceId);
	return Response.created(URI.create(updatedResource.getSelf().getHref())).build();
    }
}
