package example.springboot.resources;

import java.io.IOException;

import javax.inject.Named;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.apache.cxf.jaxrs.ext.search.SearchContext;

import simplyrestful.springboot.CustomRequestPropertiesFilter;

@Named
@Provider
public class AdditionalRequestPropertiesFilter extends CustomRequestPropertiesFilter{
	@Context
	private SearchContext searchContext;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		super.filter(requestContext);
		ExampleResourceDAO.REQUEST_SEARCHCONTEXT.set(searchContext);
	}

}
