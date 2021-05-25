package simplyrestful.api.framework.client;

import java.io.StringReader;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.openapitools.jackson.dataformat.hal.HALLink;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.parser.OpenAPIV3Parser;
import simplyrestful.api.framework.providers.JacksonHALJsonProvider;
import simplyrestful.api.framework.providers.ObjectMapperProvider;
import simplyrestful.api.framework.resources.HALCollectionV2;
import simplyrestful.api.framework.resources.HALResource;
import simplyrestful.api.framework.resources.HALServiceDocument;

@Deprecated(since = "0.15.0" )
public class SimplyRESTfulClient<T extends HALResource> {
    private static final String MEDIA_TYPE_SERVICE_DOCUMENT_HAL_JSON = "application/hal+json; profile=\""+HALServiceDocument.PROFILE_STRING + "\"";
    private static final String ERROR_UPDATE_RESOURCE_DOES_NOT_EXIST = "The resource does not exist yet. Use create() if you wish to create a new resource.";
    private static final String ERROR_INVALID_RESOURCE_URI = "The identifier of the resource does not correspond to the API in this client";
    private static final String HAL_ITEM_KEY = "item";
    private static final String HAL_EMBEDDED_KEY = "_embedded";
    private static final String HAL_MEDIA_TYPE_ATTRIBUTE_PROFILE = "profile";
    private static final String MEDIA_TYPE_HAL_JSON_TYPE = "application";
    private static final String MEDIA_TYPE_HAL_JSON_SUBTYPE = "hal+json";
    private static final String QUERY_PARAM_PAGE_START = "pageStart";
    private static final String QUERY_PARAM_PAGESIZE = "pageSize";
    private static final String QUERY_PARAM_FIELDS = "fields";
    private static final String QUERY_PARAM_QUERY = "query";
    private static final String QUERY_PARAM_SORT= "sort";
    private final Class<T> resourceClass;
    private final URI baseApiUri;
    private final String resourceProfile;
    private final MediaType resourceMediaType;
    private final Client client;
    private URI resourceUri;

    SimplyRESTfulClient(Client client, URI baseApiUri, Class<T> resourceClass) {
	this.baseApiUri = baseApiUri;
	this.client = client;
	client.register(JacksonHALJsonProvider.class);
	client.register(ObjectMapperProvider.class);
	this.resourceClass = resourceClass;
	this.resourceProfile = discoverResourceProfile();
	this.resourceMediaType = detectResourceMediaType();
    }

    private String discoverResourceProfile() {
	try {
	    return resourceClass.getDeclaredConstructor().newInstance().getProfile().toString();
	} catch (ReflectiveOperationException e) {
	    throw new IllegalArgumentException(
		    "Resource class could not be instantiated so the profile and media type could not be detected", e);
	}
    }

    private MediaType detectResourceMediaType() {
	HashMap<String, String> parameters = new HashMap<>();
	parameters.put(HAL_MEDIA_TYPE_ATTRIBUTE_PROFILE, resourceProfile);
	return new MediaType(MEDIA_TYPE_HAL_JSON_TYPE, MEDIA_TYPE_HAL_JSON_SUBTYPE, parameters);
    }

