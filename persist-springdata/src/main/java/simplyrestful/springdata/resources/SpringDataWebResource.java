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
package simplyrestful.springdata.resources;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.core.UriBuilder;

import org.springframework.data.domain.PageRequest;

import io.openapitools.jackson.dataformat.hal.HALLink;
import simplyrestful.api.framework.core.AdditionalMediaTypes;
import simplyrestful.api.framework.core.DefaultWebResource;
import simplyrestful.springdata.repository.SpringDataRepository;

/**
 * @deprecated Direct mapping of API resources to database entities is not useful enough to maintain this convenience library.
 * Use the standard SimplyRESTful library (without automated mapping) instead.
 * 
 */
@Deprecated
public class SpringDataWebResource<T extends SpringDataHALResource> extends DefaultWebResource<T> {
	private static final String ERROR_UPDATE_RESOURCE_DOES_NOT_EXIST = "The provided resources does not exist so it can not be updated";
	private static final String ERROR_CREATE_RESOURCE_ALREADY_EXISTS = "The provided resources already exists so it can not be created";
	private static final String ERROR_RESOURCE_NO_IDENTIFIER = "Resource contains no unique identifier at all, neither a UUID nor a self link.";
	private SpringDataRepository<T> repo;

	public SpringDataWebResource(SpringDataRepository<T> repo) {
		this.repo = repo;
	}

	@Override
	public T create(T resource, UUID resourceUUID) {
		ensureSelfLinkAndUUIDPresent(resource);
		Optional<T> entity = repo.findByUuid(resourceUUID);
		if (entity.isPresent()) {
			throw new IllegalArgumentException(ERROR_CREATE_RESOURCE_ALREADY_EXISTS);
		}
		T persistedEntity = repo.save(resource);
		ensureSelfLinkAndUUIDPresent(persistedEntity);
		return persistedEntity;
	}

	@Override
	public T read(UUID resourceUUID) {
		Optional<T> entity = repo.findByUuid(resourceUUID);
		if (entity.isPresent()) {
			T retrievedEntity = entity.get();
			ensureSelfLinkAndUUIDPresent(retrievedEntity);
			return retrievedEntity;
		}
		return null;
	}

	@Override
	public T update(T resource, UUID resourceUUID) {
		ensureSelfLinkAndUUIDPresent(resource);
		Optional<T> entity = repo.findByUuid(resourceUUID);
		if (entity.isPresent()) {
			T retrievedEntity = entity.get();
			resource.setId(retrievedEntity.getId());
			resource.setUUID(resourceUUID);
			T persistedEntity = repo.save(resource);
			ensureSelfLinkAndUUIDPresent(persistedEntity);
			return persistedEntity;
		}
		throw new IllegalArgumentException(ERROR_UPDATE_RESOURCE_DOES_NOT_EXIST);
	}

	@Override
	public T delete(UUID resourceUUID) {
		T previousValue = read(resourceUUID);
		if (previousValue == null) {
			return null;
		}
		repo.delete(previousValue);
		ensureSelfLinkAndUUIDPresent(previousValue);
		return previousValue;
	}

	@Override
	public List<T> list(long pageNumber, long pageSize) {
		int pageZeroIndexed = Math.toIntExact(pageNumber) - 1;
		int integerPageSize = (pageSize > Integer.valueOf(Integer.MAX_VALUE).longValue()) ? Integer.MAX_VALUE
				: Math.toIntExact(pageSize);
		List<T> retrievedPage = repo.findAll(PageRequest.of(pageZeroIndexed, integerPageSize)).getContent();
		retrievedPage.forEach(resource -> ensureSelfLinkAndUUIDPresent(resource));
		return retrievedPage;
	}

	protected SpringDataRepository<T> getRepo() {
		return repo;
	}

	private void ensureSelfLinkAndUUIDPresent(T persistedResource) {
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
