package simplyrestful.api.framework.core;

import simplyrestful.api.framework.core.exceptions.InvalidResourceException;
import simplyrestful.api.framework.core.exceptions.InvalidSelfLinkException;
import simplyrestful.api.framework.core.hal.HALCollection;
import simplyrestful.api.framework.core.hal.HALResource;

/**
 * This interface allows access to backend data in terms of the API's Web Resources.
 *
 * Any implementation of this interface would need to map the data in the backend to the type of HALResource
 * object that the API makes available.
 *
 * @param <T> is the type of HALResource object that is used in the API.
 */
public interface HALResourceAccess<T extends HALResource> {
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
	public HALCollection<T> retrieveResourcesFromDataStore(int pageNumber, int pageSize, boolean compact);

    /**
     * Add a resource to the data store.
     *
     * @param resource is the resource that will be added
     * @return true iff the resource was successfully added, false otherwise.
     */
	public boolean addResourceToDataStore(T resource);

	/**
	 * Verify that a resource is known to the API.
	 *
	 * @param resourceURI is the URI that represents the resource
	 * @return true iff the resource is known to the API, false otherwise
	 */
	public boolean exists(String resourceURI);

	/**
	 * Retrieve the resource from the data store where it is stored.
	 *
	 * The identifier does not necessarily have to be the same as the identifier used in the data store. You can map this API
	 * identifier to the correct resource in any way you want.
	 *
	 * @param resourceURI is the identifier (from API perspective) for the resource
	 * @return the resource that was requested or null if it doesn't exist
	 */
	public T retrieveResourceFromDataStore(String resourceURI);

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
	public T updateResourceInDataStore(T resource) throws 	InvalidResourceException,
																		InvalidSelfLinkException;

	/**
     * Remove a resource from the data store.
     *
     * @param resourceURI is the identifier of the resource that should be removed
     * @return the removed resource, or null if it did not exist
     */
	public T removeResourceFromDataStore(String resourceURI);
}
