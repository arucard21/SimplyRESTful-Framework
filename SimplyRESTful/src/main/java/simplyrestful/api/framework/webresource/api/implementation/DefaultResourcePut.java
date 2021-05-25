package simplyrestful.api.framework.webresource.api.implementation;

import java.net.URI;
import java.util.UUID;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import simplyrestful.api.framework.MediaTypeUtils;
import simplyrestful.api.framework.WebResourceUtils;
import simplyrestful.api.framework.api.crud.DefaultExists;
import simplyrestful.api.framework.api.crud.DefaultUpdate;
import simplyrestful.api.framework.resources.HALResource;
import simplyrestful.api.framework.webresource.api.ResourcePut;

public interface DefaultResourcePut<T extends HALResource> extends ResourcePut<T>, DefaultExists, DefaultUpdate<T> {
    public static final String ERROR_SELF_LINK_ID_DOES_NOT_MATCH_PROVIDED_ID = "The provided resource contains an self-link that does not match the ID used in the request";
    public static final String ERROR_SELF_LINK_URI_DOES_NOT_MATCH_API_BASE_URI = "The identifier of the resource does not correspond to the base URI of this Web Resource";
    public static final String ERROR_RESOURCE_WITH_ID_NOT_EXISTS = "A resource with the provided ID does not exist. Try to create the resource with a POST request to the collection URI.";
    @Operation(description = "Create a resource with a specified ID or update that resource. Returns a 201 HTTP status with the UUID of the resource in the Location header, if a new one was created. Otherwise it just returns 200 OK.")
    default Response putHALResource(
            ResourceInfo resourceInfo,
            UriInfo uriInfo,
            @Parameter(description = "The UUID part of the identifier for the resource", required = true)
            UUID id,
            @Parameter(description = "The resource to be updated", required = true)
            T resource) {
    	if (!this.exists(id)) {
    	    throw new NotFoundException(ERROR_RESOURCE_WITH_ID_NOT_EXISTS);
    	}
    	if(resource.getSelf() == null) {
    	    resource.setSelf(
    	            WebResourceUtils.createLink(
    	                    WebResourceUtils.getAbsoluteWebResourceURI(resourceInfo, uriInfo, id),
    	                    MediaTypeUtils.APPLICATION_HAL_JSON,
    	                    resource.getProfile()));
    	}
    	else {
    	    UUID resourceIdFromSelf = WebResourceUtils.parseUuidFromResourceUri(resourceInfo, uriInfo, URI.create(resource.getSelf().getHref()));
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
