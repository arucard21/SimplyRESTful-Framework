package simplyrestful.api.framework.core.mapper;

import java.net.URI;

import simplyrestful.api.framework.resources.HALResource;

/**
 * This interface defines the mapper that maps resources to entities and back.
 * 
 * @param <T> is the type of HALResource object that is used in the API.
 * @param <E> is the type of entity that can be stored using the EntityDAO.
 */
public interface ResourceMapper<T extends HALResource, E> {
	/**
	 * Maps an API resource to an entity that can be stored using the EntityDAO.
	 * 
	 * @param resource is the API resource.
	 * @return the entity that can be stored using the EntityDAO, mapped from the given resource.
	 */
	public E map(T resource);
	
	/**
	 * Maps an entity from the data store, retrieved through the EntityDAO, to an API resource.
	 * 
	 * @param entity is an entity object from the data store, retrieved through the EntityDAO.
	 * @param resourceURI is the absolute resource URI for the API resource, used to generate the self link.
	 * @return the API resource, mapped from the given entity.
	 */
	public T map(E entity, URI resourceURI);
}