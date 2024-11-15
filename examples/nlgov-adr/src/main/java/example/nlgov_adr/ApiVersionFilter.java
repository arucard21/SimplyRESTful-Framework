package example.nlgov_adr;

import java.io.IOException;

import jakarta.inject.Named;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;

/**
 * Add an HTTP header named "API-Version" to every response.
 *
 * It contains the version of the API which can be defined in a property or
 * environment variable called "API_VERSION".
 */
@Named
public class ApiVersionFilter implements ContainerResponseFilter {
	public static final String API_VERSION_HTTP_HEADER_NAME = "API-Version";
	public static final String API_VERSION_ENV_NAME = "API_VERSION";

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		String apiVersion = getApiVersion();
		if(apiVersion != null) {
			responseContext.getHeaders().add(API_VERSION_HTTP_HEADER_NAME, apiVersion);
		}
	}

	private String getApiVersion() {
		String apiVersion = System.getProperty(API_VERSION_ENV_NAME);
		if (apiVersion == null) {
			apiVersion = System.getenv(API_VERSION_ENV_NAME);
		}
		return apiVersion;
	}
}
