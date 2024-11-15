package simplyrestful.api.framework.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.OpenAPIV3Parser;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.sse.SseEventSource;
import simplyrestful.api.framework.queryparams.SortOrder;
import simplyrestful.api.framework.resources.APICollection;
import simplyrestful.api.framework.resources.APIResource;
import simplyrestful.api.framework.resources.APIServiceDocument;
import simplyrestful.api.framework.resources.Link;

/**
 * A client for any SimplyRESTful-based API.
 *
 * @param <T> is the class of the resource used in the SimplyRESTful API that you wish to access.
 */
public class SimplyRESTfulClient<T extends APIResource> {
	/**
	 * Error message when an incorrect GenericType is provided to the client
	 */
	public static final String ERROR_TYPE_FOR_API_COLLECTION_INVALID = "The GenericType argument must be created for a parameterized type with APICollection as the base class and the APIResource child class as parameter type, i.e. new GenericType<APICollection<YourApiResource>>() {}";
	/**
	 * Error message when media type of the resource cannot be discovered due to a problem with creating an instance of the resource class
	 */
	public static final String ERROR_DISCOVER_RESOURCE_MEDIA_TYPE_FAILED_TEMPLATE = "Could not construct an instance of the resource class %s";
	/**
	 * Error message when the discovery process was not initiated before attempting to access the API.
	 */
	public static final String ERROR_DISCOVER_RESOURCE_URI_REQUIRED = "This method can only be used after the resource URI has been discovered. This is done at every API request but you can trigger it manually by calling discoverResourceUri() directly";
	/**
	 * Error message when trying to create a resource with the update() method.
	 */
	public static final String ERROR_UPDATE_RESOURCE_DOES_NOT_EXIST = "The resource does not exist yet. Use create() if you wish to create a new resource.";
	/**
	 * Error message when the self-link does not match the URI where the resource is available.
	 */
	public static final String ERROR_INVALID_RESOURCE_URI = "The identifier of the resource does not correspond to the API in this client";
	/**
	 * Delimiter used to separate multiple values in a single query parameter.
	 */
	public static final String QUERY_PARAM_VALUE_DELIMITER = ",";
	/**
	 * Field name for the list of items contained in the page of the collection.
	 */
	public static final String COLLECTION_ITEM_KEY = "item";
	/**
	 * Field name for the total amount of items contain in the collection.
	 */
	public static final String COLLECTION_TOTAL_KEY = "total";
	/**
	 * Query parameter name for the start index of the page of the collection that you are retrieving.
	 */
	public static final String QUERY_PARAM_PAGE_START = "pageStart";
	/**
	 * Query parameter name for the size of the page of the collection that you are retrieving.
	 */
	public static final String QUERY_PARAM_PAGESIZE = "pageSize";
	/**
	 * Query parameter name for indicating which fields should be included in the response.
	 */
	public static final String QUERY_PARAM_FIELDS = "fields";
	/**
	 * Query parameter name for the FIQL query to filter the list of resources contained in the collection.
	 */
	public static final String QUERY_PARAM_QUERY = "query";
	/**
	 * Query parameter name for specifying on which fields to sort the resources contained in the collection.
	 */
	public static final String QUERY_PARAM_SORT = "sort";

    private final GenericType<APICollection<T>> typeForAPICollection;
    private final URI baseApiUri;
    private final MediaType resourceMediaType;
    private final Client client;
    private UriBuilder resourceUriBuilder;
    private int totalAmountOfLastRetrievedCollection;

    /**
     * Create a new SimplyRESTful client.
	 *
	 * @param client is the JAX-RS client that should be used when the SimplyRESTful client executes HTTP requests.
	 * @param baseApiUri is the base URI of the SimplyRESTful-based API that the client should access.
	 * @param typeForAPICollection is a GenericType object that indicates the typing for the collection
	 * of resources, e.g.  {@code new GenericType<APICollection<YourApiResource>>() {}}. is required for the
	 * client to properly handle deserialization because of type erasure.
     */
	public SimplyRESTfulClient(Client client, URI baseApiUri, GenericType<APICollection<T>> typeForAPICollection) {
        this.baseApiUri = baseApiUri;
        this.client = client;
        if (! (typeForAPICollection.getType() instanceof ParameterizedType)) {
        	throw new IllegalArgumentException(ERROR_TYPE_FOR_API_COLLECTION_INVALID);
        }
        this.typeForAPICollection = typeForAPICollection;
        this.resourceMediaType = detectResourceMediaType();
    }

	/**
	 * Get the resource class based on the provided type for the API collection.
	 *
	 * This resource class should be a subclass of {@link APIResource}.
	 *
	 * This requires an unchecked cast because the resource class for T is unavailable at runtime so it cannot be checked.
	 * But both the collection type and the resource class type use the same generic variable T so the cast should be safe enough.
	 *
	 * @return the actual class of the API resource
	 */
	@SuppressWarnings("unchecked")
	private Class<T> getResourceClass() {
		return (Class<T>) ((ParameterizedType) typeForAPICollection.getType()).getActualTypeArguments()[0];
	}

