package simplyrestful.api.framework.core.mapper;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import io.openapitools.jackson.dataformat.hal.HALLink;
import simplyrestful.api.framework.core.MediaType;
import simplyrestful.api.framework.resources.HALResource;

/**
 * This class provides a mapper for the simple case where no actual mapping takes place.
 * 
 * This mapper requires the API resource and the persisted entity to be the same object.
 * No actual mapping occurs since the resource and entity are the same.
 *
 * @param <T> is the object that is used as both the API resource and the persisted entity.
 */
public class NoMappingMapper<T extends HALResource> implements ResourceMapper<T, T>{
	public T map(T resource) {
		return resource;
	}

	@Override
	public T map(T entity, URI resourceURI) {
		addSelfLink(entity, resourceURI);
		return entity;
	}

	private void addSelfLink(T persistedResource, URI absoluteWebResourceURI) {
		persistedResource.setSelf(new HALLink.Builder(UriBuilder.fromUri(absoluteWebResourceURI).path(persistedResource.getUUID().toString()).build())
				.type(MediaType.APPLICATION_HAL_JSON)
				.profile(persistedResource.getProfile())
				.build());
	}
}