    /**
     * Discover the resource URI for the HAL resource T for the API at baseApiUri.
     *
     * This discovery is done by accessing the OpenAPI Specification document which
     * is linked in the HAL Service Document located at the root of the API
     * (baseApiUri). Here, we find the GET path for the media type matching that of
     * the HAL resource T, which is the resource URI.
     *
     * The limitation is that the GET operation on the resource must be available in
     * order for that resource's URI to be discoverable.
     *
     * @param headers is the set of additional HTTP headers that should be used in
     *                the request.
     * @return the discovered resource URI.
     */
    private void discoverResourceURI(MultivaluedMap<String, Object> headers) {
	if (Objects.nonNull(resourceUri)) {
	    return;
	}
	Builder request = client.target(baseApiUri).request();
	configureHttpHeaders(request, headers);
	request.accept(MEDIA_TYPE_SERVICE_DOCUMENT_HAL_JSON);
	HALServiceDocument serviceDocument = request.get(HALServiceDocument.class);
	URI openApiDocumentUri = URI.create(serviceDocument.getDescribedBy().getHref());
	OpenAPI openApiSpecification = new OpenAPIV3Parser().read(openApiDocumentUri.toString());
	for (Entry<String, PathItem> pathEntry : openApiSpecification.getPaths().entrySet()) {
	    Operation getHttpMethod = pathEntry.getValue().getGet();
	    if (Objects.isNull(getHttpMethod)) {
		break;
	    }
	    boolean matchingMediaType = getHttpMethod.getResponses().getDefault().getContent().keySet().stream()
		    .map(MediaType::valueOf).anyMatch(mediaType -> mediaType.equals(resourceMediaType));
	    if (matchingMediaType) {
		String resourcePath = pathEntry.getKey();
		for (Parameter parameter : getHttpMethod.getParameters()) {
		    if ("path".equals(parameter.getIn())) {
			resourcePath = resourcePath.replaceAll(String.format("\\{%s\\}", parameter.getName()), "");
			resourcePath = resourcePath.replaceAll("//", "/");
		    }
		}
		resourceUri = UriBuilder.fromUri(openApiSpecification.getServers().get(0).getUrl())
			.scheme(baseApiUri.getScheme()).host(baseApiUri.getHost()).userInfo(baseApiUri.getUserInfo())
			.port(baseApiUri.getPort()).path(resourcePath).build();
		break;
	    }
	}
	if (Objects.isNull(resourceUri)) {
	    throw new IllegalArgumentException(
		    String.format("The API at %s does not provide resources formatted as HAL+JSON with profile %s",
			    baseApiUri.toString(), resourceProfile));
	}
    }

    /**
     * List the full API resource using the API's default paging configuration.
     *
     * @param page     is the number of the page. If zero or negative, will not be
     *                 included in the HTTP request.
     * @param pageSize is the size of each page. If zero or negative, will not be
     *                 included in the HTTP request.
     * @return a list of API resources from the page corresponding to the provided
     *         parameters.
     */
    public List<T> listResources(int page, int pageSize, String fields, String query, String sort) {
	return listResources(page, pageSize, fields, query, sort, null, null);
    }

    /**
     * List the full API resource using the API's default paging configuration.
     *
     * @param page            is the number of the page. If zero or negative, will
     *                        not be included in the HTTP request.
     * @param pageSize        is the size of each page. If zero or negative, will
     *                        not be included in the HTTP request.
     * @param additionalHeaders         is the set of additional HTTP headers that should be
     *                        used in the request.
     * @param additionalQueryParameters is the set of queryparameter that should be used in
     *                        the request
     * @return a list of API resources from the page corresponding to the provided
     *         parameters.
     */
    public List<T> listResources(
	    int page,
	    int pageSize,
	    String fields,
	    String query,
	    String sort,
	    MultivaluedMap<String, Object> additionalHeaders,
	    MultivaluedMap<String, Object> additionalQueryParameters) {
	discoverResourceURI(additionalHeaders);
	return retrievePagedCollection(page, pageSize, fields, query, sort, additionalHeaders, additionalQueryParameters).getItem();
    }

