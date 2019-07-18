package simplyrestful.api.framework.core;

import java.net.URI;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import io.openapitools.jackson.dataformat.hal.HALLink;
import simplyrestful.api.framework.resources.HALResource;

@Produces({MediaType.APPLICATION_HAL_JSON})
@Consumes({MediaType.APPLICATION_HAL_JSON})
public abstract class BaseWebResource<T extends HALResource> {
	private final ResourceDAO<T> resourceDao;

	@Context
	protected UriInfo uriInfo;
	
	public BaseWebResource(ResourceDAO<T> resourceDao) {
		this.resourceDao = resourceDao;
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

	protected ResourceDAO<T> getResourceDao() {
		return resourceDao;
	}
}
