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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.ext.search.SearchCondition;
import org.apache.cxf.jaxrs.ext.search.SearchContext;

import example.datastore.DataStore;
import example.datastore.StoredEmbeddedObject;
import example.datastore.StoredObject;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.tags.Tag;
import simplyrestful.api.framework.DefaultWebResource;
import simplyrestful.api.framework.MediaTypeUtils;
import simplyrestful.api.framework.WebResourceUtils;
import simplyrestful.api.framework.queryparams.SortOrder;
import simplyrestful.api.framework.resources.Link;
import simplyrestful.api.framework.webresource.api.implementation.DefaultCollectionGetEventStream;

@Named
@Path("/resources")
@OpenAPIDefinition(tags = { @Tag(name = "Example Resources") })
@Produces(MediaTypeUtils.APPLICATION_HAL_JSON + "; profile=" + ExampleResource.EXAMPLE_PROFILE_STRING)
@Consumes(MediaTypeUtils.APPLICATION_HAL_JSON + "; profile=" + ExampleResource.EXAMPLE_PROFILE_STRING)
public class ExampleWebResource
		implements DefaultWebResource<ExampleResource>, DefaultCollectionGetEventStream<ExampleResource> {
	public static final ThreadLocal<SearchContext> REQUEST_SEARCHCONTEXT = new ThreadLocal<>();
	/**
	 * Contains the mapping between the API's HAL resources, identified by the
	 * resource's URI, and the data store's resources, identified by a UUID. In an
	 * actual implementation, this mapping should probably be persistently stored,
	 * not kept in-memory.
	 */
	private Map<URI, UUID> resourceMapping;
	private DataStore dataStore;
	@Context
	private ResourceInfo resourceInfo;
	@Context
	private UriInfo uriInfo;

	@Inject
	public ExampleWebResource(DataStore dataStore) {
		this.dataStore = dataStore;
		resourceMapping = new HashMap<>();
		for (StoredObject entity : dataStore.getData()) {
			UUID entityID = entity.getId();
			URI relativeResourceURI = UriBuilder.fromResource(ExampleWebResource.class).path(entityID.toString())
					.build();
			resourceMapping.put(relativeResourceURI, entityID);
		}
	}

	@Override
	public ExampleResource create(ExampleResource resource, UUID resourceUUID) {
		Link selfLink = resource.getSelf();
		if (selfLink == null) {
			throw new BadRequestException("The resource does not contain a self-link");
		}
		URI resourceURI = selfLink.getHref();
		if (resourceURI == null || resourceURI.toString().isEmpty()) {
			throw new BadRequestException("The resource contains an empty self-link");
		}
		if (resourceMapping.get(resourceURI) == null) {
			// add this resource to the resource mapping
			resourceMapping.put(resourceURI, UUID.randomUUID());
		}
		StoredObject previousData = dataStore.getObject(resourceMapping.get(resourceURI));
		if (previousData == null) {
			throw new IllegalStateException("The to-be-updated resource does not exist yet.");
		}
		dataStore.getData().remove(dataStore.getObject(resourceMapping.get(resourceURI)));
		dataStore.getData().add(convertToEntity(resource));
		return convertToResource(previousData);
	}

	@Override
	public ExampleResource read(UUID resourceUUID) {
		StoredObject resourceFromDataStore = dataStore.getObject(resourceUUID);
		return resourceFromDataStore == null ? null : convertToResource(resourceFromDataStore);
	}

	@Override
	public ExampleResource update(ExampleResource resource, UUID resourceUUID) {
		Link selfLink = resource.getSelf();
		if (selfLink == null) {
			throw new BadRequestException("The resource does not contain a self-link");
		}
		URI resourceURI = selfLink.getHref();
		if (resourceURI == null || resourceURI.toString().isEmpty()) {
			throw new BadRequestException("The resource contains an empty self-link");
		}
		if (resourceMapping.get(resourceURI) == null) {
			// add this resource to the resource mapping
			resourceMapping.put(resourceURI, UUID.randomUUID());
		}
		StoredObject previousData = dataStore.getObject(resourceMapping.get(resourceURI));
		if (previousData == null) {
			throw new IllegalStateException("The to-be-updated resource does not exist yet.");
		}
		dataStore.getData().remove(dataStore.getObject(resourceMapping.get(resourceURI)));
		dataStore.getData().add(convertToEntity(resource));
		return convertToResource(previousData);
	}

	@Override
	public ExampleResource delete(UUID resourceUUID) {
		StoredObject previousData = dataStore.getObject(resourceUUID);
		dataStore.getData().remove(previousData);
		return previousData == null ? null : convertToResource(previousData);
	}

	@Override
	public List<ExampleResource> list(int pageStart, int pageSize, List<String> fields, String query,
			List<SortOrder> sort) {
		if (!sort.isEmpty()) {
			throw new ServerErrorException("This API does not yet support sorting", 501);
		}
		List<StoredObject> data = dataStore.getData();
		int dataSize = data.size();
		if (pageStart > dataSize) {
			return Collections.emptyList();
		}
		int endElement = pageStart + pageSize;
		if (endElement > dataSize) {
			endElement = dataSize;
		}
		List<ExampleResource> resources = data.subList(pageStart, endElement).stream()
				.map((entity) -> convertToResource(entity)).collect(Collectors.toList());
		SearchContext searchContext = REQUEST_SEARCHCONTEXT.get();
		if (searchContext == null) {
			return resources;
		}
		SearchCondition<ExampleResource> searchCondition = searchContext.getCondition(ExampleResource.class);
		if (searchCondition == null) {
			return resources;
		}
		return searchCondition.findAll(resources);
	}

	@Override
	public int count(String query) {
		SearchContext searchContext = REQUEST_SEARCHCONTEXT.get();
		if (searchContext == null) {
			return dataStore.getData().size();
		}
		SearchCondition<ExampleResource> searchCondition = searchContext.getCondition(ExampleResource.class);
		if (searchCondition == null) {
			return dataStore.getData().size();
		}
		return searchCondition
				.findAll(dataStore.getData().stream().map(this::convertToResource).collect(Collectors.toList())).size();
	}

	@Override
	public Stream<ExampleResource> stream(List<String> fields, String query, List<SortOrder> sort) {
		return dataStore.getData().stream().map(this::convertToResource);
	}

	@Override
	public boolean exists(UUID resourceUUID) {
		return this.read(resourceUUID) != null;
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

	private ExampleResource convertToResource(StoredObject storedResource) {
		ExampleResource exampleResource = new ExampleResource();
		exampleResource.setDescription(storedResource.getDescription());
		// create resource URI with new UUID and add it to the mapping
		exampleResource.setSelf(new Link(
				WebResourceUtils.getAbsoluteWebResourceURI(resourceInfo, uriInfo, storedResource.getId()),
				MediaTypeUtils.APPLICATION_HAL_JSON_TYPE));
		exampleResource.setEmbeddedResource(convertEmbeddedToResource(storedResource.getEmbedded()));
		resourceMapping.put(WebResourceUtils.getAbsoluteWebResourceURI(resourceInfo, uriInfo), storedResource.getId());
		return exampleResource;
	}

	private ExampleEmbeddedResource convertEmbeddedToResource(StoredEmbeddedObject embedded) {
		ExampleEmbeddedResource embRes = new ExampleEmbeddedResource();
		embRes.setName(embedded.getName());
		resourceMapping.put(WebResourceUtils.getAbsoluteWebResourceURI(resourceInfo, uriInfo),
				embedded.getId());
		return embRes;
	}
}
