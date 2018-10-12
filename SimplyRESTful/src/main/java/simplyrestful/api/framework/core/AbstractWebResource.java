package simplyrestful.api.framework.core;

import java.net.URI;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
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
import simplyrestful.api.framework.core.hal.HALCollectionBuilder;
import simplyrestful.api.framework.core.hal.HALCollectionBuilderFromPartialList;
import simplyrestful.api.framework.core.hal.HALResource;

public abstract class AbstractWebResource<T extends HALResource> {
	private ResourceDAO<T> resourceDao;

	public static final String QUERY_PARAM_PAGE = "page";
	public static final String QUERY_PARAM_PAGE_SIZE = "pageSize";
	public static final String QUERY_PARAM_COMPACT = "compact";

	@Context
	protected UriInfo uriInfo;
	
	@Inject
	public AbstractWebResource(ResourceDAO<T> resourceDao) {
		this.resourceDao = resourceDao;
	}

	@Produces({MediaType.APPLICATION_HAL_JSON})
	@GET 
	@ApiOperation(
		value = "Get a list of resources",
		notes = "Get a list of resources"
	)
	public HALCollection<T> getHALResources(
			@ApiParam(value = "The page to be shown", required = false)
			@QueryParam(QUERY_PARAM_PAGE)
			@DefaultValue(HALCollectionBuilder.DEFAULT_PAGE_NUMBER_STRING)
			int page,
			@ApiParam(value = "The amount of resources shown on each page", required = false)
			@QueryParam(QUERY_PARAM_PAGE_SIZE)
			@DefaultValue(HALCollectionBuilder.DEFAULT_MAX_PAGESIZE_STRING)
			int pageSize,
			@ApiParam(value = "Provide minimal information for each resource", required = false)
			@QueryParam(QUERY_PARAM_COMPACT)
			@DefaultValue(HALCollectionBuilder.DEFAULT_COMPACT_VALUE_STRING)
			boolean compact) {
		return new HALCollectionBuilderFromPartialList<T>(
				resourceDao.findAllForPage(page, pageSize),
				getAbsoluteBaseURI(),
				resourceDao.count())
						.page(page)
						.maxPageSize(pageSize)
						.compact(compact)
						.build();
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
		URI absoluteResourceIdentifier = getAbsoluteURI(id);
		T resource = resourceDao.findById(absoluteResourceIdentifier);
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
			@ApiParam(value = "resource", required = true) 
			@NotNull 
			final T resource
			) throws InvalidSelfLinkException, InvalidResourceException {
		if (resource.getSelf() != null && resourceDao.findById(URI.create(resource.getSelf().getHref())) != null){
			throw new ClientErrorException("A resource with the same ID already exists. Try to update the resource with a PUT request.", Response.Status.CONFLICT);
		}
		T previousResource = resourceDao.persist(resource);
		if(previousResource == null) {
			return Response
					.created(URI.create(resource.getSelf().getHref()))
					.build();
		}
		T currentResource = resourceDao.persist(previousResource);
		if (resource.equals(currentResource)) {
			throw new ClientErrorException("A resource with the same ID already exists, the persisted resource was rolled back. Try to update the resource with a PUT request.", Response.Status.CONFLICT);
		}
		throw new ClientErrorException("A resource with the same ID already exists, a rollback was attempted but failed. This resource may now be in an invalid state. Try to update the resource with a PUT request.", Response.Status.CONFLICT);
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
			@ApiParam(value = "The identifier for the resource", required = true) @PathParam("id") @NotNull String id,
			@ApiParam(value = "The resource to be updated", required = true) @NotNull final T resource){
		if(resource.getSelf() != null && !resource.getSelf().getHref().contains(id)){
			throw new BadRequestException("The provided resource contains an self-link that does not match the ID used in the request");
		}
		if(resource.getSelf() == null){
			URI absoluteResourceIdentifier = getAbsoluteURI(id);
			resource.setSelf(createLink(absoluteResourceIdentifier, resource.getProfile()));
		}
		return resourceDao.persist(resource);
	}

	@Path("/{id}")
	@DELETE
	@ApiOperation(
		value = "Delete operation with implicit header",
		notes = "Delete operation with implicit header"
	)
	public Response deleteHALResource(@ApiParam(value = "The identifier for the resource", required = true) @PathParam("id") @NotNull String id) {
		URI absoluteResourceIdentifier = getAbsoluteURI(id);
		if(resourceDao.remove(absoluteResourceIdentifier) == null){
			throw new NotFoundException();
		}
		return Response.noContent().build();
	}

	/**
	 * Get the absolute URI for a different web resource with the given ID.
	 * 
	 * The different web resource has to be hosted on the same application root as this web resource.  
	 * This will likely already be the case when you have access to the Class object of that web resource
	 *
	 * @param webResource is the class of the @Path-annotated endpoint class for the resource
	 * @param id is the ID of the resource, which can be null if the base URI is requested.
	 * @return the absolute URI for the resource on the requested endpoint.
	 */
	protected URI getAbsoluteURI(Class<?> webResource, String id) {
		if (id == null) {
			return uriInfo.getBaseUriBuilder().path(webResource).build();
		}
		return uriInfo.getBaseUriBuilder().path(webResource).path(id).build();
	}

	/**
	 * Get the absolute URI for the web resource with the given ID.
	 * 
	 * Example: https://example.com/api/resource/00000000-0000-0000-0000-000000000000
	 *
	 * @param id is the ID of the resource provided on the endpoint.
	 * @return the absolute URI for the resource on the endpoint.
	 */
	protected URI getAbsoluteURI(String id) {
		return getAbsoluteURI(this.getClass(), id);
	}
	
	/**
	 * Get the absolute base URI for this web resource.
	 * 
	 * Example: https://example.com/api/resource/
	 * 
	 * @return the absolute base URI for this resource
	 */
	protected URI getAbsoluteBaseURI() {
		return getAbsoluteURI(null);
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
