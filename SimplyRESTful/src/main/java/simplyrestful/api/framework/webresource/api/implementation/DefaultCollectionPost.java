package simplyrestful.api.framework.webresource.api.implementation;

import java.net.URI;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import simplyrestful.api.framework.api.crud.ResourceCreate;
import simplyrestful.api.framework.api.crud.ResourceExists;
import simplyrestful.api.framework.resources.ApiResource;

/**
 * Provide a default implementation for creating an API resource.
 *
 * @param <T> is the API resource type used in the JAX-RS WebResource.
 */
public interface DefaultCollectionPost<T extends ApiResource> extends ResourceExists, ResourceCreate<T> {
	/**
	 * The error message that is returned when trying to create an API resource with a self link.
	 */
    public static final String ERROR_RESOURCE_SELF_LINK_NOT_ALLOWED = "The resource contains a self link which is not allowed when creating a new resource.";
    public static final String ERROR_CREATED_RESOURCE_HAS_NO_SELF_LINK = "The created resource does not contain a self link with the absolute URL to itself.";

    /**
     * Create a resource.
     *
     * @param resource is a resource that should be created.
     * @return a "201 Created" response for the resource that was created,
     *         containing its URI identifier in the Location header, if the resource
     *         was correctly created. A "400 Bad Request" response is returned if the
     *         resource contains a self-link. A "500 Internal Server Error" response
     *         is returned if the created resource does not contain a self-link.
     */
    @POST
    @Operation(description = "Create a new API resource which must not contain a self-link")
	@ApiResponse(
    		responseCode = "201",
    		description = "Provides the location of the newly created API resource.",
    		headers = {
    				@Header(
    						name = HttpHeaders.LOCATION,
    						description = "Contains the URI to the newly created API resource",
    						schema = @Schema(type = "string", format = "uri"))})
    @ApiResponse(
    		responseCode = "400",
    		description = "The API resource contains a self-link which is not allowed when creating a new resource.")
    @ApiResponse(
    		responseCode = "500",
    		description = "The created resource does not contain a self-link.")
	default Response postAPIResource(
    		@NotNull
            @Valid
            @Parameter(required = true)
            T resource) {
    	UUID resourceId = null;
    	if(resource.self() != null) {
    		throw new BadRequestException(ERROR_RESOURCE_SELF_LINK_NOT_ALLOWED);
    	}
    	T updatedResource = this.create(resource, resourceId);
    	if(updatedResource.self() == null || updatedResource.self().getHref() == null) {
			throw new IllegalStateException(ERROR_CREATED_RESOURCE_HAS_NO_SELF_LINK);
		}
    	URI resourceLocation = updatedResource.self().getHref();
    	return Response.created(resourceLocation).build();
    }
}
