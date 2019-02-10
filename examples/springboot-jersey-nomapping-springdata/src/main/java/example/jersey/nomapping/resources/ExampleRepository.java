package example.jersey.nomapping.resources;

import javax.inject.Named;

import simplyrestful.springdata.repository.SpringDataRepository;

@Named
public interface ExampleRepository extends SpringDataRepository<ExampleResource>{/* No additional code required*/}
