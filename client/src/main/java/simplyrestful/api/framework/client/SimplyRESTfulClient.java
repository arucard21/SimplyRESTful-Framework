package simplyrestful.api.framework.client;

import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.NotFoundException;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.OpenAPIV3Parser;
import simplyrestful.api.framework.providers.ObjectMapperProvider;
import simplyrestful.api.framework.queryparams.SortOrder;
import simplyrestful.api.framework.resources.APICollection;
import simplyrestful.api.framework.resources.APIResource;
import simplyrestful.api.framework.resources.APIServiceDocument;
import simplyrestful.api.framework.resources.Link;

public class SimplyRESTfulClient<T extends APIResource> {
	public static final String ERROR_DISCOVER_RESOURCE_URI_REQUIRED = "This method can only be used after the resource URI has been discovered. This is done at every API request but you can trigger it manually by calling discoverResourceUri() directly";
	public static final String QUERY_PARAM_VALUE_DELIMITER = ",";
	public static final String ERROR_UPDATE_RESOURCE_DOES_NOT_EXIST = "The resource does not exist yet. Use create() if you wish to create a new resource.";
	public static final String ERROR_INVALID_RESOURCE_URI = "The identifier of the resource does not correspond to the API in this client";
	public static final String COLLECTION_ITEM_KEY = "item";
	public static final String COLLECTION_TOTAL_KEY = "total";
	public static final String QUERY_PARAM_PAGE_START = "pageStart";
	public static final String QUERY_PARAM_PAGESIZE = "pageSize";
	public static final String QUERY_PARAM_FIELDS = "fields";
	public static final String QUERY_PARAM_QUERY = "query";
	public static final String QUERY_PARAM_SORT = "sort";
    private final Class<T> resourceClass;
    private final URI baseApiUri;
    private final MediaType resourceMediaType;
    private final Client client;
    private UriBuilder resourceUriBuilder;
    private int totalAmountOfLastRetrievedCollection;

    SimplyRESTfulClient(Client client, URI baseApiUri, Class<T> resourceClass) {
        this.baseApiUri = baseApiUri;
        this.client = client;
        client.register(JacksonJsonProvider.class);
        client.register(ObjectMapperProvider.class);
        this.resourceClass = resourceClass;
        this.resourceMediaType = detectResourceMediaType();
    }

