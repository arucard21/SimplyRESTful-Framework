package simplyrestful.api.framework.core;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;

import io.openapitools.jackson.dataformat.hal.HALLink;
import simplyrestful.api.framework.resources.HALResource;

/**
 * This class provides a default implementation that allows using the API resource directly in the provided EntityDAO. 
 * This implementation forwards the API resource as-is to the provided EntityDAO, adjusting it as needed.
 *
 * @param <T> is the type of HALResource object that is used in the API as well as the EntityDAO.
 */
@Named
public class DefaultNoMappingResourceDAO<T extends HALResource> extends DefaultResourceDAO<T, T>{
	public DefaultNoMappingResourceDAO(EntityDAO<T> entityDao) {
		super(entityDao);
	}

	/**
	 * @return the total amount of resources that are available
	 */
	public long count() {
		return getEntityDao().count();
	}
	
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
	public List<T> findAllForPage(int pageNumber, int pageSize, URI absoluteWebResourceURI){
		return getEntityDao().findAllForPage(pageNumber, pageSize).stream()
				.map(entity -> map(entity, absoluteWebResourceURI))
				.collect(Collectors.toList());
	}

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
	public T findByURI(URI resourceURI, URI absoluteWebResourceURI) {
		URI relativizedURI = absoluteWebResourceURI.relativize(resourceURI);
		UUID resourceId = UUID.fromString(relativizedURI.getPath());
		return map(getEntityDao().findByUUID(resourceId), absoluteWebResourceURI);
	}

	/**
	 * Update the resource in the data store where it is stored.
	 *
	 * The resource should contain a self-link in order to identify which resource needs to be updated.
	 *
	 * @param resource is the updated resource (which contains a self-link with which to identify the resource)
	 * @param absoluteWebResourceURI is the absolute URI to the web resource (without UUID)
	 * @return the updated resource as persisted
	 */
	public T persist(T resource, URI absoluteWebResourceURI) {
		T entity = getEntityDao().persist(map(resource));
		if (entity == null) {
			return null;
		}
		return map(entity, absoluteWebResourceURI);
		
	}

	/**
	 * Remove a resource from the data store.
	 *
	 * @param resourceURI is the identifier of the resource that should be removed
	 * @param absoluteWebResourceURI is the absolute URI to the web resource (without UUID)
	 * @return the removed resource, or null if it did not exist
	 */
	public T remove(URI resourceURI, URI absoluteWebResourceURI) {
		URI relativizedURI = absoluteWebResourceURI.relativize(resourceURI);
		UUID resourceId = UUID.fromString(relativizedURI.getPath());
		T entity = getEntityDao().remove(resourceId);
		if (entity == null) {
			return null;
		}
		return map(entity, absoluteWebResourceURI);
	}

	/**
	 * Maps an API resource to an entity that can be stored using the EntityDAO.
	 * 
	 * @param resource is the API resource.
	 * @return the entity that can be stored using the EntityDAO, mapped from the given resource.
	 */
	@Override
	public T map(T resource) {
		return resource;
	}
	
	/**
	 * Maps an entity from the data store, retrieved through the EntityDAO, to an API resource.
	 * 
	 * @param entity is an entity object from the data store, retrieved through the EntityDAO.
	 * @param resourceURI is the absolute resource URI for the API resource, used to generate the self link.
	 * @return the API resource, mapped from the given entity.
	 */
	@Override
	public T map(T entity, URI resourceURI) {
		ensureSelfLinkPresent(entity, resourceURI);
		return entity;
	}
	
	private void ensureSelfLinkPresent(T persistedResource, URI absoluteWebResourceURI) {
		persistedResource.setSelf(new HALLink.Builder(UriBuilder.fromUri(absoluteWebResourceURI).path(persistedResource.getUUID().toString()).build())
				.type(MediaType.APPLICATION_HAL_JSON)
				.profile(persistedResource.getProfile())
				.build());
	}
}
