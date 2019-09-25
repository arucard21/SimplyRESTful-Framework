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
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.UriBuilder;

import io.openapitools.jackson.dataformat.hal.HALLink;
import io.swagger.annotations.Api;
import simplyrestful.api.framework.core.AdditionalMediaTypes;
import simplyrestful.api.framework.core.DefaultWebResource;

@Named
@Path("/resources")
@Api(value = "Example Resources")
@Produces(AdditionalMediaTypes.APPLICATION_HAL_JSON + "; profile=\""+ExampleResource.EXAMPLE_PROFILE_STRING + "\"")
@Consumes(AdditionalMediaTypes.APPLICATION_HAL_JSON + "; profile=\""+ExampleResource.EXAMPLE_PROFILE_STRING + "\"")
public class ExampleWebResource extends DefaultWebResource<ExampleResource> {
	private ExampleEntityDAO entityDao;
	
	@Inject
	public ExampleWebResource(ExampleEntityDAO entityDao) {
		this.entityDao = entityDao;
	}
	
	@Override
	public ExampleResource create(ExampleResource resource, UUID resourceUUID) {
		ExampleResource entity = entityDao.persist(map(resource));
		if (entity == null) {
			return null;
		}
		return map(entity);
	}

	@Override
	public ExampleResource read(UUID resourceUUID) {
		return map(entityDao.findByUUID(resourceUUID));
	}

	@Override
	public ExampleResource update(ExampleResource resource, UUID resourceUUID) {
		ExampleResource entity = entityDao.persist(map(resource));
		if (entity == null) {
			return null;
		}
		return map(entity);
	}

	@Override
	public ExampleResource delete(UUID resourceUUID) {
		ExampleResource entity = entityDao.remove(resourceUUID);
		if (entity == null) {
			return null;
		}
		return map(entity);
	}

	@Override
	public List<ExampleResource> listing(long pageNumber, long pageSize) {
		return entityDao.findAllForPage(pageNumber, pageSize).stream()
				.map(entity -> map(entity))
				.collect(Collectors.toList());
	}

	/**
	 * This simple map method allows using the same POJO as both the API resource and the entity used for persistence. 
	 * 
	 * @param entity is either the API resource or the entity
	 * @return the entity for the given API resource, or the API resource for the given entity 
	 */
	private ExampleResource map(ExampleResource entity) {
		if(entity == null) {
			return null;
		}
		ensureSelfLinkAndUUIDPresent(entity);
		return entity;
	}

	private void ensureSelfLinkAndUUIDPresent(ExampleResource persistedResource) {
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