    private MediaType detectResourceMediaType() {
    	try {
			return resourceClass.getDeclaredConstructor().newInstance().customJsonMediaType();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
    }

    /**
     * Discover the resource URI for this client's API resource.
     *
     * In order to ensure that any authentication and authorization required to
     * access the API is available, this discovery is done just before any other
     * API request should be made. The same authentication and authorization from
     * that request will be used here, to discover the resource URI.
     *
     * This discovery is done by accessing the OpenAPI Specification document which
     * is linked in the API Service Document located at the root of the API
     * (baseApiUri). Here, we find the GET path for the media type matching that of
     * the API resource T, which is the resource URI.
     *
     * The limitation is that the GET operation on the resource must be available in
     * order for that resource's URI to be discoverable.
     *
     * @param headers is the set of additional HTTP headers that should be used in
     *                the request, containing any authentication and authorization
     *                headers needed to access the API.
     */
    public void discoverResourceUri(MultivaluedMap<String, String> headers) {
        if (resourceUriBuilder != null) {
            return;
        }
        Builder request = client.target(baseApiUri).request();
        configureHttpHeaders(request, headers);
        request.accept(APIServiceDocument.MEDIA_TYPE_JSON);
        APIServiceDocument serviceDocument = request.get(APIServiceDocument.class);
        URI openApiDocumentUri = serviceDocument.getDescribedBy().getHref();
        OpenAPI openApiSpecification = new OpenAPIV3Parser().read(openApiDocumentUri.toString());
        for (Entry<String, PathItem> pathEntry : openApiSpecification.getPaths().entrySet()) {
            Operation getHttpMethod = pathEntry.getValue().getGet();
            if (Objects.isNull(getHttpMethod)) {
                break;
            }
            boolean matchingMediaType = getHttpMethod.getResponses().values().stream()
            		.map(apiResponse -> apiResponse.getContent())
            		.filter(Objects::nonNull)
            		.flatMap(content -> content.keySet().stream())
            		.map(MediaType::valueOf)
            		.anyMatch(mediaType -> mediaType.equals(resourceMediaType));
            if (matchingMediaType) {
                String resourcePath = pathEntry.getKey();
                resourceUriBuilder = UriBuilder.fromUri(baseApiUri).path(resourcePath);
                break;
            }
        }
        if (Objects.isNull(resourceUriBuilder)) {
            throw new IllegalArgumentException(
                    String.format("The API at %s does not provide resources of type %s", baseApiUri.toString(), resourceMediaType.toString()));
        }
    }

    /**
     * List the API resources.
     *
     * @return a list of API resources from the default page corresponding to the default parameters.
     */
    public List<T> listResources() {
    	return listResources(-1, -1, Collections.emptyList(), "", Collections.emptyList(), null, null);
    }

    /**
     * List the API resources for a given page.
     *
     * @param pageStart is the offset at which the requested page starts.
     * @param pageSize is the size of a single page in this paginated collection of resources
     * @return a list of API resources from the page corresponding to the provided parameters.
     */
    public List<T> listResources(
            int pageStart,
            int pageSize) {
    	return listResources(pageStart, pageSize, Collections.emptyList(), "", Collections.emptyList(), null, null);
    }

    /**
     * List the first page of API resources with the given filtering and sorting.
     *
     * @param fields is a list that defines which fields should be retrieved.
     * @param query is a FIQL query that defines how the resources should be filtered.
     * @param sort is a list of field names on which the resources should be sorted.
     * @return a list of API resources from the page corresponding to the provided parameters.
     */
    public List<T> listResources(
            List<String> fields,
            String query,
            List<SortOrder> sort) {
    	return listResources(-1, -1, fields, query, sort, null, null);
    }

    /**
     * List the API resources.
     *
     * @param pageStart is the offset at which the requested page starts.
     * @param pageSize is the size of a single page in this paginated collection of resources
     * @param fields is a list that defines which fields should be retrieved.
     * @param query is a FIQL query that defines how the resources should be filtered.
     * @param sort is a list of field names on which the resources should be sorted.
     * @return a list of API resources from the page corresponding to the provided parameters.
     */
    public List<T> listResources(int pageStart, int pageSize, List<String> fields, String query, List<SortOrder> sort) {
        return listResources(pageStart, pageSize, fields, query, sort, null, null);
    }

    /**
     * List the API resource with String-based values for fields and sort query parameters
     *
     * @param pageStart is the offset at which the requested page starts.
     * @param pageSize is the size of a single page in this paginated collection of resources
     * @param fields is a comma-separated list that defines which fields should be retrieved.
     * @param query is a FIQL query that defines how the resources should be filtered.
     * @param sort is a comma-separated list of field names on which the resources should be sorted, each in
     * the form "field[:(asc|desc)]". You can omit the direction to use the default sort direction defined by the API.
     * @return a list of API resources from the page corresponding to the provided parameters.
     */
    public List<T> listResources(int pageStart, int pageSize, String fields, String query, String sort) {
        return listResources(
                pageStart,
                pageSize,
                fields.isBlank() ? Collections.emptyList() :
                    List.of(fields.split(QUERY_PARAM_VALUE_DELIMITER)),
                query,
                sort.isBlank() ? Collections.emptyList() :
                    Stream.of(sort.split(QUERY_PARAM_VALUE_DELIMITER)).map(SortOrder::from).collect(Collectors.toList()),
                null,
                null);
    }

    /**
     * List the API resources while providing additional HTTP headers and query parameters.
     *
     * @param pageStart is the offset at which the requested page starts. Can be -1
     * @param pageSize is the size of a single page in this paginated collection of resources
     * @param fields is a list that defines which fields should be retrieved.
     * @param query is a FIQL query that defines how the resources should be filtered.
     * @param sort is a list of field names on which the resources should be sorted.
     * @param additionalHeaders is the set of HTTP headers that should be added to the request.
     * @param additionalQueryParameters is the set of query parameters that should be added to the request
     * @return a list of API resources from the page corresponding to the provided parameters.
     */
    public List<T> listResources(
            int pageStart,
            int pageSize,
            List<String> fields,
            String query,
            List<SortOrder> sort,
            MultivaluedMap<String, String> additionalHeaders,
            MultivaluedMap<String, String> additionalQueryParameters) {
        discoverResourceUri(additionalHeaders);
        return retrieveResourcesFromCollection(
        		pageStart,
        		pageSize,
        		fields == null ? Collections.emptyList() : fields,
        		query == null ? "" : query,
        		sort == null ? Collections.emptyList() : sort,
        		additionalHeaders,
        		additionalQueryParameters);
    }

    /**
     * Retrieve the resources from a Collection resource containing a page of API resources.
     *
     * @param pageStart is the offset at which the requested page starts.
     * @param pageSize is the size of a single page in this paginated collection of resources
     * @param fields is a list that defines which fields should be retrieved.
     * @param query is a FIQL query that defines how the resources should be filtered.
     * @param sort is a list of field names on which the resources should be sorted.
     * @param additionalHeaders is the set of HTTP headers that should be added to the request.
     * @param additionalQueryParameters is the set of query parameters that should be added to the request
     * @return the entire collection resource that was retrieved, containing either
     *         resource identifiers or embedded resources.
     */
    private List<T> retrieveResourcesFromCollection(
            int pageStart,
            int pageSize,
            List<String> fields,
            String query,
            List<SortOrder> sort,
            MultivaluedMap<String, String> additionalHeaders,
            MultivaluedMap<String, String> additionalQueryParameters) {
        WebTarget target = client.target(resourceUriBuilder.build(""));
        if (pageStart >= 0) {
            target = target.queryParam(QUERY_PARAM_PAGE_START, pageStart);
        }
        if (pageSize >= 0) {
            target = target.queryParam(QUERY_PARAM_PAGESIZE, pageSize);
        }
        if (!fields.isEmpty()) {
            target = target.queryParam(QUERY_PARAM_FIELDS, fields.toArray());
        }
        if (!query.isBlank()) {
            target = target.queryParam(QUERY_PARAM_QUERY, query);
        }
        if (!sort.isEmpty()) {
            target = target.queryParam(QUERY_PARAM_SORT, sort.toArray());
        }
        configureAdditionalQueryParameters(target, additionalQueryParameters);
        Builder request = target.request();
        request.accept(APICollection.MEDIA_TYPE_JSON);
        configureHttpHeaders(request, additionalHeaders);
        String nonDeserialized = request.get(String.class);
        JsonObject collectionJsonObject = Json.createReader(new StringReader(nonDeserialized)).readObject();
		List<T> resources = Optional.ofNullable(collectionJsonObject.getJsonArray(COLLECTION_ITEM_KEY))
				.map(JsonArray::stream)
				.orElse(Stream.of())
                .filter(Objects::nonNull)
                .map(JsonValue::toString)
                .map(resourceJson -> this.deserializeJson(resourceJson, resourceClass))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        this.totalAmountOfLastRetrievedCollection = Optional.ofNullable(collectionJsonObject.getJsonNumber(COLLECTION_TOTAL_KEY))
        		.map(JsonNumber::intValue)
        		.orElse(-1);
        return resources;
    }

    public int getTotalAmountOfLastRetrievedCollection() {
        return this.totalAmountOfLastRetrievedCollection;
    }

    private void configureAdditionalQueryParameters(WebTarget target, MultivaluedMap<String, String> queryParameters) {
        if (Objects.isNull(queryParameters)) {
            return;
        }
        for (Entry<String, List<String>> queryParameter : queryParameters.entrySet()) {
            target.queryParam(queryParameter.getKey(), queryParameter.getValue().toArray());
        }
    }

    private void configureHttpHeaders(Builder request, MultivaluedMap<String, String> headers) {
        if (Objects.isNull(headers)) {
            return;
        }
        headers.forEach((headerName, headerValue) -> request.header(headerName, headerValue));
    }

    private <S> S deserializeJson(String jsonString, Class<S> deserializationClass) {
        try {
            return getConfiguredObjectMapper().readValue(jsonString, deserializationClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private ObjectMapper getConfiguredObjectMapper() {
        return new ObjectMapperProvider().getContext(ObjectMapper.class);
    }

    /**
     * Retrieve a single API resource referenced with a Link.
     *
     * @param resourceLink is the URI identifier of the resource.
     * @return the API resource at the given URI.
     */
    public T read(Link resourceLink) {
        return read(resourceLink, null, null);
    }

    /**
     * Retrieve a single API resource referenced with a Link.
     *
     * @param resourceLink is the URI identifier of the resource.
     * @return the API resource at the given URI.
     */
    public T read(Link resourceLink, MultivaluedMap<String, String> headers, MultivaluedMap<String, String> queryParameters) {
        return read(resourceLink.getHref(), headers, queryParameters);
    }

    /**
     * Retrieve a single API resource.
     *
     * @param resourceUri is the URI identifier of the resource.
     * @return the API resource at the given URI.
     */
    public T read(URI resourceUri) {
        return read(resourceUri, null, null);
    }

    /**
     * Retrieve a single API resource.
     *
     * @param resourceUri is the URI identifier of the resource.
     * @param headers is the set of additional HTTP headers that should be used in the request.
     * @param queryParameters is the set of query parameters that should be used in the request
     * @return the API resource at the given URI.
     */
    public T read(URI resourceUri, MultivaluedMap<String, String> headers, MultivaluedMap<String, String> queryParameters) {
        discoverResourceUri(headers);
        validateResourceUri(resourceUri);
        WebTarget target = client.target(resourceUri);
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
     * @return the URI identifier for the created resource.
     */
    public URI create(T resource) {
        return create(resource, null, null);
    }

    /**
     * Create a new API resource.
     *
     * If the provided resource contains a self link, it will be removed.
     *
     * @param resource is the new resource
     * @param headers is the set of additional HTTP headers that should be used in the request.
     * @param queryParameters is the set of query parameters that should be used in the request
     * @return the URI identifier for the created resource.
     */
    public URI create(T resource, MultivaluedMap<String, String> headers, MultivaluedMap<String, String> queryParameters) {
        discoverResourceUri(headers);
        if (resource.getSelf() != null) {
            resource.setSelf(null);
        }
        WebTarget target = client.target(resourceUriBuilder.build(""));
        configureAdditionalQueryParameters(target, queryParameters);
        Builder request = target.request();
        configureHttpHeaders(request, headers);
        Entity<T> jsonEntity = Entity.entity(resource, resourceMediaType);
        try (Response response = request.post(jsonEntity)) {
            if (!Objects.equals(201, response.getStatus())) {
                throw new WebApplicationException(response);
            }
            return URI.create(response.getHeaderString(HttpHeaders.LOCATION));
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
     * @param resource is the updated resource
     * @param headers is the set of additional HTTP headers that should be used in the request.
     * @param queryParameters is the set of query parameters that should be used in the request
     */
    public void update(T resource, MultivaluedMap<String, String> headers, MultivaluedMap<String, String> queryParameters) {
        discoverResourceUri(headers);
        URI resourceInstanceURI = resource.getSelf().getHref();
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
     * Remove an API resource referenced with a Link.
     *
     * @param resourceLink is the id of the resource
     */
    public void delete(Link resourceLink) {
        delete(resourceLink, null, null);
    }

    /**
     * Remove an API resource referenced with a Link.
     *
     * @param resourceLink is the id of the resource
     */
    public void delete(Link resourceLink, MultivaluedMap<String, String> headers, MultivaluedMap<String, String> queryParameters) {
        delete(resourceLink.getHref(), headers, queryParameters);
    }


    /**
     * Remove an API resource.
     *
     * @param resourceUri is the id of the resource
     */
    public void delete(URI resourceUri) {
        delete(resourceUri, null, null);
    }

    /**
     * Remove an API resource.
     *
     * @param resourceUri is the URI identifier of the resource
     * @param headers is the set of additional HTTP headers that should be used in the request.
     * @param queryParameters is the set of query parameters that should be used in the request
     */
    public void delete(URI resourceUri, MultivaluedMap<String, String> headers, MultivaluedMap<String, String> queryParameters) {
        discoverResourceUri(headers);
        validateResourceUri(resourceUri);
        WebTarget target = client.target(resourceUri);
        configureAdditionalQueryParameters(target, queryParameters);
        Builder request = target.request();
        configureHttpHeaders(request, headers);
        Response response = request.delete();
        if (response.getStatus() != Status.NO_CONTENT.getStatusCode()) {
            if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
                throw new NotFoundException(response);
            }
            else {
                throw new WebApplicationException(response);
            }
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
    public WebTarget hypermediaControl(Link action) {
        return client.target(action.getHref());
    }

    /**
     * Check whether a resource with the given URI, referenced with a Link, exists on the server.
     *
     * @param resourceLink is the URI of the resource that should be checked.
     * @return true iff the resource exists on the server, false if it does not exist.
     * @throws WebApplicationException if the client cannot confirm that the resource either
     * exists or does not exist. Is likely caused by an error returned by the server.
     */
    public boolean exists(Link resourceLink) {
        return exists(resourceLink, null, null);
    }

    /**
     * Check whether a resource with the given URI, referenced with a Link, exists on the server.
     *
     * @param resourceLink is the URI of the resource that should be checked.
     * @return true iff the resource exists on the server, false if it does not exist.
     * @throws WebApplicationException if the client cannot confirm that the resource either
     * exists or does not exist. Is likely caused by an error returned by the server.
     */
    public boolean exists(Link resourceLink, MultivaluedMap<String, String> headers, MultivaluedMap<String, String> queryParameters) {
        return exists(resourceLink.getHref(), headers, queryParameters);
    }

    /**
     * Check whether a resource with the given URI exists on the server.
     *
     * @param resourceUri is the URI of the resource that should be checked.
     * @return true iff the resource exists on the server, false if it does not exist.
     * @throws WebApplicationException if the client cannot confirm that the resource either
     * exists or does not exist. Is likely caused by an error returned by the server.
     */
    public boolean exists(URI resourceUri) {
        return exists(resourceUri, null, null);
    }

    /**
     * Check whether a resource with the given URI exists on the server.
     *
     * @param resourceUri is the URI of the resource that should be checked.
     * @param headers is the set of additional HTTP headers that should be used in the request.
     * @param queryParameters is the set of query parameters that should be used in the request.
     * @return true iff the resource exists on the server, false if it does not exist.
     * @throws WebApplicationException if the client cannot confirm that the resource either
     * exists or does not exist. Is likely caused by an error returned by the server.
     */
    public boolean exists(URI resourceUri, MultivaluedMap<String, String> headers, MultivaluedMap<String, String> queryParameters) {
        discoverResourceUri(headers);
        validateResourceUri(resourceUri);
        WebTarget target = client.target(resourceUri);
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
        if (Objects.isNull(resourceInstanceURI)) {
            throw new NullPointerException();
        }
        if (!resourceUriBuilder.build("").getHost().equals(resourceInstanceURI.getHost()) ||
                resourceUriBuilder.build("").relativize(resourceInstanceURI).equals(resourceInstanceURI)) {
            throw new IllegalArgumentException(ERROR_INVALID_RESOURCE_URI);
        }
    }

    /**
     * Create the resource URI from the UUID part of the URI identifier.
     *
     * @param resourceId is the UUID part of the URI identifier.
     * @return the full URI identifier for the resource, based on the discovered resource URI.
     */
    public URI createResourceUriFromUuid(UUID resourceId) {
        checkResourceUriDiscovered();
        return resourceUriBuilder.build(resourceId);
    }

    /**
     * Parse the resource UUID from the URI identifier.
     *
     * @param resourceUri is the URI identifier for the resource.
     * @return the UUID part of the URI identifier.
     */
    public UUID createResourceUuidFromUri(URI resourceUri) {
        checkResourceUriDiscovered();
        URI relativizedURI = resourceUriBuilder.build("").relativize(resourceUri);
        return UUID.fromString(relativizedURI.getPath());
    }

    private void checkResourceUriDiscovered() {
        if(resourceUriBuilder == null) {
            throw new IllegalStateException(ERROR_DISCOVER_RESOURCE_URI_REQUIRED);
        }
    }
}
