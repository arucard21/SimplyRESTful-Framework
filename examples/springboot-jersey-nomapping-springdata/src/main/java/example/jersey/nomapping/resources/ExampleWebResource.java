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
package example.jersey.nomapping.resources;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import io.github.perplexhub.rsql.RSQLJPASupport;
import io.openapitools.jackson.dataformat.hal.HALLink;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.tags.Tag;
import simplyrestful.api.framework.core.DefaultWebResource;
import simplyrestful.api.framework.core.MediaTypeUtils;
import simplyrestful.api.framework.core.SortOrder;
import simplyrestful.api.framework.core.WebResourceUtils;
import simplyrestful.api.framework.core.webresource.api.implementation.DefaultCollectionGetEventStream;
import simplyrestful.api.framework.springdata.paging.OffsetBasedPageRequest;

@Named
@Path("/resources")
@OpenAPIDefinition(tags = {
	@Tag(name = "Example Resources")
})
@Produces({
    MediaTypeUtils.APPLICATION_HAL_JSON + "; profile=\"" + ExampleResource.EXAMPLE_PROFILE_STRING + "\"; qs=0.5",
    ExampleResource.EXAMPLE_MEDIA_TYPE_JSON+"; qs=0.8"})
@Consumes({
    MediaTypeUtils.APPLICATION_HAL_JSON + "; profile=\"" + ExampleResource.EXAMPLE_PROFILE_STRING + "\"",
    ExampleResource.EXAMPLE_MEDIA_TYPE_JSON})
public class ExampleWebResource implements DefaultWebResource<ExampleResource>, DefaultCollectionGetEventStream<ExampleResource> {
    private static final String RSQL_JPA_SORT_QUERY_DIRECTION_DELIMITER = ",";
    private static final String RSQL_JPA_SORT_QUERY_FIELD_DELIMITER = ";";
    private static final String RSQL_JPA_SORT_QUERY_DIRECTION_ASCENDING = "asc";
    private static final String RSQL_JPA_SORT_QUERY_DIRECTION_DESCENDING = "desc";
    private static final String ERROR_UPDATE_RESOURCE_DOES_NOT_EXIST = "The provided resources does not exist so it can not be updated";
    private static final String ERROR_CREATE_RESOURCE_ALREADY_EXISTS = "The provided resources already exists so it can not be created";
    private static final String ERROR_RESOURCE_NO_IDENTIFIER = "Resource contains no unique identifier at all, neither a UUID nor a self link.";

    private ExampleRepository repo;
    @Context
    ResourceInfo resourceInfo;
    @Context
    UriInfo uriInfo;

    @Inject
    public ExampleWebResource(ExampleRepository repo) {
        this.repo = repo;
        addInitialTestData(repo);
    }

    private void addInitialTestData(ExampleRepository repo) {
	if(repo.count() == 0) {
	    for(int i = 0; i < 3; i++) {
		ExampleResource resource = new ExampleResource();
       	    	resource.setUUID(UUID.randomUUID());
       	    	resource.setDescription("This is test resource "+ i);
       	    	ExampleComplexAttribute complexAttribute = new ExampleComplexAttribute();
       	    	complexAttribute.setName("complex attribute of test resource "+ i);
       	    	resource.setComplexAttribute(complexAttribute);
       	    	repo.save(resource);
       	    }
	}
    }

    @Override
    public ExampleResource create(ExampleResource resource, UUID resourceUUID) {
	ensureSelfLinkAndUUIDPresent(resource);
	Optional<ExampleResource> entity = repo.findByUuid(resourceUUID);
	if (entity.isPresent()) {
	    throw new IllegalArgumentException(ERROR_CREATE_RESOURCE_ALREADY_EXISTS);
	}
	ExampleResource persistedEntity = repo.save(resource);
	ensureSelfLinkAndUUIDPresent(persistedEntity);
	return persistedEntity;
    }

    @Override
    public ExampleResource read(UUID resourceUUID) {
	Optional<ExampleResource> entity = repo.findByUuid(resourceUUID);
	if (entity.isPresent()) {
	    ExampleResource retrievedEntity = entity.get();
	    ensureSelfLinkAndUUIDPresent(retrievedEntity);
	    return retrievedEntity;
	}
	return null;
    }

