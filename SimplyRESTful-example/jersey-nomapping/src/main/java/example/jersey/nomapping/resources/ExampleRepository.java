package example.jersey.nomapping.resources;

import java.util.UUID;

import javax.inject.Named;

import simplyrestful.springdata.repository.nomapping.NoMappingRepository;

@Named
public interface ExampleRepository extends NoMappingRepository<ExampleResource, UUID>{/* No additional code required*/}
