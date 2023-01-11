package simplyrestful.api.framework.filters;

import java.io.IOException;
import java.net.URI;

import jakarta.inject.Named;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.ext.Provider;

@Named
@PreMatching
@Provider
public class UriCustomizer implements ContainerRequestFilter {
    public static final String CONFIGURATION_PROPERTY_NAME = "SIMPLYRESTFUL_URI_HTTP_HEADER";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
	String httpHeaderName = getHttpHeaderName();
	if(httpHeaderName == null || httpHeaderName.isBlank()) {
	    return;
	}
	String uriFromHttpHeader = requestContext.getHeaderString(httpHeaderName);
	if(uriFromHttpHeader == null || uriFromHttpHeader.isBlank()) {
	    return;
	}
	URI fromHttpHeader = URI.create(uriFromHttpHeader);
	URI newBaseUri = buildNewBaseUri(
		requestContext.getUriInfo().getBaseUri(),
		requestContext.getUriInfo().getRequestUri(),
		fromHttpHeader);
	if(newBaseUri == null) {
	    return;
	}
	requestContext.setRequestUri(newBaseUri, fromHttpHeader);
    }

    private URI buildNewBaseUri(URI originalBaseUri, URI originalRequestUri, URI newRequestUri) {
	if (originalBaseUri.equals(originalRequestUri)) {
	    return newRequestUri;
	}
	String originalRelativePath = originalBaseUri.relativize(originalRequestUri).getPath();
	String newBasePath = newRequestUri.getPath().replaceAll(originalRelativePath+".*", "");
	if(newBasePath.isBlank()) {
	    newBasePath = "/";
	}
	return UriBuilder.fromUri(newRequestUri).replacePath(newBasePath).build();
    }

    private String getHttpHeaderName() {
	String httpHeaderName = System.getProperty(CONFIGURATION_PROPERTY_NAME);
	if (httpHeaderName == null) {
	    httpHeaderName = System.getenv(CONFIGURATION_PROPERTY_NAME);
	}
	return httpHeaderName;
    }
}
