package example.springboot.resources;

import java.io.IOException;

import jakarta.inject.Named;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;

import org.apache.cxf.jaxrs.ext.search.SearchContext;

@Named
@Provider
public class AdditionalRequestPropertiesFilter implements ContainerRequestFilter {
	@Context
	private SearchContext searchContext;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		ExampleWebResource.REQUEST_SEARCHCONTEXT.set(searchContext);
	}

}
