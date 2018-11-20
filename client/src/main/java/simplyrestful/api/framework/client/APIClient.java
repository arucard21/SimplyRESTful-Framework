package simplyrestful.api.framework.client;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import io.swagger.models.Path;
import io.swagger.models.Swagger;
import simplyrestful.api.framework.resources.HALResource;
import simplyrestful.api.framework.resources.HALServiceDocument;

public class APIClient<T extends HALResource> {
	private static final String PATH_GET = "get";
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
	 * @param page is the number of the page
	 * @param pageSize is the size of each page
	 * @return the page of resources corresponding to the provided parameters.
	 */
	public List<T> retrievePage(int page, int pageSize){
		return null;
	}
	
	/**
	 * Retrieve a single API resource.
	 * 
	 * @param resourceId is the id of the resource
	 * @return the API resource at the given URI
	 */
	public T retrieve(UUID resourceId){
		return null;
	}
	
	/**
	 * Create a new API resource.
	 * 
	 * @param resource is the new resource
	 * @return the id for the created resource, applicable to the provided base URI
	 */
	public UUID create(T resource) {
		return null;
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
		return false;
	}
	
	/**
	 * Remove an API resource.
	 * 
	 * @param absoluteResourceURI is the absolute URI of the resource
	 * @return true if the resource was deleted, false otherwise
	 */
	public boolean remove(URI absoluteResourceURI) {
		return false;
	}
}
