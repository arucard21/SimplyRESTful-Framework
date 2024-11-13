package simplyrestful.api.framework.webresource.api.implementation;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import simplyrestful.api.framework.api.crud.DefaultExists;
import simplyrestful.api.framework.api.crud.DefaultUpdate;
import simplyrestful.api.framework.resources.APIResource;
import simplyrestful.api.framework.resources.Link;
import simplyrestful.api.framework.utils.WebResourceUtils;

public interface DefaultResourcePut<T extends APIResource> extends DefaultExists, DefaultUpdate<T> {
    public static final String ERROR_SELF_LINK_ID_DOES_NOT_MATCH_PROVIDED_ID = "The provided resource contains an self-link that does not match the ID used in the request";
    public static final String ERROR_SELF_LINK_URI_DOES_NOT_MATCH_API_BASE_URI = "The identifier of the resource does not correspond to the base URI of this Web Resource";
    public static final String ERROR_RESOURCE_WITH_ID_NOT_EXISTS = "A resource with the provided ID does not exist. Try to create the resource with a POST request to the collection URI.";

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
    @Operation(description = "Modify an existing API resource.")
    default Response putAPIResource(
    		@Context
            ResourceInfo resourceInfo,
            @Context
            UriInfo uriInfo,
    		@PathParam("id")
    	    @NotNull
    	    @Parameter(description = "The UUID part of the identifier for the resource", required = true)
    	    UUID id,
    	    @NotNull
    	    @Valid
    	    @Parameter(required = true)
            T resource) {
    	if (!this.exists(id)) {
    	    throw new NotFoundException(ERROR_RESOURCE_WITH_ID_NOT_EXISTS);
    	}
    	if(resource.getSelf() == null) {
    	    resource.setSelf(new Link(
    	                    WebResourceUtils.getAbsoluteWebResourceURI(resourceInfo, uriInfo, id),
    	                    resource.customJsonMediaType()));
    	}
    	else {
    	    UUID resourceIdFromSelf = WebResourceUtils.parseUuidFromResourceUri(resourceInfo, uriInfo, resource.getSelf().getHref());
    	    if (resourceIdFromSelf == null) {
                    throw new BadRequestException(ERROR_SELF_LINK_URI_DOES_NOT_MATCH_API_BASE_URI);
                }
                if (!resourceIdFromSelf.equals(id)) {
                    throw new BadRequestException(ERROR_SELF_LINK_ID_DOES_NOT_MATCH_PROVIDED_ID);
                }
    	}
    	this.update(resource, id);
    	return Response.ok().build();
    }
}
