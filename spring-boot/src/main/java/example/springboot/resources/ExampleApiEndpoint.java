/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package example.springboot.resources;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Named;
import javax.ws.rs.Path;

import org.apache.cxf.jaxrs.ext.search.SearchCondition;

import dk.nykredit.jackson.dataformat.hal.HALLink;
import example.datastore.DataStore;
import example.datastore.StoredEmbeddedObject;
import example.datastore.StoredObject;
import example.jetty.resources.ExampleEmbeddedResource;
import example.jetty.resources.ExampleResource;
import io.swagger.annotations.Api;
import simplyrestful.api.framework.core.WebResourceBase;
import simplyrestful.api.framework.core.exceptions.InvalidResourceException;
import simplyrestful.api.framework.core.exceptions.InvalidSelfLinkException;
import simplyrestful.api.framework.core.hal.HALCollection;
import simplyrestful.api.framework.core.hal.HALCollectionFactory;

@Named
@Path("/resources")
@Api(value = "Example Resources")
public class ExampleApiEndpoint extends WebResourceBase<ExampleResource> {
	/**
	 * Contains the mapping between the API's HAL resources, identified by
	 * the resource's URI, and the data store's resources, identified by a
	 * UUID. In an actual implementation, this mapping should probably be
	 * persistently stored, not kept in-memory.
	 */
	private Map<String, UUID> resourceMapping;
	private DataStore dataStore;

    public ExampleApiEndpoint() {
    	resourceMapping = new HashMap<String, UUID>();
    	dataStore = new DataStore();
    }

    @Override
	protected HALCollection<ExampleResource> retrieveResourcesFromDataStore(int pageNumber, int pageSize, boolean compact) {
    	List<ExampleResource> exampleResources = new ArrayList<ExampleResource>();
    	for (StoredObject storedResource: dataStore.getData()){
    		exampleResources.add(convert(storedResource));
    	}
    	SearchCondition<ExampleResource> searchCondition = searchContext.getCondition(ExampleResource.class);
    	if (searchCondition != null){
    		exampleResources = searchCondition.findAll(exampleResources);
    	}
    	return new HALCollectionFactory<ExampleResource>().createPagedCollectionFromFullList(exampleResources, pageNumber, pageSize, uriInfo.getRequestUri(), compact);
	}

	@Override
	protected boolean addResourceToDataStore(ExampleResource resource) {
		// there's probably much smarter ways to handle conversion and mapping
		HALLink selfLink = resource.getSelf();
		if (selfLink == null){
			UUID resourceID = UUID.randomUUID();
			selfLink = createSelfLinkWithUUID(resourceID, resource.getProfile());
			resource.setSelf(selfLink);
			resourceMapping.put(selfLink.getHref(), resourceID);
		}
		String resourceURI = selfLink.getHref();
		UUID dataID = resourceMapping.get(resourceURI);
		if (dataID == null){
			dataID = UUID.randomUUID();
			resourceMapping.put(resourceURI, dataID);
		}
		return dataStore.getData().add(convert(resource));
	}

	@Override
	protected ExampleResource retrieveResourceFromDataStore(String resourceURI) {
		UUID dataID = resourceMapping.get(resourceURI);
		if (dataID == null){
			return null;
		}
		StoredObject resourceFromDataStore = dataStore.getObject(dataID);
		return resourceFromDataStore == null ? null : convert(resourceFromDataStore);
	}

	@Override
	protected ExampleResource updateResourceInDataStore(ExampleResource resource) throws InvalidResourceException {
		HALLink selfLink = resource.getSelf();
		if (selfLink == null){
			throw new InvalidSelfLinkException("The resource does not contain a self-link");
		}
		String resourceURI = selfLink.getHref();
		if (resourceURI == null || resourceURI.isEmpty()){
			throw new InvalidResourceException("The resource contains an empty self-link");
		}
		if (resourceMapping.get(resourceURI) == null){
			// add this resource to the resource mapping
			resourceMapping.put(resourceURI, UUID.randomUUID());
		}
		StoredObject previousData = dataStore.getObject(resourceMapping.get(resourceURI));
		if (previousData == null){
			throw new IllegalStateException("The to-be-updated resource does not exist yet.");
		}
		else{
			dataStore.getData().remove(resourceMapping.get(resourceURI));
			dataStore.getData().add(convert(resource));
			return convert(previousData);
		}
	}

	@Override
	protected ExampleResource removeResourceFromDataStore(String resourceURI) {
		UUID dataID = resourceMapping.get(resourceURI);
		if (dataID == null){
			return null;
		}
		StoredObject previousData = dataStore.getObject(dataID);
		dataStore.getData().remove(dataID);
		return previousData == null ? null : convert(previousData);
	}

	@Override
	protected ExampleResource handleHateoasAction(String resourceURI, String action) {
		switch(action){
			case "test": return retrieveResourceFromDataStore(resourceURI);
		}
		return null;
	}

	@Override
	protected boolean exists(String resourceURI) {
		UUID dataID = resourceMapping.get(resourceURI);
		return dataID == null ? false : true;
	}

	private StoredObject convert(ExampleResource exampleResource) {
		StoredObject dataResource = new StoredObject();
		dataResource.setDescription(exampleResource.getDescription());
		dataResource.setId(resourceMapping.get(exampleResource.getSelf().getHref()));
		dataResource.setEmbedded(convert(exampleResource.getEmbeddedResource()));
		return dataResource;
	}

	private StoredEmbeddedObject convert(ExampleEmbeddedResource embedded) {
		StoredEmbeddedObject embObj = new StoredEmbeddedObject();
		embObj.setName(embedded.getName());
		embObj.setId(resourceMapping.get(embedded.getSelf().getHref()));
		return embObj;
	}

	private ExampleResource convert(StoredObject storedResource) {
		ExampleResource exampleResource = new ExampleResource();
		exampleResource.setDescription(storedResource.getDescription());
		// create resource URI with new UUID and add it to the mapping
		exampleResource.setSelf(createSelfLinkWithUUID(storedResource.getId(), exampleResource.getProfile()));
		exampleResource.setEmbeddedResource(convert(storedResource.getEmbedded()));
		resourceMapping.put(exampleResource.getSelf().getHref(), storedResource.getId());
		return exampleResource;
	}

	private ExampleEmbeddedResource convert(StoredEmbeddedObject embedded) {
		ExampleEmbeddedResource embRes = new ExampleEmbeddedResource();
		embRes.setName(embedded.getName());
		embRes.setSelf(createSelfLinkWithUUID(embedded.getId(), embRes.getProfile()));
		resourceMapping.put(embRes.getSelf().getHref(), embedded.getId());
		return embRes;
	}

	protected HALLink createSelfLinkWithUUID(UUID id, URI resourceProfile) {
		return createLink(getAbsoluteResourceURI(id.toString()), resourceProfile);
	}
}
