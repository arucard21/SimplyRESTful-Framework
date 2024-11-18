package simplyrestful.api.framework.webresource.api.implementation;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import simplyrestful.api.framework.api.crud.DefaultCreate;
import simplyrestful.api.framework.api.crud.DefaultExists;
import simplyrestful.api.framework.resources.ApiResource;
import simplyrestful.api.framework.resources.Link;
import simplyrestful.api.framework.utils.WebResourceUtils;

public interface DefaultCollectionPost<T extends ApiResource> extends DefaultExists, DefaultCreate<T> {
    public static final String ERROR_RESOURCE_WITH_ID_EXISTS = "A resource with the same ID already exists. Try to update the resource with a PUT request to the URI for that resource.";

    /**
     * Create a resource.
     *
     * @param resource is a resource that should be created.
     * @return a "201 Created" response for the resource that was created,
     *         containing its URI identifier in the Location header, if the resource
     *         was correctly created. A "409 Conflict" response is returned if the
     *         resource contains an self-link that refers to an existing resource.
     */
    @POST
    @Operation(description = "Create a new API resource which can already have a self-link containing a URI as identifier or one will be generated")
	@ApiResponse(
    		responseCode = "201",
    		description = "Provides the location of the newly created API resource.",
    		headers = {
    				@Header(
    						name = HttpHeaders.LOCATION,
    						description = "Contains the URI to the newly created API resource",
    						schema = @Schema(type = "string", format = "uri"))})
    @ApiResponse(
    		responseCode = "409",
    		description = "The self-link in the API resource conflicts with an existing API resource so it could not be created")
	default Response postAPIResource(
    		@Context
            ResourceInfo resourceInfo,
            @Context
            UriInfo uriInfo,
            @NotNull
            @Valid
            @Parameter(required = true)
            T resource) {
    	UUID resourceId;
    	if(resource.getSelf() != null) {
    	    resourceId = WebResourceUtils.parseUuidFromResourceUri(resourceInfo, uriInfo, resource.getSelf().getHref());
    	    if (this.exists(resourceId)) {
    	    	throw new ClientErrorException(ERROR_RESOURCE_WITH_ID_EXISTS, Response.Status.CONFLICT);
    	    }
    	}
    	else {
    	    resourceId = UUID.randomUUID();
    	    resource.setSelf(new Link(
    	    		WebResourceUtils.getAbsoluteWebResourceURI(resourceInfo, uriInfo, resourceId),
    	    		resource.customJsonMediaType()));
    	}
    	T updatedResource = this.create(resource, resourceId);
    	return Response.created(updatedResource.getSelf().getHref()).build();
    }
}
