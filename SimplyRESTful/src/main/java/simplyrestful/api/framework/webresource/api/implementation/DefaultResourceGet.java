package simplyrestful.api.framework.webresource.api.implementation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import simplyrestful.api.framework.MediaTypeUtils;
import simplyrestful.api.framework.QueryParamUtils;
import simplyrestful.api.framework.WebResourceUtils;
import simplyrestful.api.framework.api.crud.DefaultRead;
import simplyrestful.api.framework.resources.HALResource;

public interface DefaultResourceGet<T extends HALResource> extends DefaultRead<T> {
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
    @Operation(description = "Retrieve a single resource")
    default T getHALResource(
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
		    @Parameter(description = "The identifier for the resource", required = true)
		    UUID id,
		    @QueryParam(DefaultCollectionGet.QUERY_PARAM_FIELDS)
	        @DefaultValue(QUERY_PARAM_FIELDS_DEFAULT)
	        @Parameter(description = "The fields that should be retrieved", required = false)
	        List<String> fields) {
    	QueryParamUtils.configureFieldsDefault(requestContext, fields);
    	T resource = Optional.ofNullable(this.read(id)).orElseThrow(NotFoundException::new);
        MediaType selected = MediaTypeUtils.selectMediaType(resourceInfo, httpHeaders);
        if (MediaTypeUtils.APPLICATION_HAL_JSON_TYPE.isCompatible(selected)) {
            resource.setSelf(
                    WebResourceUtils.createLink(
                            WebResourceUtils.getAbsoluteWebResourceURI(resourceInfo, uriInfo, id),
                            MediaTypeUtils.APPLICATION_HAL_JSON,
                            resource.getProfile()));
        } else {
            resource.setSelf(
                    WebResourceUtils.createLink(
                            WebResourceUtils.getAbsoluteWebResourceURI(resourceInfo, uriInfo, id),
                            resource.getCustomJsonMediaType().toString(),
                            null));
        }
        return resource;
    }
}
