package example.jersey.nomapping.resources;

import javax.inject.Inject;
import javax.inject.Named;

import simplyrestful.springdata.repository.SpringDataEntityDAO;

@Named
public class ExampleEntityDAO extends SpringDataEntityDAO<ExampleResource> {
	@Inject
	public ExampleEntityDAO(ExampleRepository repo) {
		super(repo);
	}
}
