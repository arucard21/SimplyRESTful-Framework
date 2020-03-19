package simplyrestful.springdata.repository;

import java.util.Optional;
import java.util.UUID;

import javax.inject.Named;

import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * A Spring Data Repository to access the data store. 
 * 
 * The entity that is persisted must have a UUID field in order to identify each entity.  
 *
 * @param <E> is the entity used to persist data (which contains a UUID field)
 */
@Named
public interface SpringDataRepository<E> extends PagingAndSortingRepository<E, Long>{
	Optional<E> findByUuid(UUID uuid);
}
