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
package example.jetty.resources;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import example.jetty.resources.dao.ExampleEntityDAO;
import io.openapitools.jackson.dataformat.hal.HALLink;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.tags.Tag;
import simplyrestful.api.framework.core.DefaultWebResource;
import simplyrestful.api.framework.core.MediaTypeUtils;
import simplyrestful.api.framework.core.SortOrder;
import simplyrestful.api.framework.core.WebResourceUtils;
import simplyrestful.api.framework.core.api.webresource.DefaultCollectionGetEventStream;

@Path("/resources")
@OpenAPIDefinition(tags = { @Tag(name = "Example Resources") })
@Produces(MediaTypeUtils.APPLICATION_HAL_JSON + "; profile=" + ExampleResource.EXAMPLE_PROFILE_STRING)
@Consumes(MediaTypeUtils.APPLICATION_HAL_JSON + "; profile=" + ExampleResource.EXAMPLE_PROFILE_STRING)
public class ExampleWebResource implements DefaultWebResource<ExampleResource>, DefaultCollectionGetEventStream<ExampleResource> {
    private ExampleEntityDAO dao;
    @Context
    private ResourceInfo resourceInfo;
    @Context
    private UriInfo uriInfo;

    public ExampleWebResource(@Context ExampleEntityDAO dao) {
	this.dao = dao;
    }

    @Override
    public ExampleResource create(ExampleResource resource, UUID resourceUUID) {
	ExampleResource entity = getEntityDao().persist(map(resource));
	if (entity == null) {
	    return null;
	}
	return map(entity);
    }

    @Override
    public ExampleResource read(UUID resourceUUID) {
	return map(getEntityDao().findByUUID(resourceUUID));
    }

    @Override
    public ExampleResource update(ExampleResource resource, UUID resourceUUID) {
	resource.setUUID(resourceUUID);
	ExampleResource entity = getEntityDao().persist(map(resource));
	if (entity == null) {
	    return null;
	}
	return map(entity);
    }

    @Override
    public ExampleResource delete(UUID resourceUUID) {
	ExampleResource entity = getEntityDao().remove(resourceUUID);
	return map(entity);
    }

    @Override
    public List<ExampleResource> list(int pageNumber, int pageSize, List<String> fields, String query,
	    List<SortOrder> sort) {
	if (!sort.isEmpty()) {
	    throw new ServerErrorException("This API does not yet support sorting", 501);
	}
	return getEntityDao().findAllForPage(pageNumber, pageSize).stream().map(entity -> map(entity))
		.collect(Collectors.toList());
    }

    @Override
    public int count(String query) {
	return Math.toIntExact(getEntityDao().count());
    }

    /**
     * This simple map method allows using the same POJO as both the API resource
     * and the entity used for persistence.
     *
     * @param entity is either the API resource or the entity
     * @return the entity for the given API resource, or the API resource for the
     *         given entity
     */
    private ExampleResource map(ExampleResource entity) {
	if (entity == null) {
	    return null;
	}
	ensureSelfLinkPresent(entity);
	return entity;
    }

    private void ensureSelfLinkPresent(ExampleResource persistedResource) {
	if (persistedResource.getSelf() == null) {
	    persistedResource.setSelf(new HALLink.Builder(UriBuilder.fromUri(WebResourceUtils.getAbsoluteWebResourceURI(resourceInfo, uriInfo))
		    .path(persistedResource.getUUID().toString()).build()).type(MediaTypeUtils.APPLICATION_HAL_JSON)
			    .profile(persistedResource.getProfile()).build());
	}
	if (persistedResource.getUUID() == null) {
	    UUID id = UUID.fromString(WebResourceUtils.getAbsoluteWebResourceURI(resourceInfo, uriInfo)
		    .relativize(URI.create(persistedResource.getSelf().getHref())).getPath());
	    persistedResource.setUUID(id);
	}
    }

    private ExampleEntityDAO getEntityDao() {
	return dao;
    }

    @Override
    public Stream<ExampleResource> stream(List<String> fields, String query, List<SortOrder> sort) {
	return dao.stream();
    }

    @Override
    public boolean exists(UUID resourceUUID) {
	return dao.exists(resourceUUID);
    }
}