    /**
     * Retrieve HAL Collection resource containing a page of API resources.
     *
     * @param page            is the number of the page. If negative, will not be
     *                        included in the HTTP request.
     * @param pageSize        is the size of each page. If negative, will not be
     *                        included in the HTTP request.
     * @param compact         returns only a resource identifier, if true. If false,
     *                        each resource will be entirely embedded in the list.
     * @param headers         is the set of additional HTTP headers that should be
     *                        used in the request.
     * @param queryParameters is the set of queryparameter that should be used in
     *                        the request
     * @return the entire collection resource that was retrieved, containing either
     *         resource identifiers or embedded resources.
     */
    @SuppressWarnings("unchecked")
    private HALCollectionV2<T> retrievePagedCollection(int pageStart, int pageSize, String fields, String query, String sort, MultivaluedMap<String, Object> headers, MultivaluedMap<String, Object> queryParameters) {
	WebTarget target = client.target(resourceUri);
	if (pageStart >= 0) {
	    target = target.queryParam(QUERY_PARAM_PAGE_START, pageStart);
	}
	if (pageSize >= 0) {
	    target = target.queryParam(QUERY_PARAM_PAGESIZE, pageSize);
	}
	if(!fields.isBlank()) {
	    target = target.queryParam(QUERY_PARAM_FIELDS, fields);
	}
	if(!query.isBlank()) {
	    target = target.queryParam(QUERY_PARAM_QUERY, query);
	}
	if(!sort.isBlank()) {
	    target = target.queryParam(QUERY_PARAM_SORT, sort);
	}
	configureAdditionalQueryParameters(target, queryParameters);
	Builder request = target.request();
	request.accept(HALCollectionV2.MEDIA_TYPE_HAL_JSON);
	configureHttpHeaders(request, headers);
	String nonDeserialized = request.get(String.class);
	HALCollectionV2<T> collection;
	collection = (HALCollectionV2<T>) deserializeJsonWithGenerics(nonDeserialized, new TypeReference<HALCollectionV2<BasicHALResource>>() {});
	JsonObject embedded = Json.createReader(new StringReader(nonDeserialized)).readObject()
		.getJsonObject(HAL_EMBEDDED_KEY);
	if (Objects.nonNull(embedded)) {
	    collection.setItem(embedded.getJsonArray(HAL_ITEM_KEY).stream()
		    .filter(Objects::nonNull)
		    .map(jsonValue -> deserializeJson(jsonValue.toString(), resourceClass))
		    .filter(Objects::nonNull)
		    .collect(Collectors.toList()));
	}
	return collection;
    }

    private void configureAdditionalQueryParameters(WebTarget target, MultivaluedMap<String, Object> queryParameters) {
	if (Objects.isNull(queryParameters)) {
	    return;
	}
	for (Entry<String, List<Object>> queryParameter : queryParameters.entrySet()) {
	    target.queryParam(queryParameter.getKey(), queryParameter.getValue().toArray());
	}
    }

    private void configureHttpHeaders(Builder request, MultivaluedMap<String, Object> headers) {
	if (Objects.isNull(headers)) {
	    return;
	}
	headers.forEach((headerName, headerValue) -> request.header(headerName, headerValue));
    }

    private <S> S deserializeJson(String jsonString, Class<S> deserializationClass) {
	try {
	    return getHALMapper().readValue(jsonString, deserializationClass);
	} catch (JsonProcessingException e) {
	    throw new RuntimeException(e);
	}
    }

    private <S> S deserializeJsonWithGenerics(String jsonString, TypeReference<S> typeRef) {
	try {
	    return getHALMapper().readValue(jsonString, typeRef);
	} catch (JsonProcessingException e) {
	    throw new RuntimeException(e);
	}
    }

    private ObjectMapper getHALMapper() {
	ObjectMapper mapper = new ObjectMapperProvider()
	    .getContext(ObjectMapper.class);
	if(!mapper.getRegisteredModuleIds().contains(JacksonHALJsonProvider.JACKSON_HAL_MODULE.getTypeId())) {
	    mapper.registerModule(JacksonHALJsonProvider.JACKSON_HAL_MODULE);
	}
	return mapper;
    }

    /**
     * Retrieve a single API resource.
     *
     * @param resourceId is the id of the resource.
     * @return the API resource at the given URI.
     */
    public T read(UUID resourceId) {
	return read(resourceId, null, null);
    }

    /**
     * Retrieve a single API resource.
     *
     * @param resourceId      is the id of the resource.
     * @param headers         is the set of additional HTTP headers that should be
     *                        used in the request.
     * @param queryParameters is the set of queryparameter that should be used in
     *                        the request
     * @return the API resource at the given URI.
     */
    public T read(UUID resourceId, MultivaluedMap<String, Object> headers,
	    MultivaluedMap<String, Object> queryParameters) {
	discoverResourceURI(headers);
	URI resourceInstanceURI = UriBuilder.fromUri(resourceUri).path(resourceId.toString()).build();
	WebTarget target = client.target(resourceInstanceURI);
	configureAdditionalQueryParameters(target, queryParameters);
	Builder request = target.request();
	configureHttpHeaders(request, headers);
	request.accept(resourceMediaType);
	return request.get(resourceClass);
    }

