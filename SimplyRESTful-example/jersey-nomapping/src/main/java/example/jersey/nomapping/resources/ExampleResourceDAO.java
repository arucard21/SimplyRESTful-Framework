package example.jersey.nomapping.resources;

import javax.inject.Inject;
import javax.inject.Named;

import simplyrestful.springdata.repository.nomapping.NoMappingResourceDAO;


@Named
public class ExampleResourceDAO extends NoMappingResourceDAO<ExampleResource> {
	@Inject
	public ExampleResourceDAO(ExampleRepository repo) {
		super(repo);
	}
}
