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
package example.microprofile.quarkus.microprofile.quarkus.resources;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

import example.resources.jpa.ExampleComplexAttribute;
import example.resources.jpa.ExampleResource;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.tags.Tag;
import simplyrestful.api.framework.DefaultWebResource;
import simplyrestful.api.framework.WebResourceUtils;
import simplyrestful.api.framework.queryparams.SortOrder;
import simplyrestful.api.framework.resources.APICollection;
import simplyrestful.api.framework.resources.Link;
import simplyrestful.api.framework.webresource.api.implementation.DefaultCollectionGet;
import simplyrestful.api.framework.webresource.api.implementation.DefaultCollectionGetEventStream;
import simplyrestful.api.framework.webresource.api.implementation.DefaultResourceGet;

@RequestScoped
@Path("/resources")
@OpenAPIDefinition(tags = { @Tag(name = "Example Resources") })
@Produces(ExampleResource.EXAMPLE_MEDIA_TYPE_JSON)
@Consumes(ExampleResource.EXAMPLE_MEDIA_TYPE_JSON)
public class ExampleWebResource
        implements DefaultWebResource<ExampleResource>, DefaultCollectionGetEventStream<ExampleResource> {
	public static final String ERROR_UPDATE_RESOURCE_DOES_NOT_EXIST = "The provided resources does not exist so it can not be updated";
	public static final String ERROR_CREATE_RESOURCE_ALREADY_EXISTS = "The provided resources already exists so it can not be created";
	public static final String ERROR_RESOURCE_NO_IDENTIFIER = "Resource contains no unique identifier at all, neither a UUID nor a self link.";
    @Inject
    ExampleRepository repo;
    @Context
    ResourceInfo resourceInfo;
    @Context
    UriInfo uriInfo;

    @PostConstruct
    @Transactional
    void addInitialTestData() {
        if (repo.count() == 0) {
            for (int i = 0; i < 3; i++) {
                ExampleResource resource = new ExampleResource();
                resource.setUUID(UUID.randomUUID());
                resource.setDescription("This is test resource " + i);
                ExampleComplexAttribute complexAttribute = new ExampleComplexAttribute();
                complexAttribute.setName("complex attribute of test resource " + i);
                resource.setComplexAttribute(complexAttribute);
                resource.setDateTime(ZonedDateTime.now(ZoneOffset.UTC));
                repo.persist(resource);
            }
        }
    }

    @Override
    @Transactional
    public ExampleResource create(ExampleResource resource, UUID resourceUUID) {
        ensureSelfLinkAndUUIDPresent(resource);
        Optional<ExampleResource> entity = repo.findByUuid(resourceUUID);
        if (entity.isPresent()) {
            throw new IllegalArgumentException(ERROR_CREATE_RESOURCE_ALREADY_EXISTS);
        }
        repo.persist(resource);
        ExampleResource persistedEntity = repo.findByUuid(resourceUUID).orElseThrow();
        ensureSelfLinkAndUUIDPresent(persistedEntity);
        return persistedEntity;
    }

    @Override
    @Transactional
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
    @Transactional
    public ExampleResource update(ExampleResource resource, UUID resourceUUID) {
        ensureSelfLinkAndUUIDPresent(resource);
        Optional<ExampleResource> entity = repo.findByUuid(resourceUUID);
        if (entity.isPresent()) {
            ExampleResource retrievedEntity = entity.get();
            resource.setId(retrievedEntity.getId());
            resource.setUUID(resourceUUID);
            repo.persist(resource);
            ExampleResource persistedEntity = repo.findByUuid(resourceUUID).orElseThrow();
            ensureSelfLinkAndUUIDPresent(persistedEntity);
            return persistedEntity;
        }
        throw new IllegalArgumentException(ERROR_UPDATE_RESOURCE_DOES_NOT_EXIST);
    }

    @Override
    @Transactional
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
    @Transactional
    public List<ExampleResource> list(int pageStart, int pageSize, List<String> fields, String query,
            List<SortOrder> sort) {
        List<ExampleResource> retrievedPage = repo.findAll(createSortQuery(sort)).range(pageStart, pageSize).list();
        // FIXME add query functionality
//		RSQLJPASupport.<ExampleResource>toSpecification(query).and(RSQLJPASupport.toSort(createSortQuery(sort))),
//		new OffsetBasedPageRequest(pageStart, pageSize))
//		.getContent();
        return retrievedPage.stream().map(resource -> ensureSelfLinkAndUUIDPresent(resource))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Stream<ExampleResource> stream(List<String> fields, String query, List<SortOrder> sort) {
        return repo.findAll(createSortQuery(sort)).stream()
                // FIXME add query functionality
//	        RSQLJPASupport.<ExampleResource>toSpecification(query).and(RSQLJPASupport.toSort(createSortQuery(sort))))
                .map(resource -> {
                    simulateSlowDataRetrieval();
                    return resource;
                }).map(this::ensureSelfLinkAndUUIDPresent);
    }

    @Override
    @Transactional
    public int count(String query) {
        return Math.toIntExact(repo.count());
        // FIXME add query functionality
//        return Math.toIntExact(repo.count(RSQLJPASupport.toSpecification(query)));
    }

    @Override
    @Transactional
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

    private Sort createSortQuery(List<SortOrder> sortFields) {
        Sort sortQuery = null;
        for (SortOrder sortField : sortFields) {
            if (sortQuery == null) {
                sortQuery = Sort.by(sortField.getField(),
                        sortField.isAscending() ? Direction.Ascending : Direction.Descending);
            } else {
                sortQuery.and(sortField.getField(),
                        sortField.isAscending() ? Direction.Ascending : Direction.Descending);
            }
        }
        return sortQuery;
    }

    private ExampleResource ensureSelfLinkAndUUIDPresent(ExampleResource persistedResource) {
        if (persistedResource.getSelf() == null && persistedResource.getUUID() == null) {
            throw new IllegalStateException(ERROR_RESOURCE_NO_IDENTIFIER);
        }
        if (persistedResource.getSelf() == null) {
            persistedResource.setSelf(new Link(
            		UriBuilder.fromUri(WebResourceUtils.getAbsoluteWebResourceURI(resourceInfo, uriInfo)).path(persistedResource.getUUID().toString()).build(),
            		persistedResource.customJsonMediaType()));
        }
        if (persistedResource.getUUID() == null) {
            UUID id = UUID.fromString(WebResourceUtils.getAbsoluteWebResourceURI(resourceInfo, uriInfo)
                    .relativize(persistedResource.getSelf().getHref())
                    .getPath());
            persistedResource.setUUID(id);
        }
        return persistedResource;
    }

    /**
     * Quarkus seems to prefer the JAX-RS annotations to be in the same class or interface. It does not seem to adhere
     * to the annotation inheritance defined by JAX-RS. So the JAX-RS annotated methods from their respective interfaces
     * are duplicated here so Quarkus can find them.
     */

    @Override
    @GET
    @Produces(APICollection.MEDIA_TYPE_JSON)
    public APICollection<ExampleResource> listAPIResources(
    		@Context
    		ContainerRequestContext requestContext,
	        @Context
	        ResourceInfo resourceInfo,
	        @Context
	        UriInfo uriInfo,
	        @Context
	        HttpHeaders httpHeaders,
	        @QueryParam(QUERY_PARAM_PAGE_START)
	        @DefaultValue(QUERY_PARAM_PAGE_START_DEFAULT)
	        int pageStart,
	        @QueryParam(QUERY_PARAM_PAGE_SIZE)
	        @DefaultValue(QUERY_PARAM_PAGE_SIZE_DEFAULT)
	        int pageSize,
	        @QueryParam(QUERY_PARAM_FIELDS)
	        @DefaultValue(DefaultCollectionGet.QUERY_PARAM_FIELDS_DEFAULT)
	        List<String> fields,
	        @QueryParam(QUERY_PARAM_QUERY)
	        @DefaultValue(QUERY_PARAM_QUERY_DEFAULT)
	        String query,
	        @QueryParam(QUERY_PARAM_SORT)
	        @DefaultValue(QUERY_PARAM_SORT_DEFAULT)
	        List<String> sort) {
        return DefaultWebResource.super.listAPIResources(requestContext, resourceInfo, uriInfo, httpHeaders, pageStart, pageSize, fields, query, sort);
    }

    @Override
    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS+";qs=0.1")
    public void streamAPIResources(
    		@Context
    		ContainerRequestContext requestContext,
	        @QueryParam(QUERY_PARAM_FIELDS)
	        @DefaultValue(DefaultCollectionGet.QUERY_PARAM_FIELDS_DEFAULT)
	        List<String> fields,
	        @QueryParam(QUERY_PARAM_QUERY)
	        @DefaultValue(QUERY_PARAM_QUERY_DEFAULT)
	        String query,
	        @QueryParam(QUERY_PARAM_SORT)
	        @DefaultValue(QUERY_PARAM_SORT_DEFAULT)
	        List<String> sort,
	        @Context
	        SseEventSink eventSink,
	        @Context
	        Sse sse) {
        DefaultCollectionGetEventStream.super.streamAPIResources(requestContext, fields, query, sort, eventSink, sse);
    }

    @Override
    @POST
    public Response postAPIResource(
            @Context
            ResourceInfo resourceInfo,
            @Context
            UriInfo uriInfo,
            @NotNull
            @Valid
            ExampleResource resource) {
        return DefaultWebResource.super.postAPIResource(resourceInfo, uriInfo, resource);
    }

    @Override
    @Path("/{id}")
    @GET
    public ExampleResource getAPIResource(
    		@Context
    		ContainerRequestContext requestContext,
	        @Context
	        ResourceInfo resourceInfo,
	        @Context
	        UriInfo uriInfo,
	        @Context
	        HttpHeaders httpHeaders,
	        @PathParam("id")
	        @NotNull
	        UUID id,
	        @QueryParam(DefaultCollectionGet.QUERY_PARAM_FIELDS)
	        @DefaultValue(DefaultResourceGet.QUERY_PARAM_FIELDS_DEFAULT)
	        List<String> fields) {
        return DefaultWebResource.super.getAPIResource(requestContext, resourceInfo, uriInfo, httpHeaders, id, fields);
    }

    @Override
    @Path("/{id}")
    @PUT
    public Response putAPIResource(
            @Context
            ResourceInfo resourceInfo,
            @Context
            UriInfo uriInfo,
            @PathParam("id")
            @NotNull
            UUID id,
            @NotNull
            @Valid
            ExampleResource resource) {
        return DefaultWebResource.super.putAPIResource(resourceInfo, uriInfo, id, resource);
    }

    @Override
    @Path("/{id}")
    @DELETE
    public Response deleteAPIResource(
        @PathParam("id")
        @NotNull
        UUID id) {
        return DefaultWebResource.super.deleteAPIResource(id);
    }
}
