package example.springboot.resources;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.apache.cxf.jaxrs.ext.search.SearchContext;

import jakarta.inject.Named;

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
