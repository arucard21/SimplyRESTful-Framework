package example.nlgov_adr;

import java.io.IOException;

import jakarta.inject.Named;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;

/**
 * Respond with "404 Not Found" to any request that has a trailing slash.
 */
@Named
public class TrailingSlashFilter implements ContainerRequestFilter {
	public static final String API_VERSION_HTTP_HEADER_NAME = "API-Version";
	public static final String API_VERSION_ENV_NAME = "API_VERSION";

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		if(requestContext.getUriInfo().getPath().endsWith("/")) {
			throw new NotFoundException("The API does not accept trailing slashes in the URI");
		}
	}
}
