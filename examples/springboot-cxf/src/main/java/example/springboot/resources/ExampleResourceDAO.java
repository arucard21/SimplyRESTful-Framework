package example.springboot.resources;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.UriBuilder;

import org.apache.cxf.jaxrs.ext.search.SearchCondition;
import org.apache.cxf.jaxrs.ext.search.SearchContext;

import example.datastore.DataStore;
import example.datastore.StoredEmbeddedObject;
import example.datastore.StoredObject;
import io.openapitools.jackson.dataformat.hal.HALLink;
import simplyrestful.api.framework.core.DefaultResourceDAO;
import simplyrestful.api.framework.core.MediaType;

@Named
public class ExampleResourceDAO extends DefaultResourceDAO<ExampleResource, StoredObject> {
	public static final ThreadLocal<SearchContext> REQUEST_SEARCHCONTEXT = new ThreadLocal<>();

	/**
	 * Contains the mapping between the API's HAL resources, identified by
	 * the resource's URI, and the data store's resources, identified by a
	 * UUID. In an actual implementation, this mapping should probably be
	 * persistently stored, not kept in-memory.
	 */
	private Map<String, UUID> resourceMapping;
	private DataStore dataStore;

	public ExampleResourceDAO() {
		// FIXME: example currently does not use the mapper and entityDAO yet
		super(new ExampleEntityDAO());
		dataStore = new DataStore();
		resourceMapping = new HashMap<>();
		for(StoredObject entity : dataStore.getData()) {
			UUID entityID = entity.getId();
			URI relativeResourceURI = UriBuilder.fromResource(ExampleWebResource.class).path(entityID.toString()).build();
			resourceMapping.put(relativeResourceURI.getPath(), entityID);
		}
	}

	@Override
	public long count() {
		return dataStore.getData().size();
	}

	@Override
	public List<ExampleResource> findAllForPage(int pageNumber, int pageSize, URI absoluteWebResourceURI) {
		int startElement = ((pageNumber-1)*pageSize);
		int endElement = ((pageNumber)*pageSize);
		List<StoredObject> data = dataStore.getData();
		int dataSize = data.size();
		if (startElement > dataSize) {
			startElement = dataSize;
			endElement = dataSize;
		}
		if (endElement > dataSize) {
			endElement = dataSize;
		}
		List<ExampleResource> resources = data.subList(startElement, endElement)
				.stream()
				.map((entity) -> convertToResource(entity, absoluteWebResourceURI))
				.collect(Collectors.toList());
		SearchContext searchContext = REQUEST_SEARCHCONTEXT.get();
		if (searchContext == null) {
			return resources;
		}
		SearchCondition<ExampleResource> searchCondition = searchContext.getCondition(ExampleResource.class);
    	if (searchCondition == null){
    		return resources;
    	}
    	return searchCondition.findAll(resources);
	}

	@Override
	public ExampleResource findByURI(URI resourceURI, URI absoluteWebResourceURI) {
		UUID dataID = resourceMapping.get(resourceURI.getPath());
		if (dataID == null){
			return null;
		}
		StoredObject resourceFromDataStore = dataStore.getObject(dataID);
		return resourceFromDataStore == null ? null : convertToResource(resourceFromDataStore, absoluteWebResourceURI);
	}

	@Override
	public ExampleResource persist(ExampleResource resource, URI absoluteWebResourceURI) {
		HALLink selfLink = resource.getSelf();
		if (selfLink == null){
			throw new BadRequestException("The resource does not contain a self-link");
		}
		String resourceURI = selfLink.getHref();
		if (resourceURI == null || resourceURI.isEmpty()){
			throw new BadRequestException("The resource contains an empty self-link");
		}
		if (resourceMapping.get(resourceURI) == null){
			// add this resource to the resource mapping
			resourceMapping.put(resourceURI, UUID.randomUUID());
		}
		StoredObject previousData = dataStore.getObject(resourceMapping.get(resourceURI));
		if (previousData == null){
			throw new IllegalStateException("The to-be-updated resource does not exist yet.");
		}
		dataStore.getData().remove(dataStore.getObject(resourceMapping.get(resourceURI)));
		dataStore.getData().add(convertToEntity(resource));
		return convertToResource(previousData, absoluteWebResourceURI);
	}

	@Override
	public ExampleResource remove(URI resourceURI, URI absoluteWebResourceURI) {
		UUID dataID = resourceMapping.get(resourceURI.getPath());
		if (dataID == null){
			return null;
		}
		StoredObject previousData = dataStore.getObject(dataID);
		dataStore.getData().remove(previousData);
		return previousData == null ? null : convertToResource(previousData, absoluteWebResourceURI);
	}

	private StoredObject convertToEntity(ExampleResource exampleResource) {
		StoredObject dataResource = new StoredObject();
		dataResource.setDescription(exampleResource.getDescription());
		dataResource.setId(resourceMapping.get(exampleResource.getSelf().getHref()));
		dataResource.setEmbedded(convertEmbeddedToEntity(exampleResource.getEmbeddedResource()));
		return dataResource;
	}

	private StoredEmbeddedObject convertEmbeddedToEntity(ExampleEmbeddedResource embedded) {
		StoredEmbeddedObject embObj = new StoredEmbeddedObject();
		embObj.setName(embedded.getName());
		return embObj;
	}

	private ExampleResource convertToResource(StoredObject storedResource, URI absoluteWebResourceURI) {
		ExampleResource exampleResource = new ExampleResource();
		exampleResource.setDescription(storedResource.getDescription());
		// create resource URI with new UUID and add it to the mapping
		exampleResource.setSelf(createSelfLinkWithUUID(storedResource.getId(), exampleResource.getProfile(), absoluteWebResourceURI));
		exampleResource.setEmbeddedResource(convertEmbeddedToResource(storedResource.getEmbedded(), absoluteWebResourceURI));
		resourceMapping.put(absoluteWebResourceURI.getPath(), storedResource.getId());
		return exampleResource;
	}

	private ExampleEmbeddedResource convertEmbeddedToResource(StoredEmbeddedObject embedded, URI absoluteWebResourceURI) {
		ExampleEmbeddedResource embRes = new ExampleEmbeddedResource();
		embRes.setName(embedded.getName());
		resourceMapping.put(absoluteWebResourceURI.getPath(), embedded.getId());
		return embRes;
	}

	private HALLink createSelfLinkWithUUID(UUID id, URI resourceProfile, URI absoluteWebResourceURI) {
		return new HALLink.Builder(absoluteWebResourceURI)
			.type(MediaType.APPLICATION_HAL_JSON)
			.profile(resourceProfile)
			.build();
	}

	@Override
	public StoredObject map(ExampleResource resource) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExampleResource map(StoredObject entity, URI resourceURI) {
		// TODO Auto-generated method stub
		return null;
	}
}
