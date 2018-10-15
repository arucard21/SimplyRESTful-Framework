package simplyrestful.springdata.repository.nomapping;

import java.util.Optional;

import javax.inject.Named;

import org.springframework.data.repository.PagingAndSortingRepository;

@Named
public interface NoMappingRepository<T extends NoMappingHALResource, UUID> extends PagingAndSortingRepository<T, UUID>{
	Optional<T> findByUuid(UUID uuid);
}
