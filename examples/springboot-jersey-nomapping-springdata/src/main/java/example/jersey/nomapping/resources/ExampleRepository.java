package example.jersey.nomapping.resources;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import jakarta.inject.Named;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

@Named
public interface ExampleRepository extends PagingAndSortingRepository<ExampleResourceEntity, Long>, CrudRepository<ExampleResourceEntity, Long> {
	Optional<ExampleResourceEntity> findByUuid(UUID uuid);
	boolean existsByUuid(UUID uuid);
	Page<ExampleResourceEntity> findAll(Specification<ExampleResourceEntity> spec, Pageable pageable);
	Stream<ExampleResourceEntity> findAll(Specification<ExampleResourceEntity> spec);
	Stream<ExampleResourceEntity> findAll(Specification<ExampleResourceEntity> spec, Sort sort);
	long count(Specification<ExampleResourceEntity> spec);
}