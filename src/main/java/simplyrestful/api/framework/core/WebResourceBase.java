package simplyrestful.api.framework.core;

import java.net.URI;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.openapitools.jackson.dataformat.hal.HALLink;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import simplyrestful.api.framework.core.exceptions.InvalidResourceException;
import simplyrestful.api.framework.core.exceptions.InvalidSelfLinkException;
import simplyrestful.api.framework.core.hal.HALCollection;
import simplyrestful.api.framework.core.hal.HALResource;

public abstract class WebResourceBase<T extends HALResource> {
	@Context
	protected UriInfo uriInfo;

	public static final String QUERY_PARAM_PAGE = "page";
	public static final String QUERY_PARAM_PAGE_SIZE = "pageSize";
	public static final String QUERY_PARAM_COMPACT = "compact";

    @Produces({MediaType.APPLICATION_HAL_JSON})
    @GET
    @ApiOperation(
        value = "Get a list of resources",
        notes = "Get a list of resources"
    )
    public HALCollection<T> getHALResources(
    		@ApiParam(value = "The page to be shown", required = false) @QueryParam(QUERY_PARAM_PAGE) @DefaultValue("1") int page,
    		@ApiParam(value = "The amount of resources shown on each page", required = false) @QueryParam(QUERY_PARAM_PAGE_SIZE) @DefaultValue("100") int pageSize,
    		@ApiParam(value = "Provide minimal information for each resource", required = false) @QueryParam(QUERY_PARAM_COMPACT) @DefaultValue("true") boolean compact) {
    	return retrieveResourcesFromDataStore(page, pageSize, compact);
    }

    @Produces({MediaType.APPLICATION_HAL_JSON})
    @Path("/{id}")
    @GET
    @ApiOperation(
        value = "Get operation with type and headers",
        notes = "Get operation with type and headers"
    )
    public T getHALResource(
    		@ApiParam(value = "The identifier for the resource", required = true) @PathParam("id") String id) {
    	URI absoluteResourceIdentifier = getAbsoluteResourceURI(id);
		T resource = retrieveResourceFromDataStore(absoluteResourceIdentifier.toString());
        if (resource == null){
        	throw new NotFoundException();
        }
        return resource;

    }


	@Consumes({MediaType.APPLICATION_HAL_JSON})
    @POST
    @ApiOperation(
        value = "Create a new resource",
        notes = "Create a new resource which can already have a self-link containing an URI as identifier or one will be generated"
    )
    public Response postHALResource(
    		@ApiParam(value = "resource", required = true) final T resource) {
    	if (resource.getSelf() != null){
			if (exists(resource.getSelf().getHref())){
    			throw new ClientErrorException("A resource with the same ID already exists", Response.Status.CONFLICT);
    		}
    	}
    	addResourceToDataStore(resource);
        return Response
            .created(URI.create(resource.getSelf().getHref()))
            .build();
    }

	@Consumes({MediaType.APPLICATION_HAL_JSON})
	@Produces({MediaType.APPLICATION_HAL_JSON})
    @Path("/{id}")
    @PUT
    @ApiOperation(
        value = "Create or update a resource",
        notes = "Create a resource with a specified ID or update that resource. Returns the previous (now overridden) resource or nothing if a new resource was created."
    )
    public T putHALResource(
    		@ApiParam(value = "The identifier for the resource", required = true) @PathParam("id") String id,
    		@ApiParam(value = "The resource to be updated", required = true) final T resource){
		if(resource.getSelf() != null && !resource.getSelf().getHref().contains(id)){
			throw new BadRequestException("The provided resource contains an self-link that does not match the ID used in the request");
		}
		if(resource.getSelf() == null){
			URI absoluteResourceIdentifier = getAbsoluteResourceURI(id);
			resource.setSelf(createLink(absoluteResourceIdentifier, resource.getProfile()));
		}
		T existingResource;
		try{
			existingResource = updateResourceInDataStore(resource);
		}
		catch(InvalidResourceException invalidResource){
			throw new BadRequestException("The provided resource is invalid, most likely due to an invalid or missing self-link");
		}
		if (existingResource == null){
			// create the resource since it did not exist yet
			if (addResourceToDataStore(resource)){
				return null;
			}
			else{
				throw new NotFoundException("The to-be-updated resource did not exist yet, but could also not be created");
			}
		}
		return existingResource;
    }

