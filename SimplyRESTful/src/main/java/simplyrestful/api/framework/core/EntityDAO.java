package simplyrestful.api.framework.core;

import java.util.List;
import java.util.UUID;

import javax.inject.Named;

/**
 * This interface allows access to stored data in terms of the data store (called entities here). 
 *
 * An implementation of this interface would access stored data directly from the data store. The main difference
 * with the ResourceDAO is that the identifier for entities is a UUID (instead of a URI for resources).
 * 
 * IMPORTANT: Knowledge of these underlying mechanisms, like the mapping or use of additional DAOs, should not be
 * needed to use this interface. 
 *
 * @param <E> is the type of object that will be persisted
 */
@Named
public interface EntityDAO<E> {
	/**
	 * @return the total amount of entities that are available
	 */
	public long count();
	
	/**
	 * Retrieve a subset of entities, corresponding to then give page number and size.
	 *
	 * @param pageNumber is the requested page number
	 * @param pageSize is the requested size of each page
	 * @return the requested collection containing the entities for the requested page
	 */
	public List<E> findAllForPage(int pageNumber, int pageSize);

	/**
	 * Retrieve the entity from the data store.
	 *
	 * @param entityID is the identifier for the entity
	 * @return the entity that was requested or null if it doesn't exist
	 */
	public E findByUUID(UUID entityID);

	/**
	 * Update the entity in the data store.
	 *
	 * @param entity is the entity that should be persisted
	 * @return the updated entity as persisted
	 */
	public E persist(E entity);

	/**
	 * Remove an entity from the data store.
	 *
	 * @param entityID is the identifier of the entity that should be removed
	 * @return the removed entity, or null if it did not exist
	 */
	public E remove(UUID entityID);
}
