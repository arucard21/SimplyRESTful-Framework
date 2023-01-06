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

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import example.resources.jpa.ExampleComplexAttribute;
import example.resources.jpa.ExampleResource;
import io.github.perplexhub.rsql.RSQLJPASupport;
import io.swagger.v3.oas.annotations.tags.Tag;
import simplyrestful.api.framework.DefaultWebResource;
import simplyrestful.api.framework.WebResourceUtils;
import simplyrestful.api.framework.queryparams.SortOrder;
import simplyrestful.api.framework.resources.APICollection;
import simplyrestful.api.framework.resources.Link;
import simplyrestful.api.framework.springdata.paging.OffsetBasedPageRequest;

@Named
@Path("/resources")
@Tag(name = "Example Resources")
@Produces(ExampleResource.EXAMPLE_MEDIA_TYPE_JSON)
@Consumes(ExampleResource.EXAMPLE_MEDIA_TYPE_JSON)
public class ExampleWebResource implements DefaultWebResource<ExampleResource> {
	public static final String ERROR_UPDATE_RESOURCE_DOES_NOT_EXIST = "The provided resources does not exist so it can not be updated";
	public static final String ERROR_CREATE_RESOURCE_ALREADY_EXISTS = "The provided resources already exists so it can not be created";
	public static final String ERROR_RESOURCE_NO_IDENTIFIER = "Resource contains no unique identifier at all, neither a UUID nor a self link.";
	private ExampleRepository repo;
	@Context
	ResourceInfo resourceInfo;
	@Context
	UriInfo uriInfo;
	@Context
	ContainerRequestContext requestContext;

	@Inject
	public ExampleWebResource(ExampleRepository repo) {
		this.repo = repo;
		addInitialTestData(repo);
	}

	private void addInitialTestData(ExampleRepository repo) {
		if (repo.count() == 0) {
			for (int i = 0; i < 3; i++) {
				ExampleResource resource = new ExampleResource();
				resource.setUUID(UUID.randomUUID());
				resource.setDescription("This is test resource " + i);
				ExampleComplexAttribute complexAttribute = new ExampleComplexAttribute();
				complexAttribute.setName("complex attribute of test resource " + i);
				resource.setComplexAttribute(complexAttribute);
				resource.setDateTime(ZonedDateTime.now(ZoneOffset.UTC));
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
	public List<ExampleResource> list(int pageStart, int pageSize, List<String> fields, String query,
			List<SortOrder> sort) {
		List<ExampleResource> retrievedPage = repo.findAll(RSQLJPASupport.<ExampleResource>toSpecification(query),
				new OffsetBasedPageRequest(pageStart, pageSize, map(sort))).getContent();
		return retrievedPage.stream().map(resource -> ensureSelfLinkAndUUIDPresent(resource))
				.collect(Collectors.toList());
	}
//    @Override
//    public Stream<ExampleResource> stream(List<String> fields, String query, List<SortOrder> sort) {
//	return repo.findAll(RSQLJPASupport.<ExampleResource>toSpecification(query), map(sort))
//		.map(resource -> {
//		    simulateSlowDataRetrieval();
//		    return resource;
//		})
//		.map(this::ensureSelfLinkAndUUIDPresent);
//    }

	@Override
	public int count(String query) {
		return Math.toIntExact(repo.count(RSQLJPASupport.toSpecification(query)));
	}

	@Override
	public boolean exists(UUID resourceUUID) {
		return repo.existsByUuid(resourceUUID);
	}

//	private void simulateSlowDataRetrieval() {
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//	}

	private Sort map(List<SortOrder> sort) {
		if (sort == null || sort.isEmpty()) {
			return Sort.unsorted();
		}
		return Sort.by(sort.stream().map(sortOrder -> sortOrder.isAscending() ? Order.asc(sortOrder.getField())
				: Order.desc(sortOrder.getField())).collect(Collectors.toList()));
	}

	private ExampleResource ensureSelfLinkAndUUIDPresent(ExampleResource persistedResource) {
		if (persistedResource.getSelf() == null && persistedResource.getUUID() == null) {
			throw new IllegalStateException(ERROR_RESOURCE_NO_IDENTIFIER);
		}
		if (persistedResource.getSelf() == null) {
			persistedResource.setSelf(new Link(
					UriBuilder.fromUri(WebResourceUtils.getAbsoluteWebResourceURI(resourceInfo, uriInfo)).path(persistedResource.getUUID().toString()).build(),
					MediaType.valueOf(ExampleResource.EXAMPLE_MEDIA_TYPE_JSON)));
		}
		if (persistedResource.getUUID() == null) {
			UUID id = UUID.fromString(WebResourceUtils.getAbsoluteWebResourceURI(resourceInfo, uriInfo)
					.relativize(persistedResource.getSelf().getHref())
					.getPath());
			persistedResource.setUUID(id);
		}
		return persistedResource;
	}

	@Override
	public APICollection<ExampleResource> listAPIResources(ContainerRequestContext requestContext, ResourceInfo resourceInfo, UriInfo uriInfo, HttpHeaders httpHeaders, int pageStart, int pageSize, List<String> fields, String query, List<String> sort) {
		return DefaultWebResource.super.listAPIResources(requestContext, resourceInfo, uriInfo, httpHeaders, pageStart, pageSize, fields, query, sort);
	}

	@Override
	public ExampleResource getAPIResource(ContainerRequestContext requestContext, ResourceInfo resourceInfo, UriInfo uriInfo, HttpHeaders httpHeaders, @NotNull UUID id, List<String> fields) {
		return DefaultWebResource.super.getAPIResource(requestContext, resourceInfo, uriInfo, httpHeaders, id, fields);
	}

	@Override
	public Response postAPIResource(ResourceInfo resourceInfo, UriInfo uriInfo, @NotNull @Valid ExampleResource resource) {
		return DefaultWebResource.super.postAPIResource(resourceInfo, uriInfo, resource);
	}

	@Override
	public Response putAPIResource(ResourceInfo resourceInfo, UriInfo uriInfo, @NotNull UUID id, @NotNull @Valid ExampleResource resource) {
		return DefaultWebResource.super.putAPIResource(resourceInfo, uriInfo, id, resource);
	}

	@Override
	public Response deleteAPIResource(@NotNull UUID id) {
		return DefaultWebResource.super.deleteAPIResource(id);
	}
}
