package simplyrestful.api.framework.webresource.api.implementation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.UriInfo;
import simplyrestful.api.framework.api.crud.DefaultRead;
import simplyrestful.api.framework.resources.APIResource;

public interface DefaultResourceGet<T extends APIResource> extends DefaultRead<T> {
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
    @Operation(description = "Retrieve an API resource")
    default T getAPIResource(
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
    	return Optional.ofNullable(this.read(id)).orElseThrow(NotFoundException::new);
    }
}
