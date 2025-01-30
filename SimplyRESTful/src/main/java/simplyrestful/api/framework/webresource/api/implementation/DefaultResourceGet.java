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
import simplyrestful.api.framework.api.crud.ResourceRead;
import simplyrestful.api.framework.resources.ApiResource;

/**
 * Provide a default implementation for retrieving the API resource.
 *
 * @param <T> is the API resource class that used in the JAX-RS WebResource.
 */
public interface DefaultResourceGet<T extends ApiResource> extends ResourceRead<T> {
	/**
	 * The default value for the "fields" query parameter when retrieving an API resource.
	 */
	public static final String QUERY_PARAM_FIELDS_DEFAULT = "all";

	/**
     * Retrieve a resource.
     *
     * @param id is the UUID part from the entire URI identifier of the resource.
     * @param fields is a list that defines which fields should be retrieved. This is only included for convenience as
     * it is already handled by the framework. It can be used to filter on these fields in the backend as well, e.g. to
     * improve performance.
     * @return the requested resource.
     */
    @Path("/{id}")
    @GET
    @Operation(description = "Retrieve an API resource")
    default T getAPIResource(
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
