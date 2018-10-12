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
	 * @return the requested HAL collection containing the resources for the requested page
	 */
	public List<T> findAllForPage(int pageNumber, int pageSize);

	/**
	 * Retrieve the resource from the data store where it is stored.
	 *
	 * The identifier does not necessarily have to be the same as the identifier used in the data store. You can map this API
	 * identifier to the correct resource in any way you want.
	 *
	 * @param resourceURI is the identifier (from API perspective) for the resource
	 * @return the resource that was requested or null if it doesn't exist
	 */
	public T findById(URI resourceURI);

	/**
	 * Update the resource in the data store where it is stored.
	 *
	 * The resource should contain a self-link in order to identify which resource needs to be updated.
	 *
	 * @param resource is the updated resource (which contains a self-link with which to identify the resource)
	 * @return the previous value of the updated resource, or null if no existing resource was found and the resource was created
	 * @throws InvalidResourceException when the resource is not valid (most likely because it does not contain a self-link).
	 * @throws InvalidSelfLinkException when the resource contains a self-link which is not valid.
	 */
	public T persist(T resource) throws InvalidResourceException, InvalidSelfLinkException;

	/**
	 * Remove a resource from the data store.
	 *
	 * @param resourceURI is the identifier of the resource that should be removed
	 * @return the removed resource, or null if it did not exist
	 */
	public T remove(URI resourceURI);
}
