package simplyrestful.api.framework.core;

import java.net.URI;
import java.util.List;

import javax.inject.Named;

import simplyrestful.api.framework.core.exceptions.InvalidResourceException;
import simplyrestful.api.framework.core.exceptions.InvalidSelfLinkException;
import simplyrestful.api.framework.core.hal.HALResource;

/**
 * This interface allows access to stored data in terms of the API's Web Resources which are identified by a URI.
 *
 * An implementation of this interface could access stored data directly from the data store or it could map the 
 * API resource to a different entity more suitable for the type of storage used. Depending on the type of storage, 
 * a different DAO may be needed to provide access to the stored data for that mapped entity. 
 * 
 * IMPORTANT: Knowledge of these underlying mechanisms, like the mapping or use of additional DAOs, should not be 
 * needed to use this interface. 
 *
 * @param <T> is the type of HALResource object that is used in the API.
 */
@Named
public interface ResourceDAO<T extends HALResource> {
	public static final ThreadLocal<URI> ABSOLUTE_BASE_URI = new ThreadLocal<>();

	/**
	 * @return the total amount of resources that are available
	 */
	public long count();
	
	/**
	 * Retrieve the paged collection of resources that have been requested.
	 *
	 * For proper discoverability of the API, all links (href values in each HALLink object) should contain absolute URI's
	 * and a self-link must be available in each resource.
	 *
	 * @param pageNumber is the requested page number
	 * @param pageSize is the requested size of each page
	 * @param absoluteWebResourceURI is the absolute URI to the web resource (without UUID)
	 * @return the requested HAL collection containing the resources for the requested page
	 */
	public List<T> findAllForPage(int pageNumber, int pageSize, URI absoluteWebResourceURI);

	/**
	 * Retrieve the resource from the data store where it is stored.
	 *
	 * The identifier provided by the API is the URI of the resource. This does not have to be the
	 * identifier used in the data store (UUID is more commonly used) but each entity in the data
	 * store must be uniquely identifiable by the information provided in the URI. 
	 *
	 * @param resourceURI is the identifier (from API perspective) for the resource
	 * @param absoluteWebResourceURI is the absolute URI to the web resource (without UUID)
	 * @return the resource that was requested or null if it doesn't exist
	 */
	public T findByURI(URI resourceURI, URI absoluteWebResourceURI);

	/**
	 * Update the resource in the data store where it is stored.
	 *
	 * The resource should contain a self-link in order to identify which resource needs to be updated.
	 *
	 * @param resource is the updated resource (which contains a self-link with which to identify the resource)
	 * @param absoluteWebResourceURI is the absolute URI to the web resource (without UUID)
	 * @return the updated resource as persisted
	 * @throws InvalidResourceException when the resource is not valid (most likely because it does not contain a self-link).
	 * @throws InvalidSelfLinkException when the resource contains a self-link which is not valid.
	 */
	public T persist(T resource, URI absoluteWebResourceURI) throws InvalidResourceException, InvalidSelfLinkException;

	/**
	 * Remove a resource from the data store.
	 *
	 * @param resourceURI is the identifier of the resource that should be removed
	 * @param absoluteWebResourceURI is the absolute URI to the web resource (without UUID)
	 * @return the removed resource, or null if it did not exist
	 */
	public T remove(URI resourceURI, URI absoluteWebResourceURI);
}
