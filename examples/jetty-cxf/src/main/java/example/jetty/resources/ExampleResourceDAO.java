package example.jetty.resources;

import simplyrestful.api.framework.core.DefaultNoMappingResourceDAO;

public class ExampleResourceDAO extends DefaultNoMappingResourceDAO<ExampleResource> {
	public ExampleResourceDAO() {
		super(new ExampleEntityDAO());
	}
}
