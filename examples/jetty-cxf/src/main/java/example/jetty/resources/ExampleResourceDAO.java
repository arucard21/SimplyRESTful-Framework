package example.jetty.resources;

import simplyrestful.api.framework.core.ResourceDAO;

public class ExampleResourceDAO extends ResourceDAO<ExampleResource, ExampleResource> {
	public ExampleResourceDAO() {
		super(new ExampleEntityDAO());
	}
}
