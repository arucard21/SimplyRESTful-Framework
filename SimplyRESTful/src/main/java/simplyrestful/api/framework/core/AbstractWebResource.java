package simplyrestful.api.framework.core;

import java.net.URI;
import java.util.UUID;

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
import simplyrestful.api.framework.core.hal.HALCollectionBuilder;
import simplyrestful.api.framework.core.hal.HALCollectionBuilderFromPartialList;
import simplyrestful.api.framework.resources.HALCollection;
import simplyrestful.api.framework.resources.HALResource;

@Produces({MediaType.APPLICATION_HAL_JSON})
@Consumes({MediaType.APPLICATION_HAL_JSON})
public abstract class AbstractWebResource<T extends HALResource, E> {
	private ResourceDAO<T, E> resourceDao;

	public static final String QUERY_PARAM_PAGE = "page";
	public static final String QUERY_PARAM_PAGE_SIZE = "pageSize";
	public static final String QUERY_PARAM_COMPACT = "compact";

	@Context
	protected UriInfo uriInfo;
	
	public AbstractWebResource(ResourceDAO<T, E> resourceDao) {
		this.resourceDao = resourceDao;
	}

	@GET
	@Produces(MediaType.APPLICATION_HAL_JSON + "; profile=\"" + HALCollection.PROFILE_STRING + "\"")
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
				resourceDao.findAllForPage(page, pageSize, getAbsoluteWebResourceURI()),
				getRequestURI(),
				resourceDao.count())
						.page(page)
						.maxPageSize(pageSize)
						.compact(compact)
						.build();
	}

	@POST
	@ApiOperation(
		value = "Create a new resource",
		notes = "Create a new resource which can already have a self-link containing a URI as identifier or one will be generated"
	)
	public Response postHALResource(
			@ApiParam(value = "resource", required = true) 
			@NotNull 
			final T resource
			) {
		if (resource.getSelf() == null) {
			T updatedResource = resourceDao.persist(resource, getAbsoluteWebResourceURI());
			return Response
					.created(URI.create(updatedResource.getSelf().getHref()))
					.build();
		}
		URI resourceSelfURI = URI.create(resource.getSelf().getHref());
		if(getAbsoluteWebResourceURI().relativize(resourceSelfURI).equals(resourceSelfURI)) {
			throw new BadRequestException("The identifier of the resource does not correspond to the URI of this Web Resource");
		}
		T previousResource = resourceDao.findByURI(resourceSelfURI, getAbsoluteWebResourceURI());
		if (previousResource != null){
			throw new ClientErrorException("A resource with the same ID already exists. Try to update the resource with a PUT request.", Response.Status.CONFLICT);
		}
		T updatedResource = resourceDao.persist(resource, getAbsoluteWebResourceURI());
		return Response
				.created(URI.create(updatedResource.getSelf().getHref()))
				.build();
	}

	@Path("/{id}")
	@GET
	@ApiOperation(
		value = "Retrieve a single resource",
		notes = "Retrieve a single resource"
	)
	public T getHALResource(
			@ApiParam(value = "The identifier for the resource", required = true)
			@PathParam("id")
			UUID id) {
		URI absoluteResourceIdentifier = getAbsoluteWebResourceURI(id);
		T resource = resourceDao.findByURI(absoluteResourceIdentifier, getAbsoluteWebResourceURI());
		if (resource == null){
			throw new NotFoundException();
		}
		return resource;
	}

	@Path("/{id}")
	@PUT
	@ApiOperation(
		value = "Create or update a resource",
		notes = "Create a resource with a specified ID or update that resource. Returns a 201 HTTP status with the UUID of the resource in the Location header, if a new one was created. Otherwise it just returns 200 OK."
	)
	public Response putHALResource(
			@ApiParam(value = "The UUID part of the identifier for the resource", required = true)
			@PathParam("id")
			@NotNull
			UUID id,
			@ApiParam(value = "The resource to be updated", required = true)
			@NotNull
			final T resource){
		if(resource.getSelf() != null && !resource.getSelf().getHref().contains(id.toString())){
			throw new BadRequestException("The provided resource contains an self-link that does not match the ID used in the request");
		}
		URI absoluteResourceIdentifier = getAbsoluteWebResourceURI(id);
		if(resource.getSelf() == null){
			resource.setSelf(createLink(absoluteResourceIdentifier, resource.getProfile()));
		}
		URI absoluteWebResourceURI = getAbsoluteWebResourceURI();
		T previousResource = resourceDao.findByURI(absoluteResourceIdentifier, absoluteWebResourceURI);
		resourceDao.persist(resource, absoluteWebResourceURI);
		if (previousResource == null) {
			return Response.created(absoluteResourceIdentifier).build();
		}
		return Response.ok().build();
	}

	@Path("/{id}")
	@DELETE
	@ApiOperation(
		value = "Delete a single resource",
		notes = "Delete a single resource"
	)
	public Response deleteHALResource(
			@ApiParam(value = "The UUID part of the identifier for the resource", required = true)
			@PathParam("id")
			@NotNull
			UUID id) {
		URI absoluteResourceIdentifier = getAbsoluteWebResourceURI(id);
		if(resourceDao.remove(absoluteResourceIdentifier, getAbsoluteWebResourceURI()) == null){
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
	 * @param id is the UUID of the resource, which can be null if the base URI is requested.
	 * @return the absolute URI for the resource on the requested endpoint.
	 */
	protected URI getAbsoluteWebResourceURI(Class<?> webResource, UUID id) {
		if (id == null) {
			return uriInfo.getBaseUriBuilder().path(webResource).build();
		}
		return uriInfo.getBaseUriBuilder().path(webResource).path(id.toString()).build();
	}

	/**
	 * Get the absolute URI for the web resource with the given ID.
	 * 
	 * Example: https://example.com/api/resource/00000000-0000-0000-0000-000000000000
	 *
	 * @param id is the ID of the resource provided on the endpoint.
	 * @return the absolute URI for the resource on the endpoint.
	 */
	protected URI getAbsoluteWebResourceURI(UUID id) {
		return getAbsoluteWebResourceURI(this.getClass(), id);
	}
	
	/**
	 * Get the absolute base URI for this web resource.
	 * 
	 * Example: https://example.com/api/resource/
	 * 
	 * @return the absolute base URI for this resource
	 */
	protected URI getAbsoluteWebResourceURI() {
		return getAbsoluteWebResourceURI(null);
	}

	protected URI getRequestURI() {
		return uriInfo.getRequestUri();
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
