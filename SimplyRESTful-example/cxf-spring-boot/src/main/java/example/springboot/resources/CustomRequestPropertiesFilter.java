package example.springboot.resources;

import java.io.IOException;

import javax.inject.Named;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import org.apache.cxf.jaxrs.ext.search.SearchContext;

@Named
@Provider
public class CustomRequestPropertiesFilter implements ContainerRequestFilter{
	@Context
	private UriInfo uriInfo;
	@Context
	private Request request;
	@Context
	private SearchContext searchContext;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		ExampleResourceDAO.ABSOLUTE_BASE_URI.set(uriInfo.getBaseUriBuilder().replaceQuery(null).build());
		ExampleResourceDAO.REQUEST_SEARCHCONTEXT.set(searchContext);
	}

}
