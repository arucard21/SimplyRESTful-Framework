package example.jersey.nomapping.resources;

import java.util.Optional;
import java.util.UUID;

import javax.inject.Named;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

@Named
public interface ExampleRepository extends PagingAndSortingRepository<ExampleResource, Long>, JpaSpecificationExecutor<ExampleResource>{
	Optional<ExampleResource> findByUuid(UUID uuid);
}
