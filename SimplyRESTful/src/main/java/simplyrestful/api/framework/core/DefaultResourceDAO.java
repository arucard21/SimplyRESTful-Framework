package simplyrestful.api.framework.core;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Named;

import simplyrestful.api.framework.resources.HALResource;

/**
 * This class provides a default implementation that allows mapping the API resource to the entity from the provided EntityDAO. 
 * This assumes that the API resource can be mapped 1-on-1 to and from the entity. 
 *
 * @param <T> is the type of HALResource object that is used in the API.
 */
@Named
public abstract class DefaultResourceDAO<T extends HALResource, E> implements ResourceDAO<T>{
	private final EntityDAO<E> entityDao;
	
	public DefaultResourceDAO(EntityDAO<E> entityDao) {
		this.entityDao = entityDao;
	}

	/**
	 * @return the total amount of resources that are available
	 */
	public long count() {
		return entityDao.count();
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
		return entityDao.findAllForPage(pageNumber, pageSize).stream()
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
		return map(entityDao.findByUUID(resourceId), absoluteWebResourceURI);
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
		E entity = entityDao.persist(map(resource));
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
		E entity = entityDao.remove(resourceId);
		if (entity == null) {
			return null;
		}
		return map(entity, absoluteWebResourceURI);
	}

	protected EntityDAO<E> getEntityDao() {
		return entityDao;
	}
	
	/**
	 * Maps an API resource to an entity that can be stored using the EntityDAO.
	 * 
	 * @param resource is the API resource.
	 * @return the entity that can be stored using the EntityDAO, mapped from the given resource.
	 */
	public abstract E map(T resource);
	
	/**
	 * Maps an entity from the data store, retrieved through the EntityDAO, to an API resource.
	 * 
	 * @param entity is an entity object from the data store, retrieved through the EntityDAO.
	 * @param resourceURI is the absolute resource URI for the API resource, used to generate the self link.
	 * @return the API resource, mapped from the given entity.
	 */
	public abstract T map(E entity, URI resourceURI);
}
