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

import java.net.URI;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;

import example.resources.jpa.ExampleComplexAttribute;
import example.resources.jpa.ExampleResource;
import io.openapitools.jackson.dataformat.hal.HALLink;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.tags.Tag;
import simplyrestful.api.framework.DefaultWebResource;
import simplyrestful.api.framework.MediaTypeUtils;
import simplyrestful.api.framework.WebResourceUtils;
import simplyrestful.api.framework.queryparams.SortOrder;
import simplyrestful.api.framework.resources.HALCollection;
import simplyrestful.api.framework.resources.HALCollectionV1;
import simplyrestful.api.framework.resources.HALCollectionV2;
import simplyrestful.api.framework.webresource.api.CollectionGet;
import simplyrestful.api.framework.webresource.api.ResourceGet;
import simplyrestful.api.framework.webresource.api.implementation.DefaultCollectionGetEventStream;

@SuppressWarnings("deprecation")
@RequestScoped
@Path("/resources")
@OpenAPIDefinition(tags = { @Tag(name = "Example Resources") })
@Produces({
        MediaTypeUtils.APPLICATION_HAL_JSON + "; profile=\"" + ExampleResource.EXAMPLE_PROFILE_STRING + "\"; qs=0.5",
        ExampleResource.EXAMPLE_MEDIA_TYPE_JSON + "; qs=0.8" })
@Consumes({ MediaTypeUtils.APPLICATION_HAL_JSON + "; profile=\"" + ExampleResource.EXAMPLE_PROFILE_STRING + "\"",
        ExampleResource.EXAMPLE_MEDIA_TYPE_JSON })
public class ExampleWebResource
        implements DefaultWebResource<ExampleResource>, DefaultCollectionGetEventStream<ExampleResource> {
    private static final String ERROR_UPDATE_RESOURCE_DOES_NOT_EXIST = "The provided resources does not exist so it can not be updated";
    private static final String ERROR_CREATE_RESOURCE_ALREADY_EXISTS = "The provided resources already exists so it can not be created";
    private static final String ERROR_RESOURCE_NO_IDENTIFIER = "Resource contains no unique identifier at all, neither a UUID nor a self link.";
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
            persistedResource.setSelf(new HALLink.Builder(
                    UriBuilder.fromUri(WebResourceUtils.getAbsoluteWebResourceURI(resourceInfo, uriInfo))
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

    /**
     * Quarkus seems to prefer the JAX-RS annotations to be in the same class or interface. It does not seem to adhere
     * to the annotation inheritance defined by JAX-RS. So the JAX-RS annotated methods from their respective interfaces
     * are duplicated here so Quarkus can find them.
     */

    @Override
    @GET
    @Produces({
    HALCollectionV2.MEDIA_TYPE_HAL_JSON+";qs=0.7",
    HALCollectionV2.MEDIA_TYPE_JSON+";qs=0.9",
    HALCollectionV1.MEDIA_TYPE_HAL_JSON+";qs=0.2"
    })
    public HALCollection<ExampleResource> listHALResources(
    		@Context
    		ContainerRequestContext requestContext,
	        @Context
	        ResourceInfo resourceInfo,
	        @Context
	        UriInfo uriInfo,
	        @Context
	        HttpHeaders httpHeaders,
	        @QueryParam(V1_QUERY_PARAM_PAGE)
	        @DefaultValue(V1_QUERY_PARAM_PAGE_DEFAULT)
	        int page,
	        @QueryParam(QUERY_PARAM_PAGE_START)
	        @DefaultValue(QUERY_PARAM_PAGE_START_DEFAULT)
	        int pageStart,
	        @QueryParam(QUERY_PARAM_PAGE_SIZE)
	        @DefaultValue(QUERY_PARAM_PAGE_SIZE_DEFAULT)
	        int pageSize,
	        @QueryParam(V1_QUERY_PARAM_COMPACT)
	        @DefaultValue(V1_QUERY_PARAM_COMPACT_DEFAULT)
	        boolean compact,
	        @QueryParam(QUERY_PARAM_FIELDS)
	        @DefaultValue(CollectionGet.QUERY_PARAM_FIELDS_DEFAULT)
	        List<String> fields,
	        @QueryParam(QUERY_PARAM_QUERY)
	        @DefaultValue(QUERY_PARAM_QUERY_DEFAULT)
	        String query,
	        @QueryParam(QUERY_PARAM_SORT)
	        @DefaultValue(QUERY_PARAM_SORT_DEFAULT)
	        List<String> sort) {
        return DefaultWebResource.super.listHALResources(requestContext, resourceInfo, uriInfo, httpHeaders, page, pageStart, pageSize, compact,
                fields, query, sort);
    }

    @Override
    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS+";qs=0.1")
    public void streamHALResources(
    		@Context
    		ContainerRequestContext requestContext,
	        @QueryParam(QUERY_PARAM_FIELDS)
	        @DefaultValue(CollectionGet.QUERY_PARAM_FIELDS_DEFAULT)
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
        DefaultCollectionGetEventStream.super.streamHALResources(requestContext, fields, query, sort, eventSink, sse);
    }

    @Override
    @POST
    public Response postHALResource(
            @Context
            ResourceInfo resourceInfo,
            @Context
            UriInfo uriInfo,
            @NotNull
            @Valid
            ExampleResource resource) {
        return DefaultWebResource.super.postHALResource(resourceInfo, uriInfo, resource);
    }

    @Override
    @Path("/{id}")
    @GET
    public ExampleResource getHALResource(
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
	        @QueryParam(CollectionGet.QUERY_PARAM_FIELDS)
	        @DefaultValue(ResourceGet.QUERY_PARAM_FIELDS_DEFAULT)
	        List<String> fields) {
        return DefaultWebResource.super.getHALResource(requestContext, resourceInfo, uriInfo, httpHeaders, id, fields);
    }

    @Override
    @Path("/{id}")
    @PUT
    public Response putHALResource(
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
        return DefaultWebResource.super.putHALResource(resourceInfo, uriInfo, id, resource);
    }

    @Override
    @Path("/{id}")
    @DELETE
    public Response deleteHALResource(
        @PathParam("id")
        @NotNull
        UUID id) {
        return DefaultWebResource.super.deleteHALResource(id);
    }
}
