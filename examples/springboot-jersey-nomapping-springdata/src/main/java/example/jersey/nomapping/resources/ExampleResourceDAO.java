package example.jersey.nomapping.resources;

import javax.inject.Inject;
import javax.inject.Named;

import simplyrestful.api.framework.core.DefaultNoMappingResourceDAO;


@Named
public class ExampleResourceDAO extends DefaultNoMappingResourceDAO<ExampleResource> {
	@Inject
	public ExampleResourceDAO(ExampleEntityDAO entityDao) {
		super(entityDao);
	}
}
