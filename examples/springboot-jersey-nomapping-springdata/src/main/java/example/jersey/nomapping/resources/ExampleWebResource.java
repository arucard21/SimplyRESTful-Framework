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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.UriBuilder;

import example.jersey.nomapping.OffsetBasedPageRequest;
import io.github.perplexhub.rsql.RSQLSupport;
import io.openapitools.jackson.dataformat.hal.HALLink;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.tags.Tag;
import simplyrestful.api.framework.core.AdditionalMediaTypes;
import simplyrestful.api.framework.core.DefaultWebResource;

@Named
@Path("/resources")
@OpenAPIDefinition(tags = {
	@Tag(name = "Example Resources")
})
@Produces(AdditionalMediaTypes.APPLICATION_HAL_JSON + "; profile=\"" + ExampleResource.EXAMPLE_PROFILE_STRING + "\"")
@Consumes(AdditionalMediaTypes.APPLICATION_HAL_JSON + "; profile=\"" + ExampleResource.EXAMPLE_PROFILE_STRING + "\"")
public class ExampleWebResource extends DefaultWebResource<ExampleResource> {
    private static final String RSQL_JPA_SORT_QUERY_DIRECTION_DELIMITER = ",";
    private static final String RSQL_JPA_SORT_QUERY_FIELD_DELIMITER = ";";
    private static final String RSQL_JPA_SORT_QUERY_DIRECTION_ASCENDING = "asc";
    private static final String RSQL_JPA_SORT_QUERY_DIRECTION_DESCENDING = "desc";
    private static final String ERROR_UPDATE_RESOURCE_DOES_NOT_EXIST = "The provided resources does not exist so it can not be updated";
    private static final String ERROR_CREATE_RESOURCE_ALREADY_EXISTS = "The provided resources already exists so it can not be created";
    private static final String ERROR_RESOURCE_NO_IDENTIFIER = "Resource contains no unique identifier at all, neither a UUID nor a self link.";
    private ExampleRepository repo;

    @Inject
    public ExampleWebResource(ExampleRepository repo) {
	super();
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
    public List<ExampleResource> list(int pageStart, int pageSize, List<String> fields, String query, Map<String, Boolean> sort) {
	List<ExampleResource> retrievedPage = repo.findAll(
		RSQLSupport.<ExampleResource>toSpecification(query).and(RSQLSupport.toSort(createSortQuery(sort))),
		new OffsetBasedPageRequest(pageStart, pageSize))
		.getContent();
	retrievedPage.forEach(resource -> ensureSelfLinkAndUUIDPresent(resource));
	return retrievedPage;
    }

    private String createSortQuery(Map<String, Boolean> sort) {
	return sort.entrySet().stream()
		.map(entry -> createSingleSortQueryField(entry.getKey(), entry.getValue()))
		.collect(Collectors.joining(RSQL_JPA_SORT_QUERY_FIELD_DELIMITER));
    }

    private String createSingleSortQueryField(String sortField, Boolean ascending) {
	String direction = RSQL_JPA_SORT_QUERY_DIRECTION_ASCENDING;
	if (!ascending) {
	    direction = RSQL_JPA_SORT_QUERY_DIRECTION_DESCENDING;
	}
	return String.join(RSQL_JPA_SORT_QUERY_DIRECTION_DELIMITER,sortField, direction);
	
    }

    @Override
    public int count(String query) {
	return Math.toIntExact(repo.count(RSQLSupport.toSpecification(query)));
    }

    private void ensureSelfLinkAndUUIDPresent(ExampleResource persistedResource) {
	if (persistedResource.getSelf() == null && persistedResource.getUUID() == null) {
	    throw new IllegalStateException(ERROR_RESOURCE_NO_IDENTIFIER);
	}
	if (persistedResource.getSelf() == null) {
	    persistedResource.setSelf(new HALLink.Builder(UriBuilder.fromUri(getAbsoluteWebResourceURI())
		    .path(persistedResource.getUUID().toString()).build())
			    .type(AdditionalMediaTypes.APPLICATION_HAL_JSON).profile(persistedResource.getProfile())
			    .build());
	}
	if (persistedResource.getUUID() == null) {
	    UUID id = UUID.fromString(getAbsoluteWebResourceURI()
		    .relativize(URI.create(persistedResource.getSelf().getHref())).getPath());
	    persistedResource.setUUID(id);
	}
    }
}
