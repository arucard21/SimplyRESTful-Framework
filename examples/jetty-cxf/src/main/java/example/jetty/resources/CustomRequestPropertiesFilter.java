package example.jetty.resources;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.apache.cxf.jaxrs.ext.search.SearchContext;

@Provider
public class CustomRequestPropertiesFilter implements ContainerRequestFilter{
	@Context
	private SearchContext searchContext;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		ExampleResourceDAO.REQUEST_SEARCHCONTEXT.set(searchContext);
	}

}
