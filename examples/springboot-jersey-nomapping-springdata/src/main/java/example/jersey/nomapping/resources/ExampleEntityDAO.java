package example.jersey.nomapping.resources;

import simplyrestful.springdata.repository.SpringDataEntityDAO;

public class ExampleEntityDAO extends SpringDataEntityDAO<ExampleResource> {
	public ExampleEntityDAO(ExampleRepository repo) {
		super(repo);
	}
}
