package simplyrestful.api.framework.webresource.api;

import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import simplyrestful.api.framework.resources.HALResource;

public interface ResourceGet<T extends HALResource> {
    public static final String QUERY_PARAM_FIELDS_DEFAULT = "all";

    /**
     * Retrieve a resource.
     *
     * @param requestContext is a JAX-RS context object.
     * @param resourceInfo is a JAX-RS context object.
     * @param uriInfo is a JAX-RS context object.
     * @param httpHeaders is a JAX-RS context object.
     * @param id is the UUID part from the entire URI identifier of the resource.
     * @return the requested resource.
     */
    @Path("/{id}")
    @GET
    T getHALResource(
    		@Context
    		ContainerRequestContext requestContext,
		    @Context
		    ResourceInfo resourceInfo,
		    @Context
		    UriInfo uriInfo,
		    @Context
		    HttpHeaders httpHeaders,
		    @PathParam("id")
		    @NotNull
		    UUID id,
		    @QueryParam(CollectionGet.QUERY_PARAM_FIELDS)
	        @DefaultValue(QUERY_PARAM_FIELDS_DEFAULT)
	        List<String> fields);
}
