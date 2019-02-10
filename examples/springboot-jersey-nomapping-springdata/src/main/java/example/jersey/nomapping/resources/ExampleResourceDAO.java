package example.jersey.nomapping.resources;

import javax.inject.Inject;
import javax.inject.Named;

import simplyrestful.api.framework.core.ResourceDAO;


@Named
public class ExampleResourceDAO extends ResourceDAO<ExampleResource, ExampleResource> {
	@Inject
	public ExampleResourceDAO(ExampleEntityDAO entityDao) {
		super(entityDao);
	}
}
