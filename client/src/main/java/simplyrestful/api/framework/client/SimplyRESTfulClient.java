package simplyrestful.api.framework.client;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriBuilder;

import io.openapitools.jackson.dataformat.hal.HALLink;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import simplyrestful.api.framework.resources.HALCollection;
import simplyrestful.api.framework.resources.HALResource;
import simplyrestful.api.framework.resources.HALServiceDocument;

public class SimplyRESTfulClient<T extends HALResource> {
	private static final String HAL_MEDIA_TYPE_ATTRIBUTE_PROFILE = "profile";
	private static final String MEDIA_TYPE_HAL_JSON_TYPE = "application";
	private static final String MEDIA_TYPE_HAL_JSON_SUBTYPE = "hal+json";
	private static final String MEDIA_TYPE_HAL_JSON = MEDIA_TYPE_HAL_JSON_TYPE + "/" + MEDIA_TYPE_HAL_JSON_SUBTYPE;
	private static final String QUERY_PARAM_PAGE = "page";
	private static final String QUERY_PARAM_PAGESIZE = "pageSize";
	private static final String QUERY_PARAM_COMPACT = "compact";
	private Class<T> resourceClass;
	private URI baseApiUri;
	private URI resourceUri;
	private String resourceProfile;
	private MediaType resourceMediaType;
	private Client client;

	@SuppressWarnings("unchecked")
	public SimplyRESTfulClient(URI baseApiUri) {
		this.baseApiUri = baseApiUri;
		this.client = ClientBuilder.newClient();
		this.resourceClass = (Class<T>) new APIResource().getRawType();
		this.resourceUri = discoverResourceURI();
		detectResourceMediaType();
	}

	private void detectResourceMediaType() {
		try {
			this.resourceProfile = resourceClass.newInstance().getProfile().toString();
			HashMap<String, String> parameters = new HashMap<>();
			parameters.put(HAL_MEDIA_TYPE_ATTRIBUTE_PROFILE, resourceProfile);
			this.resourceMediaType = new MediaType(MEDIA_TYPE_HAL_JSON_TYPE, MEDIA_TYPE_HAL_JSON_SUBTYPE, parameters);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalArgumentException("Resource class could not be instantiated", e);
		}
	}

	/**
	 * Discover the resource URI for the HAL resource T for the API at baseApiUri.
	 *
	 * This discovery is done by accessing the OpenAPI Specification v2 document which is linked in the
	 * HAL Service Document located at the root of the API (baseApiUri). Here, we find the GET path for
	 * the media type matching that of the HAL resource T, which is the resource URI.
	 *
	 * The limitation is that the GET operation on the resource must be available in order for that
	 * resource's URI to be discoverable.
	 *
	 * @return the discovered resource URI.
	 */
	private URI discoverResourceURI() {
		HALServiceDocument serviceDocument = client
				.target(baseApiUri)
				.request()
				.get(HALServiceDocument.class);
		URI openApiJson = URI.create(serviceDocument.getDescribedby().getHref());
		Swagger openApiSpecification = client
				.target(openApiJson)
				.request()
				.get(Swagger.class);

		for(Entry<String, Path> pathEntry : openApiSpecification.getPaths().entrySet()) {
			Operation getHttpMethod = pathEntry.getValue().getGet();
			boolean containsPathParameter = false;
			for(Parameter parameter: getHttpMethod.getParameters()) {
				if("path".equals(parameter.getIn())){
					containsPathParameter = true;
					break;
				}
			}
			if(containsPathParameter) {
				continue;
			}
			boolean matchingMediaType = getHttpMethod.getProduces()
					.stream()
					.map(MediaType::valueOf)
					.anyMatch(mediaType -> mediaType.equals(resourceMediaType));
			if(matchingMediaType) {
				String basePath = openApiSpecification.getBasePath();
				if (basePath == null) {
					return UriBuilder.fromUri(baseApiUri).path(pathEntry.getKey()).build();
				}
				return UriBuilder.fromUri(baseApiUri).path(basePath).path(pathEntry.getKey()).build();
			}

		}
		throw new IllegalArgumentException(
				String.format(
						"The API at %s does not provide resources formatted as HAL+JSON with profile %s",
						baseApiUri.toString(),
						resourceProfile));

	}

	/**
	 * List the full API resource using the API's default paging configuration.
	 *
	 * @param page is the number of the page. If negative, will not be included in the HTTP request.
	 * @param pageSize is the size of each page. If negative, will not be included in the HTTP request.
	 * @return a list of API resources from the page corresponding to the provided parameters.
	 */
	public List<T> listResources(int page, int pageSize){
		return retrievePagedCollection(page, pageSize, false).getItemEmbedded();
	}

