package simplyrestful.springdata.repository;

import java.util.Optional;
import java.util.UUID;

import javax.inject.Named;

import org.springframework.data.repository.PagingAndSortingRepository;

@Named
public interface SpringDataRepository<E> extends PagingAndSortingRepository<E, UUID>{
	Optional<E> findByUuid(UUID uuid);
}