	private MediaType detectResourceMediaType() {
    	try {
			return getResourceClass().getDeclaredConstructor().newInstance().customJsonMediaType();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException(String.format(ERROR_DISCOVER_RESOURCE_MEDIA_TYPE_FAILED_TEMPLATE, getResourceClass().getName()), e);
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
        Builder serviceDocumentRequest = client.target(baseApiUri).request();
        configureHttpHeaders(serviceDocumentRequest, headers);
        serviceDocumentRequest.accept(APIServiceDocument.MEDIA_TYPE_JSON);
        APIServiceDocument serviceDocument = serviceDocumentRequest.get(APIServiceDocument.class);
        URI openApiDocumentUri = serviceDocument.getDescribedBy().getHref();

        Builder openApiDocumentRequest = client.target(openApiDocumentUri).request();
        configureHttpHeaders(openApiDocumentRequest, headers);
        openApiDocumentRequest.accept(MediaType.APPLICATION_JSON_TYPE);
        String openApiDocumentContents = openApiDocumentRequest.get(String.class);
        OpenAPI openApiSpecification = new OpenAPIV3Parser().readContents(openApiDocumentContents).getOpenAPI();

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
	 * Retrieve the total amount of resources that were contained in the (filtered) collection that was last retrieved.
	 *
	 * Note that this is the total amount in the collection, not the total amount in the page that was returned.
	 *
	 * @return the total amount of resources in the collection that was last retrieved.
	 */
	public int getTotalAmountOfLastRetrievedCollection() {
	    return this.totalAmountOfLastRetrievedCollection;
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
        APICollection<T> resourceCollection = request.get(typeForAPICollection);
        this.totalAmountOfLastRetrievedCollection = resourceCollection.getTotal();
        return resourceCollection.getItem();
    }

	/**
	 * Stream the API resources from the API using server-sent events.
	 *
	 * @param fields is a list that defines which fields should be retrieved.
	 * @param query is a FIQL query that defines how the resources should be filtered.
	 * @param sort is a list of field names on which the resources should be sorted.
	 * @param additionalHeaders is the set of HTTP headers that should be added to the request.
	 * @param additionalQueryParameters is the set of query parameters that should be added to the request.
	 * @param timeoutInMs is the max amount of time (in milliseconds) to wait for all resources to have been sent.
	 * @return the entire collection resource that was retrieved, containing either
	 *         resource identifiers or embedded resources.
	 */
	public List<T> streamResourcesFromCollection(
	        List<String> fields,
	        String query,
	        List<SortOrder> sort,
	        MultivaluedMap<String, String> additionalHeaders,
	        MultivaluedMap<String, String> additionalQueryParameters,
	        int timeoutInMs) {
	    WebTarget target = client.target(resourceUriBuilder.build(""));
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
	    target.register((ClientRequestFilter) requestContext -> {
	    	if(additionalHeaders != null) {
	    		additionalHeaders.forEach((headerName, headerValue) -> requestContext.getHeaders().add(headerName, headerValue));
	    	}
	    });
	    List<T> resources = new ArrayList<>();
	    try (SseEventSource source = SseEventSource.target(target).build()) {
	    	ExecutorService resourceStreamingService = Executors.newSingleThreadExecutor();
	    	source.register(event -> {
	    		if(event.getComment() != null && event.getComment().equals("end-of-collection")) {
	    			source.close();
	    			resourceStreamingService.shutdown();
	    			return;
	    		}
	    		resources.add(event.readData(getResourceClass()));
	    	});
	    	resourceStreamingService.submit(() -> {
	    		source.open();
	    	});
	    	try {
	    		resourceStreamingService.awaitTermination(timeoutInMs, TimeUnit.MILLISECONDS);
	    	} catch (InterruptedException e) {
	    		throw new IllegalStateException("The streaming of API resources was interrupted", e);
	    	}
	    }
	    return resources;
	}

	private void configureAdditionalQueryParameters(WebTarget target, MultivaluedMap<String, String> queryParameters) {
        if (queryParameters == null) {
            return;
        }
        for (Entry<String, List<String>> queryParameter : queryParameters.entrySet()) {
            target.queryParam(queryParameter.getKey(), queryParameter.getValue().toArray());
        }
    }

    private void configureHttpHeaders(Builder request, MultivaluedMap<String, String> headers) {
        if (headers == null) {
            return;
        }
        headers.forEach((headerName, headerValue) -> request.header(headerName, headerValue));
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
     * @param headers contains any additional HTTP headers that should be sent.
     * @param queryParameters contains any additional query parameters that should be sent.
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
     * @param queryParameters is the set of query parameters that should be used in the request.
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
        return request.get(getResourceClass());
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
     * @param queryParameters is the set of query parameters that should be used in the request.
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
     * @param queryParameters is the set of query parameters that should be used in the request.
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
        if (!Objects.equals(response.getStatusInfo(), Status.NO_CONTENT)) {
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
     * @param headers is the set of additional HTTP headers that should be used in the request.
     * @param queryParameters is the set of query parameters that should be used in the request.
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
     * @param queryParameters is the set of query parameters that should be used in the request.
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
     * @param headers is the set of additional HTTP headers that should be used in the request.
     * @param queryParameters is the set of query parameters that should be used in the request.
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
