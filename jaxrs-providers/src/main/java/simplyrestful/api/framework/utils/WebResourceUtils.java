package simplyrestful.api.framework.utils;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import jakarta.ws.rs.core.UriInfo;

public interface WebResourceUtils {
    /**
     * Get the absolute URI for a different web resource with the given ID.
     *
     * The different web resource has to be hosted on the same application root as
     * this web resource. This will likely already be the case when you have access
     * to the Class object of that web resource
     *
     * @param webResource is the class of the @Path-annotated endpoint class for the
     *                    resource
     * @param id          is the UUID of the resource, which can be null if the base
     *                    URI is requested.
     * @return the absolute URI for the resource on the requested endpoint.
     */
    public static URI getAbsoluteWebResourceUri(UriInfo uriInfo, Class<?> webResource, UUID id) {
        if (id == null) {
            return uriInfo.getBaseUriBuilder().path(webResource).build();
        }
        return uriInfo.getBaseUriBuilder().path(webResource).path(id.toString()).build();
    }

    /**
     * Get the absolute URI of the current request URI with the provided UUID appended to it.
     *
     * Any query parameter, matrix parameters, and fragments are removed from the request URI.
     *
     * @param uriInfo is a JAX-RS context object that contains the current request URI.
     * @param id is the UUID of the resource (must not be null)
     * @return the absolute URI of the current request URI with the provided UUID appended to it.
     */
    public static URI getAbsoluteWebResourceUri(UriInfo uriInfo, UUID id) {
        return uriInfo.getRequestUriBuilder()
        		.replaceQuery(null)
        		.replaceMatrix(null)
        		.fragment(null)
        		.path(id.toString())
        		.build();
    }

    /**
     * Get the absolute URI of the current request URI.
     *
     * Any query parameter, matrix parameters, and fragments are removed from the request URI.
     *
     * @param uriInfo is a JAX-RS context object that contains the current request URI.
     * @return the absolute URI of the current request URI.
     */
	public static URI getAbsoluteWebResourceUri(UriInfo uriInfo) {
		return uriInfo.getRequestUriBuilder()
				.replaceQuery(null)
				.replaceMatrix(null)
				.fragment(null)
				.build();
	}

    public static UUID parseUuidFromLastSegmentOfUri(URI resourceUri) {
    	Path uriPath = Paths.get(resourceUri.getPath());
    	String lastSegment = uriPath.getName(uriPath.getNameCount() - 1).toString();
    	return UUID.fromString(lastSegment);
    }
}
