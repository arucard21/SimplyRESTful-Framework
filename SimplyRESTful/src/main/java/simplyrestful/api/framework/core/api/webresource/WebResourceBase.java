package simplyrestful.api.framework.core.api.webresource;

import java.net.URI;
import java.util.UUID;

import javax.ws.rs.core.UriInfo;

import io.openapitools.jackson.dataformat.hal.HALLink;
import simplyrestful.api.framework.resources.HALResource;

public interface WebResourceBase<T extends HALResource> {
    public static final String ERROR_SELF_LINK_ID_DOES_NOT_MATCH_PROVIDED_ID = "The provided resource contains an self-link that does not match the ID used in the request";
    public static final String ERROR_SELF_LINK_URI_DOES_NOT_MATCH_API_BASE_URI = "The identifier of the resource does not correspond to the base URI of this Web Resource";
    public static final String ERROR_RESOURCE_WITH_ID_EXISTS = "A resource with the same ID already exists. Try to update the resource with a PUT request to the URI for that resource.";
    public static final String ERROR_RESOURCE_WITH_ID_NOT_EXISTS = "A resource with the provided ID does not exist. Try to create the resource with a POST request to the collection URI.";

    public static final String MEDIA_TYPE_HAL_PARAMETER_PROFILE_NAME = "profile";

    public static final String V1_QUERY_PARAM_PAGE = "page";
    public static final String V1_QUERY_PARAM_COMPACT = "compact";
    public static final String QUERY_PARAM_PAGE_START = "pageStart";
    public static final String QUERY_PARAM_PAGE_SIZE = "pageSize";
    public static final String QUERY_PARAM_FIELDS = "fields";
    public static final String QUERY_PARAM_QUERY = "query";
    public static final String QUERY_PARAM_SORT = "sort";

    public static final String QUERY_PARAM_PAGE_START_DEFAULT = "0";
    public static final String QUERY_PARAM_PAGE_SIZE_DEFAULT = "100";
    public static final String QUERY_PARAM_FIELDS_DEFAULT = "_links.self,_links.first,_links.last,_links.prev,_links.next,total,_embedded.item._links.self";
    public static final String QUERY_PARAM_FIELDS_ALL= "all";
    public static final String QUERY_PARAM_FIELDS_COMPACT = "_links.self,_links.first,_links.last,_links.prev,_links.next,total,_links.item._links.self";
    public static final String QUERY_PARAM_QUERY_DEFAULT = "";
    public static final String QUERY_PARAM_SORT_DEFAULT = "";

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
    default URI getAbsoluteWebResourceURI(UriInfo uriInfo, Class<?> webResource, UUID id) {
        if (id == null) {
            return uriInfo.getBaseUriBuilder().path(webResource).build();
        }
        return uriInfo.getBaseUriBuilder().path(webResource).path(id.toString()).build();
    }

    /**
     * Get the absolute URI for the web resource with the given ID.
     *
     * Example:
     * https://example.com/api/resource/00000000-0000-0000-0000-000000000000
     *
     * @param id is the ID of the resource provided on the endpoint.
     * @return the absolute URI for the resource on the endpoint.
     */
    default URI getAbsoluteWebResourceURI(UriInfo uriInfo, UUID id) {
        return getAbsoluteWebResourceURI(uriInfo, this.getClass(), id);
    }

    /**
     * Get the absolute base URI for this web resource.
     *
     * Example: https://example.com/api/resource/
     *
     * @return the absolute base URI for this resource
     */
    default URI getAbsoluteWebResourceURI(UriInfo uriInfo) {
        return getAbsoluteWebResourceURI(uriInfo, null);
    }

    default URI getRequestURI(UriInfo uriInfo) {
        return uriInfo.getRequestUri();
    }

    /**
     * Create a {@link HALLink} that refers to the provided resource URI with the
     * given profile.
     *
     * Note that the media type is always set to HAL+JSON.
     *
     * @param resourceURI     is the URI of the resource to which this
     *                        {@link HALLink} refers
     * @param resourceProfile is the URI of the profile describing the resource to
     *                        which this {@link HALLink} refers
     * @return a {@link HALLink} that refers to the provided URI with the given
     *         profile
     */
    default HALLink createLink(URI resourceURI, String mediaType, URI resourceProfile) {
        return new HALLink.Builder(resourceURI)
        	.type(mediaType)
        	.profile(resourceProfile)
        	.build();
    }

    default UUID parseUuidFromResourceUri(UriInfo uriInfo, URI resourceUri) {
        URI relativizedResourceUri = getAbsoluteWebResourceURI(uriInfo).relativize(resourceUri);
        if (relativizedResourceUri.equals(resourceUri)) {
            return null;
        }
        UUID resourceIdFromSelf = UUID.fromString(relativizedResourceUri.getPath());
        return resourceIdFromSelf;
    }
}
