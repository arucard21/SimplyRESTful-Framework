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
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.HALMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.parser.OpenAPIV3Parser;
import simplyrestful.api.framework.resources.HALCollection;
import simplyrestful.api.framework.resources.HALResource;
import simplyrestful.api.framework.resources.HALServiceDocument;

public class SimplyRESTfulClient<T extends HALResource> {
    private static final String ERROR_CREATE_RESOURCE_EXISTS = "The resource already exists on the server. Use update() if you wish to modify the existing resource.";
    private static final String ERROR_INVALID_RESOURCE_URI = "The identifier of the resource does not correspond to the API in this client";
    private static final String HAL_ITEM_KEY = "item";
    private static final String HAL_EMBEDDED_KEY = "_embedded";
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

    public SimplyRESTfulClient(URI baseApiUri, Class<T> resourceClass) {
	this.baseApiUri = baseApiUri;
	this.client = ClientBuilder
		.newBuilder()
		.register(new JacksonJsonProvider(new HALMapper()))
		.build();
	this.resourceClass = resourceClass;
	detectResourceMediaType();
	this.resourceUri = discoverResourceURI();
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
     * This discovery is done by accessing the OpenAPI Specification document
     * which is linked in the HAL Service Document located at the root of the API
     * (baseApiUri). Here, we find the GET path for the media type matching that of
     * the HAL resource T, which is the resource URI.
     *
     * The limitation is that the GET operation on the resource must be available in
     * order for that resource's URI to be discoverable.
     *
     * @return the discovered resource URI.
     */
    private URI discoverResourceURI() {
	HALServiceDocument serviceDocument = client.target(baseApiUri).request().get(HALServiceDocument.class);
	URI openApiDocumentUri = URI.create(serviceDocument.getDescribedby().getHref());
	OpenAPI openApiSpecification = new OpenAPIV3Parser().read(openApiDocumentUri.toString());
	for (Entry<String, PathItem> pathEntry : openApiSpecification.getPaths().entrySet()) {
	    Operation getHttpMethod = pathEntry.getValue().getGet();
	    boolean matchingMediaType = getHttpMethod.getResponses().get("200").getContent().keySet().stream()
		    .map(MediaType::valueOf) 
		    .anyMatch(mediaType -> mediaType.equals(resourceMediaType));
	    if (matchingMediaType) {
		String resourcePath = pathEntry.getKey();
		for (Parameter parameter : getHttpMethod.getParameters()) {
		    if ("path".equals(parameter.getIn())) {
			resourcePath = resourcePath.replaceAll(String.format("\\{%s\\}", parameter.getName()), "");
			resourcePath = resourcePath.replaceAll("//", "/");
		    }
		}
		return UriBuilder.fromUri(openApiSpecification.getServers().get(0).getUrl())
			.scheme(baseApiUri.getScheme())
			.path(resourcePath)
			.build();
	    }
	}
	throw new IllegalArgumentException(
		String.format("The API at %s does not provide resources formatted as HAL+JSON with profile %s",
			baseApiUri.toString(), resourceProfile));
    }

    /**
     * List the full API resource using the API's default paging configuration.
     *
     * @param page     is the number of the page. If zero or negative, will not be included
     *                 in the HTTP request.
     * @param pageSize is the size of each page. If zero or negative, will not be included
     *                 in the HTTP request.
     * @return a list of API resources from the page corresponding to the provided
     *         parameters.
     */
    public List<T> listResources(int page, int pageSize) {
	return retrievePagedCollection(page, pageSize, false).getItemEmbedded();
    }

    /**
     * Retrieve a list of API resource identifiers for a page of API resources.
     *
     * @param page     is the number of the page. If negative, will not be included
     *                 in the HTTP request.
     * @param pageSize is the size of each page. If negative, will not be included
     *                 in the HTTP request.
     * @return a list of resource identifiers from the page corresponding to the
     *         provided parameters.
     */
    public List<UUID> listResourceIdentifiers(int page, int pageSize) {
	return retrievePagedCollection(page, pageSize, true).getItem().stream()
		.filter(selfLink -> Objects.isNull(selfLink.getTemplated()) || !selfLink.getTemplated())
		.map(selfLink -> {
		    URI resourceInstanceUri = URI.create(selfLink.getHref());
		    URI relativizedURI = resourceUri.relativize(resourceInstanceUri);
		    return UUID.fromString(relativizedURI.getPath());
		}).collect(Collectors.toList());
    }

    /**
     * Retrieve HAL Collection resource containing a page of API resources.
     *
     * @param page     is the number of the page. If negative, will not be included
     *                 in the HTTP request.
     * @param pageSize is the size of each page. If negative, will not be included
     *                 in the HTTP request.
     * @param compact  returns only a resource identifier, if true. If false, each
     *                 resource will be entirely embedded in the list.
     * @return the entire collection resource that was retrieved, containing either
     *         resource identifiers or embedded resources.
     */
    @SuppressWarnings("unchecked")
    private HALCollection<T> retrievePagedCollection(int page, int pageSize, boolean compact){
	WebTarget target = client.target(resourceUri);
	if (page >= 0) {
	    target = target.queryParam(QUERY_PARAM_PAGE, page);
	}
	if (pageSize >= 0) {
	    target = target.queryParam(QUERY_PARAM_PAGESIZE, pageSize);
	}
	String nonDeserialized =  target.queryParam(QUERY_PARAM_COMPACT, compact).request().get(String.class);
	HALCollection<T> collection;
	
	collection = (HALCollection<T>) deserializeJsonWithGenerics(nonDeserialized, new TypeReference<HALCollection<BasicHALResource>>() {});
	JsonObject embedded = Json
		.createReader(new StringReader(nonDeserialized))
		.readObject()
		.getJsonObject(HAL_EMBEDDED_KEY);
	if(Objects.nonNull(embedded)) {	    
	    collection.setItemEmbedded(
		    embedded.getJsonArray(HAL_ITEM_KEY).stream()
		    .filter(Objects::nonNull)
		    .map(jsonValue -> deserializeJson(jsonValue.toString(), resourceClass))
		    .filter(Objects::nonNull)
		    .collect(Collectors.toList()));
	}
	return collection;
    }

    private <S> S deserializeJson(String jsonString, Class<S> deserializationClass) {
	try {
	    return new HALMapper().readValue(jsonString, deserializationClass);
	} catch (JsonProcessingException e) {
	    throw new RuntimeException(e);
	}
    }
    
    private <S> S deserializeJsonWithGenerics(String jsonString, TypeReference<S> typeRef) {
	try {
	    return new HALMapper().readValue(jsonString, typeRef);
	} catch (JsonProcessingException e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * Retrieve a single API resource.
     *
     * @param resourceId is the id of the resource.
     * @return the API resource at the given URI.
     */
    public T read(UUID resourceId) {
	URI resourceInstanceURI = UriBuilder.fromUri(resourceUri).path(resourceId.toString()).build();
	return client.target(resourceInstanceURI).request().get(resourceClass);
    }

    /**
     * Create a new API resource.
     * 
     * If the provided resource contains a valid id, the resource will be created on the server
     * with that same id, using HTTP PUT. Otherwise, the resource will be created using HTTP POST
     * which will generate an id for that resource.
     *
     * @param resource is the new resource
     * @return the id for the created resource, applicable to the provided base URI
     */
    public UUID create(T resource) {
	HALLink resourceSelf = resource.getSelf();
	Response response;
	if(Objects.nonNull(resourceSelf) && isResourceUriValid(URI.create(resourceSelf.getHref()))) {
	    URI resourceInstanceUri = URI.create(resourceSelf.getHref());
	    if(exists(resourceInstanceUri)) {
		throw new IllegalArgumentException(ERROR_CREATE_RESOURCE_EXISTS);
	    }
	    response = client
		    .target(resourceInstanceUri)
		    .request()
		    .put(Entity.entity(resource, MEDIA_TYPE_HAL_JSON));
	}
	else {
	    response = client
		    .target(resourceUri)
		    .request()
		    .post(Entity.entity(resource, MEDIA_TYPE_HAL_JSON));
	}
	if(!Objects.equals(201, response.getStatus())) {
	    throw new WebApplicationException(response);
	}
	String location = response.getHeaderString(HttpHeaders.LOCATION);
	URI relativizedURI = resourceUri.relativize(URI.create(location));
	return UUID.fromString(relativizedURI.getPath());
    }

    /**
     * Update an existing API resource.
     *
     * This may also create a new resource at the absolute URI provided in the
     * resource. The return value indicates whether a new resource was created or
     * not.
     *
     * @param resource is the updated resource
     * @return true if the resource was newly created, false if an existing resource
     *         was updated
     */
    public boolean update(T resource) {
	URI resourceInstanceURI = URI.create(resource.getSelf().getHref());
	validateResourceUri(resourceInstanceURI);
	StatusType statusCode = client.target(resourceInstanceURI).request()
		.put(Entity.entity(resource, MEDIA_TYPE_HAL_JSON)).getStatusInfo();
	return statusCode == Status.CREATED;
    }

    /**
     * Verify that the provided URI is relative to the web resource that's being served. 
     * 
     * @param resourceInstanceURI is the URI to be verified
     * @return true iff the provided URI is not null and relative to the URI of the web resource, false otherwise.
     */
    private boolean isResourceUriValid(URI resourceInstanceURI) {
	if(Objects.isNull(resourceInstanceURI)) {
	    return false;
	}
	if (!resourceUri.relativize(resourceInstanceURI).equals(resourceInstanceURI)) {
	    return true;
	}
	return false;
    }
    
    /**
     * Validates the give URI according to isResourceUriValid() and throws an exception if it is not valid. 
     * 
     * @param resourceInstanceURI is the URI that is required to be valid.
     */
    private void validateResourceUri(URI resourceInstanceURI) {
	if(!isResourceUriValid(resourceInstanceURI)) {
	    throw new IllegalArgumentException(ERROR_INVALID_RESOURCE_URI);
	}
    }

    /**
     * Remove an API resource.
     *
     * @param resourceId is the id of the resource
     * @return true if the resource was deleted, false otherwise
     */
    public boolean delete(UUID resourceId) {
	URI resourceInstanceURI = UriBuilder.fromUri(resourceUri).path(resourceId.toString()).build();
	StatusType statusCode = client.target(resourceInstanceURI).request().delete().getStatusInfo();
	return statusCode == Status.NO_CONTENT;
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
     * @return true iff the resource exists on the server, false if it does not exist.
     * @throws WebApplicationException if the client cannot confirm that the resource either exists or does not exist. 
     * Is likely caused by an error returned by the server.
     */
    public boolean exists(UUID resourceId) {
	URI resourceInstanceURI = UriBuilder.fromUri(resourceUri).path(resourceId.toString()).build();
	return exists(resourceInstanceURI);
    }
    
    /**
     * Check whether a resource with the given URI exists on the server.
     * 
     * @param resourceInstanceURI is the URI of the resource that should be checked.
     * @return true iff the resource exists on the server, false if it does not exist.
     * @throws WebApplicationException if the client cannot confirm that the resource either exists or does not exist. 
     * Is likely caused by an error returned by the server. 
     */
    public boolean exists(URI resourceInstanceURI) {
	validateResourceUri(resourceInstanceURI);
	Response response = client.target(resourceInstanceURI).request().get();
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
