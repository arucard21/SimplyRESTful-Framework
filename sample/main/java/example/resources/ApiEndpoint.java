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

package example.resources;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.Path;

import org.apache.commons.collections4.bidimap.TreeBidiMap;
import org.apache.cxf.jaxrs.ext.search.SearchCondition;

import api.framework.core.ApiEndpointBase;
import api.framework.core.hal.HalCollection;
import api.framework.core.hal.HalCollectionFactory;
import dk.nykredit.jackson.dataformat.hal.HALLink;
import example.data.DataStore;
import example.data.StoredResource;
import io.swagger.annotations.Api;

@Path("/resources")
@Api(value = "Example Resources")
public class ApiEndpoint extends ApiEndpointBase<ExampleResource> {
	/**
	 * Contains the mapping between the API's HAL resources, identified by
	 * the resource's URI, and the data store's resources, identified by a
	 * UUID. In an actual implementation, this mapping should probably be
	 * persistently stored, not kept in-memory.
	 */
	private TreeBidiMap<String, UUID> resourceMapping;
	private DataStore dataStore;

    public ApiEndpoint() {
    	resourceMapping = new TreeBidiMap<String, UUID>();
    	dataStore = new DataStore();
    }

    @Override
	protected HalCollection<ExampleResource> createPagedCollection(int pageNumber, int pageSize, boolean compact) {
    	List<ExampleResource> exampleResources = new ArrayList<ExampleResource>();
    	for (StoredResource storedResource: dataStore.getData().values()){
    		exampleResources.add(convert(storedResource));
    	}
    	SearchCondition<ExampleResource> searchCondition = searchContext.getCondition(ExampleResource.class);
    	if (searchCondition != null){
    		exampleResources = searchCondition.findAll(exampleResources);
    	}
    	return new HalCollectionFactory<ExampleResource>().createPagedCollection(exampleResources, pageNumber, pageSize, uriInfo.getRequestUri(), compact);
	}

	@Override
	protected void addResourceToDataStore(ExampleResource resource) {
		// there's probably much smarter ways to handle conversion and mapping
		HALLink selfLink = resource.getSelf();
		if (selfLink == null){
			UUID resourceID = UUID.randomUUID();
			selfLink = createSelfLinkWithUUID(resourceID);
			resource.setSelf(selfLink);
			resourceMapping.put(selfLink.getHref(), resourceID);
		}
		String resourceURI = selfLink.getHref();
		UUID dataID = resourceMapping.get(resourceURI);
		if (dataID == null){
			dataID = UUID.randomUUID();
			resourceMapping.put(resourceURI, dataID);
		}
		dataStore.getData().put(dataID, convert(resource));
	}

	@Override
	protected ExampleResource retrieveResourceFromDataStore(String resourceURI) {
		UUID dataID = resourceMapping.get(resourceURI);
		if (dataID == null){
			return null;
		}
		StoredResource resourceFromDataStore = dataStore.getData().get(dataID);
		return resourceFromDataStore == null ? null : convert(resourceFromDataStore);
	}

	@Override
	protected ExampleResource updateResourceInDataStore(ExampleResource resource) {
		String resourceURI = resource.getSelf().getHref();
		if (resourceMapping.get(resourceURI) == null){
			// add this resource to the resource mapping
			resourceMapping.put(resourceURI, UUID.randomUUID());
		}
		StoredResource previousData = dataStore.getData().put(resourceMapping.get(resourceURI), convert(resource));
		return previousData == null ? null : convert(previousData);
	}

	@Override
	protected ExampleResource removeResourceFromDataStore(String resourceURI) {
		UUID dataID = resourceMapping.get(resourceURI);
		if (dataID == null){
			return null;
		}
		StoredResource previousData = dataStore.getData().remove(dataID);
		return previousData == null ? null : convert(previousData);
	}

	@Override
	protected ExampleResource handleHateoasAction(String resourceURI, String action) {
		switch(action){
			case "test": return retrieveResourceFromDataStore(resourceURI);
		}
		return null;
	}

	private StoredResource convert(ExampleResource exampleResource) {
		StoredResource dataResource = new StoredResource();
		dataResource.setDescription(exampleResource.getDescription());
		dataResource.setId(resourceMapping.get(exampleResource.getSelf().getHref()));
		return dataResource;
	}

	private ExampleResource convert(StoredResource storedResource) {
		ExampleResource exampleResource = new ExampleResource();
		exampleResource.setDescription(storedResource.getDescription());
		String resourceURI = resourceMapping.getKey(storedResource.getId());
		if (resourceURI == null){
			// create resource URI with new UUID and add it to the mapping
			exampleResource.setSelf(createSelfLinkWithUUID(storedResource.getId()));
			resourceMapping.put(exampleResource.getSelf().getHref(), storedResource.getId());
		}
		else{
			// use the existing resource URI to create the self link
			exampleResource.setSelf(createSelfLink(URI.create(resourceURI)));
		}
		return exampleResource;
	}

	@Override
	protected boolean exists(String resourceURI) {
		UUID dataID = resourceMapping.get(resourceURI);
		return dataID == null ? false : true;
	}
}