    /**
     * Create a new API resource.
     *
     * If the provided resource contains a self link, it will be removed.
     *
     * @param resource is the new resource
     * @return the id for the created resource, applicable to the provided base URI
     */
    public UUID create(T resource) {
	return create(resource, null, null);
    }

    /**
     * Create a new API resource.
     *
     * If the provided resource contains a self link, it will be removed.
     *
     * @param resource        is the new resource
     * @param headers         is the set of additional HTTP headers that should be
     *                        used in the request.
     * @param queryParameters is the set of queryparameter that should be used in
     *                        the request
     * @return the id for the created resource, applicable to the provided base URI
     */
    public UUID create(T resource, MultivaluedMap<String, Object> headers,
	    MultivaluedMap<String, Object> queryParameters) {
	discoverResourceURI(headers);
	if(resource.getSelf() != null) {
	    resource.setSelf(null);
	}
	WebTarget target = client.target(resourceUri);
	configureAdditionalQueryParameters(target, queryParameters);
	Builder request = target.request();
	configureHttpHeaders(request, headers);
	Entity<T> halJsonEntity = Entity.entity(resource, resourceMediaType);
	try (Response response = request.post(halJsonEntity)) {
	    if (!Objects.equals(201, response.getStatus())) {
		throw new WebApplicationException(response);
	    }
	    String location = response.getHeaderString(HttpHeaders.LOCATION);
	    URI relativizedURI = resourceUri.relativize(URI.create(location));
	    return UUID.fromString(relativizedURI.getPath());
	}
    }

    /**
     * Update an existing API resource.
     *
     * @param resource is the updated resource
     */
    public void update(T resource) {
	update(resource, null, null);
    }

    /**
     * Update an existing API resource.
     *
     * @param resource        is the updated resource
     * @param headers         is the set of additional HTTP headers that should be
     *                        used in the request.
     * @param queryParameters is the set of query parameters that should be used in
     *                        the request
     */
    public void update(T resource, MultivaluedMap<String, Object> headers,
	    MultivaluedMap<String, Object> queryParameters) {
	discoverResourceURI(headers);
	URI resourceInstanceURI = URI.create(resource.getSelf().getHref());
	if (!exists(resourceInstanceURI, headers, queryParameters)) {
	    throw new IllegalArgumentException(ERROR_UPDATE_RESOURCE_DOES_NOT_EXIST);
	}
	WebTarget target = client.target(resourceInstanceURI);
	configureAdditionalQueryParameters(target, queryParameters);
	Builder request = target.request();
	configureHttpHeaders(request, headers);
	Response response = request.put(Entity.entity(resource, resourceMediaType));
	if (!Objects.equals(response.getStatusInfo(), Status.OK)) {
	    throw new WebApplicationException(response);
	}
    }

    /**
     * Validates that the given URI refers to to the web resource that is served.
     *
     * The URI should have the same host as the web resource being server. Its path
     * should also be relative to the root of the web resource's path.
     *
     * @param resourceInstanceURI is the URI that is required to be valid.
     * @param headers             is the set of additional HTTP headers that should
     *                            be used in the request.
     * @param queryParameters     is the set of query parameters that should be used
     *                            in the request
     */
    private void validateResourceUri(URI resourceInstanceURI) {
	if (Objects.isNull(resourceInstanceURI) || !resourceUri.getHost().equals(resourceInstanceURI.getHost())
		|| resourceUri.relativize(resourceInstanceURI).equals(resourceInstanceURI)) {
	    throw new IllegalArgumentException(ERROR_INVALID_RESOURCE_URI);
	}
    }

    /**
     * Remove an API resource.
     *
     * @param resourceId is the id of the resource
     */
    public void delete(UUID resourceId) {
	delete(resourceId, null, null);
    }