    @Override
    public ExampleResource update(ExampleResource resource, UUID resourceUUID) {
	ensureSelfLinkAndUUIDPresent(resource);
	Optional<ExampleResource> entity = repo.findByUuid(resourceUUID);
	if (entity.isPresent()) {
	    ExampleResource retrievedEntity = entity.get();
	    resource.setId(retrievedEntity.getId());
	    resource.setUUID(resourceUUID);
	    ExampleResource persistedEntity = repo.save(resource);
	    ensureSelfLinkAndUUIDPresent(persistedEntity);
	    return persistedEntity;
	}
	throw new IllegalArgumentException(ERROR_UPDATE_RESOURCE_DOES_NOT_EXIST);
    }

    @Override
    public ExampleResource delete(UUID resourceUUID) {
	ExampleResource previousValue = read(resourceUUID);
	if (previousValue == null) {
	    return null;
	}
	repo.delete(previousValue);
	ensureSelfLinkAndUUIDPresent(previousValue);
	return previousValue;
    }

    @Override
    public List<ExampleResource> list(int pageStart, int pageSize, List<String> fields, String query, List<SortOrder> sort) {
	List<ExampleResource> retrievedPage = repo.findAll(
		RSQLJPASupport.<ExampleResource>toSpecification(query).and(RSQLJPASupport.toSort(createSortQuery(sort))),
		new OffsetBasedPageRequest(pageStart, pageSize))
		.getContent();
	return retrievedPage.stream().map(resource -> ensureSelfLinkAndUUIDPresent(resource)).collect(Collectors.toList());
    }

    @Override
    public Stream<ExampleResource> stream(List<String> fields, String query, List<SortOrder> sort) {
	return repo.findAll(RSQLJPASupport.<ExampleResource>toSpecification(query).and(RSQLJPASupport.toSort(createSortQuery(sort))))
		.map(resource -> {
		    simulateSlowDataRetrieval();
		    return resource;
		})
		.map(this::ensureSelfLinkAndUUIDPresent);
    }

    @Override
    public int count(String query) {
        return Math.toIntExact(repo.count(RSQLJPASupport.toSpecification(query)));
    }

    @Override
    public boolean exists(UUID resourceUUID) {
        return repo.existsByUuid(resourceUUID);
    }

    private void simulateSlowDataRetrieval() {
    	try {
    	    Thread.sleep(1000);
    	} catch (InterruptedException e) {
    	    e.printStackTrace();
    	}
    }

    private String createSortQuery(List<SortOrder> sort) {
    	return sort.stream()
    		.map(this::createSingleSortQueryField)
    		.collect(Collectors.joining(RSQL_JPA_SORT_QUERY_FIELD_DELIMITER));
    }

    private String createSingleSortQueryField(SortOrder sort) {
    	String direction = sort.isAscending() ? RSQL_JPA_SORT_QUERY_DIRECTION_ASCENDING : RSQL_JPA_SORT_QUERY_DIRECTION_DESCENDING;
    	return String.join(RSQL_JPA_SORT_QUERY_DIRECTION_DELIMITER, sort.getField(), direction);
    }

    private ExampleResource ensureSelfLinkAndUUIDPresent(ExampleResource persistedResource) {
    	if (persistedResource.getSelf() == null && persistedResource.getUUID() == null) {
    	    throw new IllegalStateException(ERROR_RESOURCE_NO_IDENTIFIER);
    	}
    	if (persistedResource.getSelf() == null) {
    	    persistedResource.setSelf(new HALLink.Builder(UriBuilder.fromUri(WebResourceUtils.getAbsoluteWebResourceURI(resourceInfo, uriInfo))
    		    .path(persistedResource.getUUID().toString()).build())
    			    .type(MediaTypeUtils.APPLICATION_HAL_JSON).profile(persistedResource.getProfile())
    			    .build());
    	}
    	if (persistedResource.getUUID() == null) {
    	    UUID id = UUID.fromString(WebResourceUtils.getAbsoluteWebResourceURI(resourceInfo, uriInfo)
    		    .relativize(URI.create(persistedResource.getSelf().getHref())).getPath());
    	    persistedResource.setUUID(id);
    	}
    	return persistedResource;
    }
}
