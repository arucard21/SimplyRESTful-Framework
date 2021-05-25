package simplyrestful.api.framework.webresource.api.implementation;

import java.net.URI;
import java.util.UUID;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import simplyrestful.api.framework.MediaTypeUtils;
import simplyrestful.api.framework.WebResourceUtils;
import simplyrestful.api.framework.api.crud.DefaultCreate;
import simplyrestful.api.framework.api.crud.DefaultExists;
import simplyrestful.api.framework.resources.HALResource;
import simplyrestful.api.framework.webresource.api.CollectionPost;

public interface DefaultCollectionPost<T extends HALResource> extends CollectionPost<T>, DefaultExists, DefaultCreate<T> {
    public static final String ERROR_RESOURCE_WITH_ID_EXISTS = "A resource with the same ID already exists. Try to update the resource with a PUT request to the URI for that resource.";

    @Operation(description = "Create a new resource which can already have a self-link containing a URI as identifier or one will be generated")
    default Response postHALResource(
            ResourceInfo resourceInfo,
            UriInfo uriInfo,
            @Parameter(description = "resource", required = true)
            T resource) {
    	UUID resourceId;
    	if(resource.getSelf() != null) {
    	    resourceId = WebResourceUtils.parseUuidFromResourceUri(resourceInfo, uriInfo, URI.create(resource.getSelf().getHref()));
    	    if (this.exists(resourceId)) {
    		throw new ClientErrorException(
    			ERROR_RESOURCE_WITH_ID_EXISTS,
    			Response.Status.CONFLICT);
    	    }
    	}
    	else {
    	    resourceId = UUID.randomUUID();
    	    resource.setSelf(
    	            WebResourceUtils.createLink(
    	                    WebResourceUtils.getAbsoluteWebResourceURI(resourceInfo, uriInfo, resourceId),
    	                    MediaTypeUtils.APPLICATION_HAL_JSON,
    	                    resource.getProfile()));
    	}
    	T updatedResource = this.create(resource, resourceId);
    	return Response.created(URI.create(updatedResource.getSelf().getHref())).build();
    }
}
