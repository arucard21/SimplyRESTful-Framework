package simplyrestful.api.framework.core;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
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
import javax.ws.rs.core.Response;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import simplyrestful.api.framework.core.hal.HALCollectionBuilder;
import simplyrestful.api.framework.resources.HALCollection;
import simplyrestful.api.framework.resources.HALResource;

public abstract class DefaultWebResource<T extends HALResource> extends BaseWebResource<T>{
	private static final String ERROR_SELF_LINK_ID_DOES_NOT_MATCH_PROVIDED_ID = "The provided resource contains an self-link that does not match the ID used in the request";
	private static final String ERROR_SELF_LINK_URI_DOES_NOT_MATCH_API_BASE_URI = "The identifier of the resource does not correspond to the base URI of this Web Resource";
	public static final String QUERY_PARAM_PAGE = "page";
	public static final String QUERY_PARAM_PAGE_SIZE = "pageSize";
	public static final String QUERY_PARAM_COMPACT = "compact";

	@GET
	@Produces(AdditionalMediaTypes.APPLICATION_HAL_JSON + "; profile=\"" + HALCollection.PROFILE_STRING + "\"")
	@ApiOperation(
		value = "Get a list of resources",
		notes = "Get a list of resources"
	)
	@Override
	public HALCollection<T> getHALResources(
			@ApiParam(value = "The page to be shown", required = false)
			@QueryParam(QUERY_PARAM_PAGE)
			@DefaultValue(HALCollectionBuilder.DEFAULT_PAGE_NUMBER_STRING)
			long page,
			@ApiParam(value = "The amount of resources shown on each page", required = false)
			@QueryParam(QUERY_PARAM_PAGE_SIZE)
			@DefaultValue(HALCollectionBuilder.DEFAULT_MAX_PAGESIZE_STRING)
			long pageSize,
			@ApiParam(value = "Provide minimal information for each resource", required = false)
			@QueryParam(QUERY_PARAM_COMPACT)
			@DefaultValue(HALCollectionBuilder.DEFAULT_COMPACT_VALUE_STRING)
			boolean compact) {
		return HALCollectionBuilder.fromPartial(
				this.listing(page, pageSize),
				getRequestURI(),
				this.count())
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
	@Override
	public Response postHALResource(
			@ApiParam(value = "resource", required = true) 
			@NotNull 
			T resource) {
		UUID resourceId = ensureSelfLinkValid(resource, null);
		if (this.exists(resourceId)){
			throw new ClientErrorException("A resource with the same ID already exists. Try to update the resource with a PUT request.", Response.Status.CONFLICT);
		}
		T updatedResource = this.create(resource, resourceId);
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
	@Override
	public T getHALResource(
			@ApiParam(value = "The identifier for the resource", required = true)
			@PathParam("id")
			@NotNull
			UUID id) {
		return Optional.ofNullable(this.read(id))
				.orElseThrow(NotFoundException::new);
	}

	@Path("/{id}")
	@PUT
	@ApiOperation(
		value = "Create or update a resource",
		notes = "Create a resource with a specified ID or update that resource. Returns a 201 HTTP status with the UUID of the resource in the Location header, if a new one was created. Otherwise it just returns 200 OK."
	)
	@Override
	public Response putHALResource(
			@ApiParam(value = "The UUID part of the identifier for the resource", required = true)
			@PathParam("id")
			@NotNull
			UUID id,
			@ApiParam(value = "The resource to be updated", required = true)
			@NotNull
			T resource){
		ensureSelfLinkValid(resource, id);
		if(this.exists(id)) {
			this.update(resource, id);
			return Response.ok().build();
		}
		this.create(resource, id);
		return Response.created(getAbsoluteWebResourceURI(id)).build();
	}

	@Path("/{id}")
	@DELETE
	@ApiOperation(
		value = "Delete a single resource",
		notes = "Delete a single resource"
	)
	@Override
	public Response deleteHALResource(
			@ApiParam(value = "The UUID part of the identifier for the resource", required = true)
			@PathParam("id")
			@NotNull
			UUID id) {
		return Optional.ofNullable(this.delete(id))
				.map(resource -> Response.noContent().build())
				.orElseThrow(NotFoundException::new);
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
	@Override
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
	@Override
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
	@Override
	protected URI getAbsoluteWebResourceURI() {
		return getAbsoluteWebResourceURI(null);
	}

	@Override
	protected URI getRequestURI() {
		return uriInfo.getRequestUri();
	}
	
	/**
	 * Checks if the self-link is present and valid. 
	 * 
	 * A new self-link can be generated with a random or provided UUID, if one does not yet exist.
	 * If a UUID is provided and a self-link exists, the ID in the self-link must match the provided UUID.
	 *  
	 * @param resource is the resource to check.
	 * @param providedID is a resource ID that should be used in the self-link, if one does not yet exist.
	 * @return the resource UUID that matches the ID used in the self-link.
	 */
	private UUID ensureSelfLinkValid(T resource, UUID providedID) {
		if(resource.getSelf() == null){
			if(providedID == null) {
				providedID = UUID.randomUUID();
			}
			resource.setSelf(createLink(getAbsoluteWebResourceURI(providedID), resource.getProfile()));
			return providedID;
		}
		else{
			URI selfUri = URI.create(resource.getSelf().getHref());
			URI relativizedResourceUri = getAbsoluteWebResourceURI().relativize(selfUri);
			if (relativizedResourceUri.equals(selfUri)) {
				throw new BadRequestException(ERROR_SELF_LINK_URI_DOES_NOT_MATCH_API_BASE_URI);
			}
			UUID resourceIdFromSelf = UUID.fromString(relativizedResourceUri.getPath());
			if (providedID != null && providedID != resourceIdFromSelf){
				throw new BadRequestException(ERROR_SELF_LINK_ID_DOES_NOT_MATCH_PROVIDED_ID);
			}
			return resourceIdFromSelf;
		}
	}
}
