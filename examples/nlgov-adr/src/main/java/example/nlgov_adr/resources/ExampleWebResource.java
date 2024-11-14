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
package example.nlgov_adr.resources;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import cz.jirutka.rsql.parser.RSQLParserException;
import example.resources.jpa.ExampleComplexAttribute;
import example.resources.jpa.ExampleResource;
import io.github.perplexhub.rsql.RSQLJPASupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import simplyrestful.api.framework.DefaultWebResource;
import simplyrestful.api.framework.queryparams.SortOrder;
import simplyrestful.api.framework.resources.APICollection;
import simplyrestful.api.framework.resources.Link;
import simplyrestful.api.framework.springdata.paging.OffsetBasedPageRequest;
import simplyrestful.api.framework.utils.WebResourceUtils;

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
		try {
			List<ExampleResource> retrievedPage = repo.findAll(RSQLJPASupport.<ExampleResource>toSpecification(query),
					new OffsetBasedPageRequest(pageStart, pageSize, map(sort))).getContent();
			return retrievedPage.stream().map(resource -> ensureSelfLinkAndUUIDPresent(resource))
					.collect(Collectors.toList());
		}
		catch(RSQLParserException e) {
			throw new BadRequestException("The FIQL query could not be parsed");
		}
	}

	@Override
	public int count(String query) {
		return Math.toIntExact(repo.count(RSQLJPASupport.toSpecification(query)));
	}

	@Override
	public boolean exists(UUID resourceUUID) {
		return repo.existsByUuid(resourceUUID);
	}

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

	@ApiResponse(
    		responseCode = "200",
    		description = "A pageable collection containing your Example resources.",
    		content = {
    				@Content(
    						mediaType = APICollection.MEDIA_TYPE_JSON+";"+APICollection.MEDIA_TYPE_PARAMETER_ITEM_TYPE+"=\""+ExampleResource.EXAMPLE_MEDIA_TYPE_JSON+"\"",
    						schema = @Schema(ref = "#/components/schemas/APICollectionExampleResource"))},
    		headers = {
    				@Header(
    						name = "API-Version",
    						description = "Contains the full version number of the entire API since the URL contains only the major version number",
    						schema = @Schema(type = "string"))})
	@Override
	public APICollection<ExampleResource> listAPIResources(UriInfo uriInfo, int pageStart, int pageSize, List<String> fields, String query, List<String> sort) {
		return DefaultWebResource.super.listAPIResources(uriInfo, pageStart, pageSize, fields, query, sort);
	}

	@ApiResponse(
    		responseCode = "200",
    		description = "An API resource",
    		content = {
    				@Content(
    						mediaType = ExampleResource.EXAMPLE_MEDIA_TYPE_JSON,
    						schema = @Schema(implementation = ExampleResource.class))},
    		headers = {
    				@Header(
    						name = "API-Version",
    						description = "Contains the full version number of the entire API since the URL contains only the major version number",
    						schema = @Schema(type = "string"))})
	@Override
	public ExampleResource getAPIResource(@NotNull UUID id, List<String> fields) {
		return DefaultWebResource.super.getAPIResource(id, fields);
	}

	@ApiResponse(
    		responseCode = "201",
    		description = "Provides the location of the newly created Example resource.",
    		headers = {
    				@Header(
    						name = HttpHeaders.LOCATION,
    						description = "Contains the URI to the newly created Example resource",
    						schema = @Schema(type = "string", format = "uri")),
    				@Header(
    						name = "API-Version",
    						description = "Contains the full version number of the entire API since the URL contains only the major version number",
    						schema = @Schema(type = "string"))})
	@ApiResponse(
    		responseCode = "409",
    		description = "The self link in the Example resource conflicts with an existing Example resource so it was not created",
    		headers = {
    				@Header(
    						name = "API-Version",
    						description = "Contains the full version number of the entire API since the URL contains only the major version number",
    						schema = @Schema(type = "string"))})
	@Override
	public Response postAPIResource(ResourceInfo resourceInfo, UriInfo uriInfo, @NotNull @Valid ExampleResource resource) {
		return DefaultWebResource.super.postAPIResource(resourceInfo, uriInfo, resource);
	}


	@ApiResponse(
    		responseCode = "200",
    		description = "An API resource",
    		content = {
    				@Content(
    						mediaType = ExampleResource.EXAMPLE_MEDIA_TYPE_JSON,
    						schema = @Schema(implementation = ExampleResource.class))},
    		headers = {
    				@Header(
    						name = "API-Version",
    						description = "Contains the full version number of the entire API since the URL contains only the major version number",
    						schema = @Schema(type = "string"))})
	@ApiResponse(
    		responseCode = "404",
    		description = "The Example resource does not exist",
    		headers = {
    				@Header(
    						name = "API-Version",
    						description = "Contains the full version number of the entire API since the URL contains only the major version number",
    						schema = @Schema(type = "string"))})
	@Operation(description = "Modify an existing API resource.")
	@Override
	public Response putAPIResource(ResourceInfo resourceInfo, UriInfo uriInfo, @NotNull UUID id, @NotNull @Valid ExampleResource resource) {
		return DefaultWebResource.super.putAPIResource(resourceInfo, uriInfo, id, resource);
	}

	@ApiResponse(
    		responseCode = "204",
    		description = "The Example resource was deleted",
    		headers = {
    				@Header(
    						name = "API-Version",
    						description = "Contains the full version number of the entire API since the URL contains only the major version number",
    						schema = @Schema(type = "string"))})
	@Override
	public Response deleteAPIResource(@NotNull UUID id) {
		return DefaultWebResource.super.deleteAPIResource(id);
	}
}
