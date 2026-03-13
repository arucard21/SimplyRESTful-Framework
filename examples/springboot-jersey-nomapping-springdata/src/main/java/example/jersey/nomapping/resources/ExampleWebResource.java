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
import java.util.stream.Stream;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import cz.jirutka.rsql.parser.RSQLParserException;
import io.github.perplexhub.rsql.RSQLJPASupport;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Response;
import simplyrestful.api.framework.DefaultWebResource;
import simplyrestful.api.framework.queryparams.SortOrder;
import simplyrestful.api.framework.resources.ApiCollection;
import simplyrestful.api.framework.utils.WebResourceUtils;
import simplyrestful.api.framework.webresource.api.implementation.DefaultCollectionGetEventStream;
import simplyrestful.api.framework.springdata.paging.OffsetBasedPageRequest;

@Named
@Path("/resources")
@Tag(name = "Example Resources")
@Produces(ExampleResourceRecord.EXAMPLE_MEDIA_TYPE_JSON)
@Consumes(ExampleResourceRecord.EXAMPLE_MEDIA_TYPE_JSON)
public class ExampleWebResource implements DefaultWebResource<ExampleResourceRecord>, DefaultCollectionGetEventStream<ExampleResourceRecord> {
	public static final String ERROR_UPDATE_RESOURCE_DOES_NOT_EXIST = "The provided resources does not exist so it can not be updated";
	public static final String ERROR_CREATE_RESOURCE_ALREADY_EXISTS = "The provided resources already exists so it can not be created";
	private ExampleRepository repo;
	@Context
	UriInfo uriInfo;

	@Inject
	public ExampleWebResource(ExampleRepository repo) {
		this.repo = repo;
		addInitialTestData(repo);
	}

	private void addInitialTestData(ExampleRepository repo) {
		if (repo.count() == 0) {
			for (int i = 0; i < 3; i++) {
				ExampleResourceEntity entity = new ExampleResourceEntity();
				entity.setUuid(UUID.randomUUID());
				entity.setDescription("This is test resource " + i);
				ExampleComplexAttributeEntity complexAttribute = new ExampleComplexAttributeEntity();
				complexAttribute.setName("complex attribute of test resource " + i);
				entity.setComplexAttribute(complexAttribute);
				entity.setDateTime(ZonedDateTime.now(ZoneOffset.UTC));
				repo.save(entity);
			}
		}
	}

	private ExampleResourceMapper mapper() {
		return new ExampleResourceMapper(uriInfo);
	}

	@Override
	public ExampleResourceRecord create(ExampleResourceRecord resource) {
		ExampleResourceEntity entity = mapper().toEntity(resource);
		entity.setUuid(UUID.randomUUID());
		ExampleResourceEntity persisted = repo.save(entity);
		return mapper().toRecord(persisted);
	}

	@Override
	public ExampleResourceRecord read(UUID resourceUUID) {
		Optional<ExampleResourceEntity> entity = repo.findByUuid(resourceUUID);
		return entity.map(e -> mapper().toRecord(e)).orElse(null);
	}

	@Override
	public ExampleResourceRecord update(ExampleResourceRecord resource) {
		UUID resourceUUID = WebResourceUtils.parseUuidFromLastSegmentOfUri(resource.self().getHref());
		Optional<ExampleResourceEntity> existing = repo.findByUuid(resourceUUID);
		if (existing.isPresent()) {
			ExampleResourceEntity existingEntity = existing.get();
			ExampleResourceEntity entity = mapper().toEntity(resource);
			entity.setId(existingEntity.getId());
			entity.setUuid(resourceUUID);
			ExampleResourceEntity persisted = repo.save(entity);
			return mapper().toRecord(persisted);
		}
		throw new IllegalArgumentException(ERROR_UPDATE_RESOURCE_DOES_NOT_EXIST);
	}

	@Override
	public ExampleResourceRecord delete(UUID resourceUUID) {
		Optional<ExampleResourceEntity> entity = repo.findByUuid(resourceUUID);
		if (entity.isEmpty()) {
			return null;
		}
		ExampleResourceEntity existingEntity = entity.get();
		repo.delete(existingEntity);
		return mapper().toRecord(existingEntity);
	}

	@Override
	public List<ExampleResourceRecord> list(int pageStart, int pageSize, List<String> fields, String query,
			List<SortOrder> sort) {
		try {
			List<ExampleResourceEntity> retrievedPage = repo.findAll(RSQLJPASupport.<ExampleResourceEntity>toSpecification(query),
					new OffsetBasedPageRequest(pageStart, pageSize, map(sort))).getContent();
			return retrievedPage.stream().map(entity -> mapper().toRecord(entity))
					.collect(Collectors.toList());
		}
		catch(RSQLParserException e) {
			throw new BadRequestException("The FIQL query could not be parsed");
		}
	}

	@Override
	public Stream<ExampleResourceRecord> stream(List<String> fields, String query, List<SortOrder> sort) {
		return repo.findAll(RSQLJPASupport.<ExampleResourceEntity>toSpecification(query), map(sort))
			.map(entity -> {
			    simulateSlowDataRetrieval();
			    return entity;
			})
			.map(entity -> mapper().toRecord(entity));
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

	private Sort map(List<SortOrder> sort) {
		if (sort == null || sort.isEmpty()) {
			return Sort.unsorted();
		}
		return Sort.by(sort.stream().map(sortOrder -> sortOrder.isAscending() ? Order.asc(sortOrder.getField())
				: Order.desc(sortOrder.getField())).collect(Collectors.toList()));
	}

	@Override
	public ApiCollection<ExampleResourceRecord> listAPIResources(UriInfo uriInfo, int pageStart, int pageSize, List<String> fields, String query, List<String> sort) {
		return DefaultWebResource.super.listAPIResources(uriInfo, pageStart, pageSize, fields, query, sort);
	}

	@Override
	public ExampleResourceRecord getAPIResource(@NotNull UUID id, List<String> fields) {
		return DefaultWebResource.super.getAPIResource(id, fields);
	}

	@Override
	public Response postAPIResource(@NotNull @Valid ExampleResourceRecord resource) {
		return DefaultWebResource.super.postAPIResource(resource);
	}

	@Override
	public Response putAPIResource(@NotNull UUID id, @NotNull @Valid ExampleResourceRecord resource) {
		return DefaultWebResource.super.putAPIResource(id, resource);
	}

	@Override
	public Response deleteAPIResource(@NotNull UUID id) {
		return DefaultWebResource.super.deleteAPIResource(id);
	}
}