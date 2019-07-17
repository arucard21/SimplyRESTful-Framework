package simplyrestful.api.framework.core;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Named;

import simplyrestful.api.framework.core.mapper.NoMappingMapper;
import simplyrestful.api.framework.core.mapper.ResourceMapper;
import simplyrestful.api.framework.resources.HALResource;

/**
 * This interface allows access to stored data in terms of the API's Web Resources which are identified by a URI.
 *
 * An implementation of this interface would map the API resource to a different entity more suitable for the 
 * type of storage used. A different DAO needs to be provided for access to the stored data for that mapped entity.
 * 
 * By default, ResourceDAO will simply call the corresponding method from EntityDAO without mapping the resource to
 * an entity. 
 * 
 * IMPORTANT: Knowledge of these underlying mechanisms, like the mapping or use of additional DAOs, should not be 
 * needed to use this interface. 
 *
 * @param <T> is the type of HALResource object that is used in the API.
 */
@Named
public abstract class ResourceDAO<T extends HALResource, E> {
	private final ResourceMapper<T, E> mapper;
	private final EntityDAO<E> entityDao;
	
	public ResourceDAO(ResourceMapper<T, E> mapper, EntityDAO<E> entityDao) {
		this.mapper = mapper;
		this.entityDao = entityDao;
	}
	
	/**
	 * Create a ResourceDAO without mapping to a different entity class.
	 * 
	 * Without mapping, the entity class must be the same as the resource class. So the generic
	 * type for the entity DAO should be T.
	 * 
	 * @param entityDao is the DAO for the unmapped resource. Its generic type should match T.
	 */
	@SuppressWarnings("unchecked")
	public ResourceDAO(EntityDAO<E> entityDao) {
		this.mapper = (ResourceMapper<T, E>) new NoMappingMapper<T>();
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
				.map(entity -> mapper.map(entity, absoluteWebResourceURI))
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
		return mapper.map(entityDao.findByUUID(resourceId), absoluteWebResourceURI);
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
		E entity = entityDao.persist(mapper.map(resource));
		if (entity == null) {
			return null;
		}
		return mapper.map(entity, absoluteWebResourceURI);
		
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
		return mapper.map(entity, absoluteWebResourceURI);
	}

	protected ResourceMapper<T, E> getMapper() {
		return mapper;
	}

	protected EntityDAO<E> getEntityDao() {
		return entityDao;
	}
}