    /**
     * Remove an API resource.
     *
     * @param resourceId      is the id of the resource
     * @param headers         is the set of additional HTTP headers that should be
     *                        used in the request.
     * @param queryParameters is the set of query parameters that should be used in
     *                        the request
     */
    public void delete(UUID resourceId, MultivaluedMap<String, Object> headers,
	    MultivaluedMap<String, Object> queryParameters) {
	discoverResourceURI(headers);
	URI resourceInstanceURI = UriBuilder.fromUri(resourceUri).path(resourceId.toString()).build();
	WebTarget target = client.target(resourceInstanceURI);
	configureAdditionalQueryParameters(target, queryParameters);
	Builder request = target.request();
	configureHttpHeaders(request, headers);
	Response response = request.delete();
	if (!Objects.equals(response.getStatusInfo(), Status.NO_CONTENT)) {
	    throw new WebApplicationException(response);
	}
    }

    /**
     * Provide a JAX-RS Client's WebTarget to the URI for a hypermedia control on
     * the resource.
     *
     * This WebTarget can be used to send the request to the API after customizing
     * as needed for the resource.
     *
     * @param action is the hypermedia control, as provided in the resource.
     * @return a WebTarget (from a JAX-RS client) configured with the URI for the
     *         provided action.
     */
    public WebTarget hypermediaControl(HALLink action) {
	return client.target(action.getHref());
    }

    /**
     * Check whether a resource the given id exists on the server.
     *
     * @param resourceId is the id of the resource that should be checked.
     * @return true iff the resource exists on the server, false if it does not
     *         exist.
     * @throws WebApplicationException if the client cannot confirm that the
     *                                 resource either exists or does not exist. Is
     *                                 likely caused by an error returned by the
     *                                 server.
     */
    public boolean exists(UUID resourceId) {
	return exists(resourceId, null, null);
    }

    /**
     * Check whether a resource the given id exists on the server.
     *
     * @param resourceId      is the id of the resource that should be checked.
     * @param headers         is the set of additional HTTP headers that should be
     *                        used in the request.
     * @param queryParameters is the set of query parameters that should be used in
     *                        the request
     * @return true iff the resource exists on the server, false if it does not
     *         exist.
     * @throws WebApplicationException if the client cannot confirm that the
     *                                 resource either exists or does not exist. Is
     *                                 likely caused by an error returned by the
     *                                 server.
     */
    public boolean exists(UUID resourceId, MultivaluedMap<String, Object> headers,
	    MultivaluedMap<String, Object> queryParameters) {
	discoverResourceURI(headers);
	URI resourceInstanceURI = UriBuilder.fromUri(resourceUri).path(resourceId.toString()).build();
	return exists(resourceInstanceURI, headers, queryParameters);
    }

    /**
     * Check whether a resource with the given URI exists on the server.
     *
     * @param resourceInstanceURI is the URI of the resource that should be checked.
     * @return true iff the resource exists on the server, false if it does not
     *         exist.
     * @throws WebApplicationException if the client cannot confirm that the
     *                                 resource either exists or does not exist. Is
     *                                 likely caused by an error returned by the
     *                                 server.
     */
    public boolean exists(URI resourceInstanceURI) {
	return exists(resourceInstanceURI, null, null);
    }

    /**
     * Check whether a resource with the given URI exists on the server.
     *
     * @param resourceInstanceURI is the URI of the resource that should be checked.
     * @param headers             is the set of additional HTTP headers that should
     *                            be used in the request.
     * @param queryParameters     is the set of query parameters that should be used
     *                            in the request
     * @return true iff the resource exists on the server, false if it does not
     *         exist.
     * @throws WebApplicationException if the client cannot confirm that the
     *                                 resource either exists or does not exist. Is
     *                                 likely caused by an error returned by the
     *                                 server.
     */
    public boolean exists(URI resourceInstanceURI, MultivaluedMap<String, Object> headers,
	    MultivaluedMap<String, Object> queryParameters) {
	discoverResourceURI(headers);
	validateResourceUri(resourceInstanceURI);
	WebTarget target = client.target(resourceInstanceURI);
	configureAdditionalQueryParameters(target, queryParameters);
	Builder request = target.request();
	configureHttpHeaders(request, headers);
	Response response = request.get();
	int responseStatus = response.getStatus();
	if (Objects.equals(200, responseStatus)) {
	    return true;
	}
	if (Objects.equals(404, responseStatus)) {
	    return false;
	}
	throw new WebApplicationException(response);
    }
}
