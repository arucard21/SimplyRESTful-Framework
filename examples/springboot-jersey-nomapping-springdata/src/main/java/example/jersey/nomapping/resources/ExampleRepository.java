package example.jersey.nomapping.resources;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Named;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.PagingAndSortingRepository;

import example.resources.jpa.ExampleResource;

@Named
public interface ExampleRepository extends PagingAndSortingRepository<ExampleResource, Long> {
	Optional<ExampleResource> findByUuid(UUID uuid);
	boolean existsByUuid(UUID uuid);
	Page<ExampleResource> findAll(Specification<ExampleResource> spec, Pageable pageable);
	Stream<ExampleResource> findAll(Specification<ExampleResource> spec);
	long count(Specification<ExampleResource> spec);
}
