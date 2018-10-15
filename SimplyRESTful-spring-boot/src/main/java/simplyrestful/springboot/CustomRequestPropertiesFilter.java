package simplyrestful.springboot;

import java.io.IOException;

import javax.inject.Named;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import simplyrestful.api.framework.core.ResourceDAO;

@Named
@Provider
public class CustomRequestPropertiesFilter implements ContainerRequestFilter{
	@Context
	private UriInfo uriInfo;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		ResourceDAO.ABSOLUTE_BASE_URI.set(uriInfo.getAbsolutePath());
	}
}
