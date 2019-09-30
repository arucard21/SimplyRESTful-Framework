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
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.core.UriBuilder;

import io.openapitools.jackson.dataformat.hal.HALLink;
import simplyrestful.api.framework.core.AdditionalMediaTypes;
import simplyrestful.api.framework.core.DefaultWebResource;
import simplyrestful.springdata.repository.SpringDataEntityDAO;

public class SpringDataWebResource<T extends SpringDataHALResource> extends DefaultWebResource<T> {
	private SpringDataEntityDAO<T> entityDao;

	public SpringDataWebResource(SpringDataEntityDAO<T> entityDao) {
		this.entityDao = entityDao;
	}
	
	@Override
	public T create(T resource, UUID resourceUUID) {
		T entity = entityDao.persist(map(resource));
		if (entity == null) {
			return null;
		}
		return map(entity);
	}

	@Override
	public T read(UUID resourceUUID) {
		return map(entityDao.findByUUID(resourceUUID));
	}

	@Override
	public T update(T resource, UUID resourceUUID) {
		T entity = entityDao.persist(map(resource));
		if (entity == null) {
			return null;
		}
		return map(entity);
	}

	@Override
	public T delete(UUID resourceUUID) {
		T entity = entityDao.remove(resourceUUID);
		if (entity == null) {
			return null;
		}
		return map(entity);
	}

	@Override
	public List<T> list(long pageNumber, long pageSize) {
		return entityDao.findAllForPage(pageNumber, pageSize).stream()
				.map(entity -> map(entity))
				.collect(Collectors.toList());
	}

	public SpringDataEntityDAO<T> getEntityDao() {
		return entityDao;
	}

	/**
	 * This simple map method allows using the same POJO as both the API resource and the entity used for persistence. 
	 * 
	 * @param entity is either the API resource or the entity
	 * @return the entity for the given API resource, or the API resource for the given entity 
	 */
	private T map(T entity) {
		if(entity == null) {
			return null;
		}
		ensureSelfLinkAndUUIDPresent(entity);
		return entity;
	}

	private void ensureSelfLinkAndUUIDPresent(T persistedResource) {
		if(persistedResource.getSelf() == null) {
			persistedResource.setSelf(new HALLink.Builder(UriBuilder.fromUri(getAbsoluteWebResourceURI()).path(persistedResource.getUUID().toString()).build())
					.type(AdditionalMediaTypes.APPLICATION_HAL_JSON)
					.profile(persistedResource.getProfile())
					.build());
		}
		if(persistedResource.getUUID() == null) {
			UUID id = UUID.fromString(getAbsoluteWebResourceURI().relativize(URI.create(persistedResource.getSelf().getHref())).getPath());
			persistedResource.setUUID(id);
		}
	}
}
