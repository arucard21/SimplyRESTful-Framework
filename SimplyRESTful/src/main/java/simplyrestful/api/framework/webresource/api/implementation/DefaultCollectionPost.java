package simplyrestful.api.framework.webresource.api.implementation;

import java.net.URI;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import simplyrestful.api.framework.MediaTypeUtils;
import simplyrestful.api.framework.WebResourceUtils;
import simplyrestful.api.framework.api.crud.DefaultCreate;
import simplyrestful.api.framework.api.crud.DefaultExists;
import simplyrestful.api.framework.resources.HALResource;

public interface DefaultCollectionPost<T extends HALResource> extends DefaultExists, DefaultCreate<T> {
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
    @Operation(description = "Create a new resource which can already have a self-link containing a URI as identifier or one will be generated")
    default Response postHALResource(
    		@Context
            ResourceInfo resourceInfo,
            @Context
            UriInfo uriInfo,
            @NotNull
            @Valid
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
