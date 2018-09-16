package simplyrestful.api.framework.core;

import java.net.URI;

import javax.inject.Inject;
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
import simplyrestful.api.framework.core.hal.HALCollection;
import simplyrestful.api.framework.core.hal.HALResource;

public class WebResourceBase<T extends HALResource> {
	private HALResourceAccess<T> resourceAccess;

	public static final String QUERY_PARAM_PAGE = "page";
	public static final String QUERY_PARAM_PAGE_SIZE = "pageSize";
	public static final String QUERY_PARAM_COMPACT = "compact";

	@Context
	protected UriInfo uriInfo;
	
	@Inject
	public WebResourceBase(HALResourceAccess<T> resourceAccess) {
		this.resourceAccess = resourceAccess;
	}

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
			return resourceAccess.retrieveResourcesFromDataStore(page, pageSize, compact);
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
		T resource = resourceAccess.retrieveResourceFromDataStore(absoluteResourceIdentifier);
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
			if (resourceAccess.exists(URI.create(resource.getSelf().getHref()))){
				throw new ClientErrorException("A resource with the same ID already exists", Response.Status.CONFLICT);
			}
		}
		resourceAccess.addResourceToDataStore(resource);
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
			existingResource = resourceAccess.updateResourceInDataStore(resource);
		}
		catch(InvalidResourceException invalidResource){
			throw new BadRequestException("The provided resource is invalid, most likely due to an invalid or missing self-link");
		}
		if (existingResource == null){
			// create the resource since it did not exist yet
			if (resourceAccess.addResourceToDataStore(resource)){
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
		if(resourceAccess.removeResourceFromDataStore(absoluteResourceIdentifier) == null){
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
}
