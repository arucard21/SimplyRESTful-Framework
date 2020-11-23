package example.jersey.nomapping.resources;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Named;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.lang.Nullable;

@Named
public interface ExampleRepository extends PagingAndSortingRepository<ExampleResource, Long> {
	Optional<ExampleResource> findByUuid(UUID uuid);
	Page<ExampleResource> findAll(@Nullable Specification<ExampleResource> spec, Pageable pageable);
	Stream<ExampleResource> findAll(@Nullable Specification<ExampleResource> spec);
	long count(@Nullable Specification<ExampleResource> spec);
}
