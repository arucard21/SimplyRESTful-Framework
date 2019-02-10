package simplyrestful.api.framework.client;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriBuilder;

import io.openapitools.jackson.dataformat.hal.HALLink;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import simplyrestful.api.framework.resources.HALCollection;
import simplyrestful.api.framework.resources.HALResource;
import simplyrestful.api.framework.resources.HALServiceDocument;

public class APIClient<T extends HALResource> {
	private static final String MEDIATYPE_HAL_JSON = "application/hal+json";
	private static final String PATH_PARAMETER_ID = "{id}";
	private Class<T> resourceClass;
	private URI baseApiUri;
	private URI resourceUri;
	private Client client;
	

	public APIClient(URI baseApiUri, Class<T> resourceClass) {
		this.baseApiUri = baseApiUri;
		this.client = ClientBuilder.newClient();
		this.resourceClass = resourceClass;
		this.resourceUri = discoverResourceURI();
	}

	private URI discoverResourceURI() {
		HALServiceDocument serviceDocument = client.target(baseApiUri).request().get(HALServiceDocument.class);
		URI openApiJson = URI.create(serviceDocument.getDescribedby().getHref());
		Swagger openApiSpecification = client.target(openApiJson).request().get(Swagger.class);
		HashMap<String, String> parameters = new HashMap<>();
		try {
			String resourceProfile = resourceClass.newInstance().getProfile().toString();
			parameters.put("profile", resourceProfile);
			MediaType resourceMediaType = new MediaType("application", "hal+json", parameters);
			for(Entry<String, Path> pathEntry : openApiSpecification.getPaths().entrySet()) {
				if(pathEntry.getKey().contains(PATH_PARAMETER_ID)) {
					continue;
				}
				boolean matchingMediaType = pathEntry.getValue().getGet().getProduces()
						.stream()
						.map(produces -> MediaType.valueOf(produces))
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
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalArgumentException("Resource class could not be instantiated", e);
		}
	}

	/**
	 * Retrieve a page of API resources.
	 * 
	 * The parameters may be null, in which case they will not be included in the request. This will
	 * cause the API to use its default value for these parameters. 
	 * 
	 * @param page is the number of the page.
	 * @param pageSize is the size of each page.
	 * @return the page of resources corresponding to the provided parameters.
	 */
	public List<T> retrievePageOfResources(int page, int pageSize){
		return client.target(resourceUri).request().get(new GenericType<HALCollection<T>>() {}).getItemEmbedded();
	}
	
	/**
	 * Retrieve a page of API resource identifiers.
	 * 
	 * The parameters may be null, in which case they will not be included in the request. This will
	 * cause the API to use its default value for these parameters. 
	 * 
	 * @param page is the number of the page.
	 * @param pageSize is the size of each page.
	 * @return the page of resource identifiers corresponding to the provided parameters.
	 */
	public List<UUID> retrievePageOfResourceIdentifiers(int page, int pageSize){
		List<HALLink> selfLinks = client.target(resourceUri).request().get(new GenericType<HALCollection<T>>() {}).getItem();
		return selfLinks.stream()
				.filter(selfLink -> {
					if (selfLink.getTemplated() != null && selfLink.getTemplated() == true) {
						System.err.printf(
								"%s has a templated self link which is invalid for a self link. "
								+ "It was removed from the results as it indicates that something is wrong with this resource.", 
								selfLink.getHref());
						return false;
					}
					else {
						return true;
					}
				})
				.map(selfLink -> {
					URI resourceInstanceUri = URI.create(selfLink.getHref());
					URI relativizedURI = resourceUri.relativize(resourceInstanceUri);
					return UUID.fromString(relativizedURI.getPath());
				})
				.collect(Collectors.toList());
	}
	
	/**
	 * Retrieve a single API resource.
	 * 
	 * @param resourceId is the id of the resource
	 * @return the API resource at the given URI
	 */
	public T retrieve(UUID resourceId){
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
				.post(Entity.entity(resource, MEDIATYPE_HAL_JSON))
				.getHeaderString(HttpHeaders.LOCATION);
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
				.put(Entity.entity(resource, MEDIATYPE_HAL_JSON))
				.getStatusInfo();
		return statusCode == Status.CREATED ? true : false;
	}
	
	/**
	 * Remove an API resource.
	 * 
	 * @param resourceId is the id of the resource
	 * @return true if the resource was deleted, false otherwise
	 */
	public boolean remove(UUID resourceId) {
		URI resourceInstanceURI = UriBuilder.fromUri(resourceUri).path(resourceId.toString()).build();
		StatusType statusCode = client.target(resourceInstanceURI).request().delete().getStatusInfo();
		return statusCode == Status.NO_CONTENT ? true : false;
	}
}