	/**
	 * Retrieve a list of API resource identifiers for a page of API resources.
	 *
	 * @param page is the number of the page. If negative, will not be included in the HTTP request.
	 * @param pageSize is the size of each page. If negative, will not be included in the HTTP request.
	 * @return a list of resource identifiers from the page corresponding to the provided parameters.
	 */
	public List<UUID> listResourceIdentifiers(int page, int pageSize){
		return retrievePagedCollection(page, pageSize, true).getItem().stream()
				.filter(selfLink -> Objects.isNull(selfLink.getTemplated()) || !selfLink.getTemplated())
				.map(selfLink -> {
					URI resourceInstanceUri = URI.create(selfLink.getHref());
					URI relativizedURI = resourceUri.relativize(resourceInstanceUri);
					return UUID.fromString(relativizedURI.getPath());
				})
				.collect(Collectors.toList());
	}

	/**
	 * Retrieve HAL Collection resource containing a page of API resources.
	 *
	 * @param page is the number of the page. If negative, will not be included in the HTTP request.
	 * @param pageSize is the size of each page. If negative, will not be included in the HTTP request.
	 * @param compact returns only a resource identifier, if true. If false, each resource will be entirely embedded in the list.
	 * @return the entire collection resource that was retrieved, containing either resource identifiers or embedded resources.
	 */
	public HALCollection<T> retrievePagedCollection(int page, int pageSize, boolean compact){
		WebTarget target = client.target(resourceUri);
		if(page >= 0) {
			target = target.queryParam(QUERY_PARAM_PAGE, page);
		}
		if(pageSize >= 0) {
			target = target.queryParam(QUERY_PARAM_PAGESIZE, pageSize);
		}
		return target
				.queryParam(QUERY_PARAM_COMPACT, compact)
				.request()
				.get(new APICollection());
	}

	/**
	 * Retrieve a single API resource.
	 *
	 * @param resourceId is the id of the resource
	 * @return the API resource at the given URI
	 */
	public T read(UUID resourceId){
		URI resourceInstanceURI = UriBuilder.fromUri(resourceUri).path(resourceId.toString()).build();
		return client.target(resourceInstanceURI).request().get(resourceClass);
	}

	/**
	 * Create a new API resource.
	 *
	 * @param resource is the new resource
	 * @return the id for the created resource, applicable to the provided base URI
	 */
	public UUID create(T resource) {
		String location = client
				.target(resourceUri)
				.request()
				.post(Entity.entity(resource, MEDIA_TYPE_HAL_JSON)).getHeaderString(HttpHeaders.LOCATION);
		URI relativizedURI = resourceUri.relativize(URI.create(location));
		return UUID.fromString(relativizedURI.getPath());
	}

	/**
	 * Update an existing API resource.
	 *
	 * This may also create a new resource at the absolute URI provided in the resource.
	 * The return value indicates whether a new resource was created or not.
	 *
	 * @param resource is the updated resource
	 * @return true if the resource was newly created, false if an existing resource was updated
	 */
	public boolean update(T resource) {
		URI resourceInstanceURI = URI.create(resource.getSelf().getHref());
		if(resourceUri.relativize(resourceInstanceURI).equals(resourceInstanceURI)) {
			throw new IllegalArgumentException("The identifier of the resource does not correspond to the API in this client");
		}
		StatusType statusCode = client
				.target(resourceInstanceURI)
				.request()
				.put(Entity.entity(resource, MEDIA_TYPE_HAL_JSON)).getStatusInfo();
		return statusCode == Status.CREATED;
	}

	/**
	 * Remove an API resource.
	 *
	 * @param resourceId is the id of the resource
	 * @return true if the resource was deleted, false otherwise
	 */
	public boolean delete(UUID resourceId) {
		URI resourceInstanceURI = UriBuilder.fromUri(resourceUri).path(resourceId.toString()).build();
		StatusType statusCode = client
				.target(resourceInstanceURI)
				.request()
				.delete().getStatusInfo();
		return statusCode == Status.NO_CONTENT;
	}

	/**
	 * Provide a JAX-RS Client's WebTarget to the URI for a hypermedia control on the resource.
	 *
	 * This WebTarget can be used to send the request to the API after customizing as needed for the resource.
	 *
	 * @param action is the hypermedia control, as provided in the resource.
	 * @return a WebTarget (from a JAX-RS client) configured with the URI for the provided action.
	 */
	public WebTarget hypermediaControls(HALLink action) {
		return client.target(action.getHref());
	}

	private final class APICollection extends GenericType<HALCollection<T>> { /** Class representing the collection of API resources **/ }
	private final class APIResource extends GenericType<T> { /** Class representing the API resource **/ }
}
