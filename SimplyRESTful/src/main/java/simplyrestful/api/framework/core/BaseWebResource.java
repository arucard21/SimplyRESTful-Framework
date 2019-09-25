package simplyrestful.api.framework.core;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.openapitools.jackson.dataformat.hal.HALLink;
import io.swagger.annotations.ApiOperation;
import simplyrestful.api.framework.resources.HALCollection;
import simplyrestful.api.framework.resources.HALResource;

@Produces({AdditionalMediaTypes.APPLICATION_HAL_JSON})
@Consumes({AdditionalMediaTypes.APPLICATION_HAL_JSON})
public abstract class BaseWebResource<T extends HALResource> {
	@Context
	protected UriInfo uriInfo;
	
	/**
	 * Retrieve the paginated collection of resources.
	 * 
	 * @param page is the page number of the paginated collection of resources
	 * @param pageSize is the size of a single page in this paginated collection of resources
	 * @param compact determines whether the resource in the collection only shows its self-link (if true), or the entire resource (if false)
	 * @return the paginated collection of resources.
	 */
	@GET
	@Produces(AdditionalMediaTypes.APPLICATION_HAL_JSON + "; profile=\"" + HALCollection.PROFILE_STRING + "\"")
	@ApiOperation(
		value = "Get a list of resources",
		notes = "Get a list of resources"
	)
	public abstract HALCollection<T> getHALResources(long page, long pageSize, boolean compact);

	/**
	 * Create a resource.
	 * 
	 * @param resource is a resource that should be created.
	 * @return a "201 Created" response for the resource that was created, containing its URI 
	 * identifier in the Location header, if the resource was correctly created.
	 */
	@POST
	@ApiOperation(
		value = "Create a new resource",
		notes = "Create a new resource which can already have a self-link containing a URI as identifier or one will be generated"
	)
	public abstract Response postHALResource(@NotNull T resource);

	/**
	 * Retrieve a resource.
	 * 
	 * @param id is the UUID part from the entire URI identifier of the resource.
	 * @return the requested resource.
	 */
	@Path("/{id}")
	@GET
	@ApiOperation(
		value = "Retrieve a single resource",
		notes = "Retrieve a single resource"
	)
	public abstract T getHALResource(@NotNull UUID id);

	/**
	 * Update a resource (or create it with the given identifier)
	 * 
	 * The resource should contain a self-link. This self-link must match the provided id. 
	 * If a resource with that id does not exist yet, it will be created with that id. 
	 * 
	 * @param id is the UUID part from the entire URI identifier of the resource.
	 * @param resource is the updated resource. 
	 * @return a "200 OK" if the resource was updated, or "201 Created" if the resource was created.
	 */
	@Path("/{id}")
	@PUT
	@ApiOperation(
		value = "Create or update a resource",
		notes = "Create a resource with a specified ID or update that resource. Returns a 201 HTTP status with the UUID of the resource in the Location header, if a new one was created. Otherwise it just returns 200 OK."
	)
	public abstract Response putHALResource(@NotNull UUID id, @NotNull T resource);

	/**
	 * Delete a resource.
	 * 
	 * @param id is the UUID part from the entire URI identifier of the resource.
	 * @return a "204 No Content" if the resource was correctly deleted.
	 */
	@Path("/{id}")
	@DELETE
	@ApiOperation(
		value = "Delete a single resource",
		notes = "Delete a single resource"
	)
	public abstract Response deleteHALResource(@NotNull UUID id);

	/**
	 * Create the resource in the data store where it is stored.
	 *
	 * The resource should contain a self-link that contains the unique ID for this resource.
	 *
	 * @param resource is the resource  that should be created, containing a self-link with its unique ID
	 * @param resourceUUID is the unique ID of the resource which should match the UUID used in the self-link 
	 * @return the created resource as persisted
	 */
	public abstract T create(T resource, UUID resourceUUID);

	/**
	 * Retrieve the resource from the data store where it is stored.
	 *
	 * The identifier provided by the API is the URI of the resource. This does not have to be the
	 * identifier used in the data store (UUID is more commonly used) but each entity in the data
	 * store must be uniquely identifiable by the information provided in the URI. 
	 *
	 * @param resourceUUID is the identifier (from API perspective) for the resource
	 * @return the resource that was requested or null if it doesn't exist
	 */
	public abstract T read(UUID resourceUUID);

	/**
	 * Update the resource in the data store where it is stored.
	 *
	 * The resource should contain a self-link in order to identify which resource needs to be updated.
	 *
	 * @param resource is the updated resource (which contains a self-link with which to identify the resource)
	 * @param resourceUUID is the identifier of the resource that should be updated
	 * @return the updated resource as persisted
	 */
	public abstract T update(T resource, UUID resourceUUID);

	/**
	 * Remove a resource from the data store.
	 *
	 * @param resourceUUID is the identifier of the resource that should be removed
	 * @return the removed resource, or null if it did not exist
	 */
	public abstract T delete(UUID resourceUUID);

	/**
	 * Retrieve the paged collection of resources that have been requested.
	 *
	 * For proper discoverability of the API, all links (href values in each HALLink object) should contain absolute URI's
	 * and a self-link must be available in each resource.
	 *
	 * @param pageNumber is the requested page number
	 * @param pageSize is the requested size of each page
	 * @return the requested HAL collection containing the resources for the requested page
	 */
	public abstract List<T> listing(long pageNumber, long pageSize);

	/**
	 * Retrieve how many resources are available.
	 * 
	 * This provides a simple implementation for convenience but should be overridden with an optimized implementation, if possible. 
	 * 
	 * @return the total amount of resources that are available
	 */
	public long count() {
		return this.listing(1, Long.MAX_VALUE).stream().count();
	}

	/**
	 * Check if a resource, identified by its resourceURI, exists.
	 * 
	 * This provides a simple implementation for convenience but should be overridden with an optimized implementation, if possible.
	 * 
	 * @param resourceUUID is the identifier of a resource.
	 * @return true if the resource identified by resourceURI exists, false otherwise.
	 */
	public boolean exists(UUID resourceUUID) {
		return Objects.nonNull(this.read(resourceUUID));
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
									.type(AdditionalMediaTypes.APPLICATION_HAL_JSON)
									.profile(resourceProfile)
									.build();
	}
}
