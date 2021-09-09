package example.microprofile.quarkus.microprofile.quarkus.resources;

import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import example.resources.jpa.ExampleResource;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class ExampleRepository implements PanacheRepository<ExampleResource> {
	public Optional<ExampleResource> findByUuid(UUID uuid) {
	    return find("uuid", uuid).firstResultOptional();
	}

	public boolean existsByUuid(UUID uuid) {
	    return findByUuid(uuid).isPresent();
	}
}
