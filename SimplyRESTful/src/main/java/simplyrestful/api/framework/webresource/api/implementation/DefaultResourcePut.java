package simplyrestful.api.framework.webresource.api.implementation;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import simplyrestful.api.framework.api.crud.ResourceExists;
import simplyrestful.api.framework.api.crud.ResourceUpdate;
import simplyrestful.api.framework.resources.ApiResource;
import simplyrestful.api.framework.utils.WebResourceUtils;

/**
 * Provide a default implementation for updating the API resource.
 *
 * @param <T> is the API resource type used in the JAX-RS WebResource.
 */
public interface DefaultResourcePut<T extends ApiResource> extends ResourceExists, ResourceUpdate<T> {
	/**
	 * The error message that is returned when trying to update an API resource without providing a self link.
	 */
	public static final String ERROR_SELF_LINK_MUST_BE_PROVIDED = "The provided resource must contain a self-link with the absolute URL to itself when updating an existing resource.";
	/**
	 * The error message that is returned when trying to update an API resource with an ID in the request URI that does not correspond to any existing resource.
	 */
	public static final String ERROR_RESOURCE_WITH_ID_NOT_EXISTS = "A resource with the provided ID does not exist. You can create the resource with a POST request to the collection URI.";
    /**
     * The error message that is returned when trying to update an API resource with a self link URI that does not match the request URI (e.g. in the path preceding the ID or its host name).
     */
    public static final String ERROR_SELF_LINK_URI_DOES_NOT_MATCH_API_BASE_URI = "The identifier of the resource does not correspond to the base URI of this Web Resource";
    /**
     * The error message that is returned when trying to update an API resource with a self link that does not match the ID in the request URI.
     */
    public static final String ERROR_SELF_LINK_ID_DOES_NOT_MATCH_PROVIDED_ID = "The provided resource contains an self-link that does not match the ID used in the request";

    /**
     * Update a resource.
     * <p>
     * The resource must contain a self-link. If it is not provided, a "400 Bad Request" error
     * is returned. If the resource with the given ID does not exist, a "404 Not Found" error is
     * returned. The self-link URI must match the API base URI and the ID in the self-link must
     * match the ID provided in the request URI. If either does not match, a "400 Bad Request"
     * error is returned.
     * </p>
     * @param id is the UUID part from the entire URI identifier of the resource.
     * @param resource is the updated resource.
     * @return "204 No Content" if the resource was updated successfully.
     */
    @Path("/{id}")
    @PUT
    @Operation(description = "Modify an existing API resource.")
	@ApiResponse(
    		responseCode = "204",
    		description = "The API resource was successfully modified")
	@ApiResponse(
    		responseCode = "400",
    		description = "The self-link in the API resource is missing, does not match the base URI of the Web Resource, or does not match the ID used in the request.")
	@ApiResponse(
    		responseCode = "404",
    		description = "A resource with the provided ID does not exist.")
    default Response putAPIResource(
    		@PathParam("id")
    	    @NotNull
    	    @Parameter(description = "The UUID part of the identifier for the resource", required = true)
    	    UUID id,
    	    @NotNull
    	    @Valid
    	    @Parameter(required = true)
            T resource) {
    	if(resource.self() == null) {
    		throw new BadRequestException(ERROR_SELF_LINK_MUST_BE_PROVIDED);
    	}
    	if (!this.exists(id)) {
    	    throw new NotFoundException(ERROR_RESOURCE_WITH_ID_NOT_EXISTS);
    	}
		UUID resourceIdFromSelf = WebResourceUtils.parseUuidFromLastSegmentOfUri(resource.self().getHref());
		if (resourceIdFromSelf == null) {
			throw new BadRequestException(ERROR_SELF_LINK_URI_DOES_NOT_MATCH_API_BASE_URI);
		}
		if (!resourceIdFromSelf.equals(id)) {
			throw new BadRequestException(ERROR_SELF_LINK_ID_DOES_NOT_MATCH_PROVIDED_ID);
		}
    	this.update(resource);
    	return Response.noContent().build();
    }
}