	@Path("/{id}")
    @DELETE
    @ApiOperation(
        value = "Delete operation with implicit header",
        notes = "Delete operation with implicit header"
    )
    public Response deleteHALResource(@ApiParam(value = "The identifier for the resource", required = true) @PathParam("id") String id) {
    	URI absoluteResourceIdentifier = getAbsoluteResourceURI(id);
    	if(removeResourceFromDataStore(absoluteResourceIdentifier.toString()) == null){
    		throw new NotFoundException();
    	}
        return Response.noContent().build();
    }

    /**
     * Get the absolute URI for a resource on a different endpoint with the same base URI as this endpoint.
     *
     * @param resourceEndpoint is the class of the @Path-annotated endpoint class for the resource
     * @param id is the ID of the resource provided on the endpoint.
     * @return the absolute URI for the resource on the requested endpoint.
     */
	protected URI getAbsoluteResourceURI(Class<?> resourceEndpoint, String id) {
		return uriInfo.getBaseUriBuilder().path(resourceEndpoint).path(id).build();
	}

	/**
	 * Get the absolute URI for a resource on this endpoint.
	 *
	 * @param id is the ID of the resource provided on the endpoint.
	 * @return the absolute URI for the resource on the endpoint.
	 */
	protected URI getAbsoluteResourceURI(String id) {
		return getAbsoluteResourceURI(this.getClass(), id);
	}

	/**
	 * Create a {@link HALLink} that refers to the provided resource URI with the given profile.
	 *
	 * Note that the media type is always set to HAL+JSON.
	 *
	 * @param resourceURI is the URI of the resource to which this {@link HALLink} refers
	 * @param resourceProfile is the URI of the profile describing the resource to which this {@link HALLink} refers
	 * @return a {@link HALLink} that refers to the provided URI with the given profile
	 */
	protected HALLink createLink(URI resourceURI, URI resourceProfile) {
		return new HALLink.Builder(resourceURI)
									.type(MediaType.APPLICATION_HAL_JSON)
									.profile(resourceProfile)
									.build();
	}

	/**
     * Retrieve the paged collection of resources that have been requested.
     *
     * For proper discoverability of the API, all links (href values in each HALLink object) should contain absolute URI's
     * and a self-link must be available in each resource.
     *
     * @param pageNumber is the requested page number
     * @param pageSize is the requested size of each page
     * @param compact determines whether only the self-link is shown (in _links) or the entire resource (in _embedded)
     * @return the requested HAL collection containing the resource (for that page)
     */
	protected abstract HALCollection<T> retrieveResourcesFromDataStore(int pageNumber, int pageSize, boolean compact);

    /**
     * Add a resource to the data store.
     *
     * @param resource is the resource that will be added
     * @return true iff the resource was successfully added, false otherwise.
     */
	protected abstract boolean addResourceToDataStore(T resource);

	/**
	 * Verify that a resource is known to the API.
	 *
	 * @param resourceURI is the URI that represents the resource
	 * @return true iff the resource is known to the API, false otherwise
	 */
	protected abstract boolean exists(String resourceURI);

	/**
	 * Retrieve the resource from the data store where it is stored.
	 *
	 * The identifier does not necessarily have to be the same as the identifier used in the data store. You can map this API
	 * identifier to the correct resource in any way you want.
	 *
	 * @param resourceURI is the identifier (from API perspective) for the resource
	 * @return the resource that was requested or null if it doesn't exist
	 */
	protected abstract T retrieveResourceFromDataStore(String resourceURI);

	/**
	 * Update the resource in the data store where it is stored.
	 *
	 * The resource should contain a self-link in order to identify which resource needs to be updated.
	 *
	 * @param resource is the updated resource (which contains a self-link with which to identify the resource)
	 * @return the previous value of the updated resource, or null if no existing resource was found
	 * @throws InvalidResourceException when the resource is not valid (most likely because it does not contain a self-link).
	 * @throws InvalidSelfLinkException when the resource does not contain a valid self-link.
	 */
	protected abstract T updateResourceInDataStore(T resource) throws 	InvalidResourceException,
																		InvalidSelfLinkException;

	/**
     * Remove a resource from the data store.
     *
     * @param resourceURI is the identifier of the resource that should be removed
     * @return the removed resource, or null if it did not exist
     */
	protected abstract T removeResourceFromDataStore(String resourceURI);
}
