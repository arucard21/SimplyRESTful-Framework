package simplyrestful.springdata.repository.nomapping;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.core.UriBuilder;

import org.springframework.data.domain.PageRequest;

import io.openapitools.jackson.dataformat.hal.HALLink;
import simplyrestful.api.framework.core.ResourceDAO;
import simplyrestful.api.framework.core.exceptions.InvalidResourceException;
import simplyrestful.api.framework.core.exceptions.InvalidSelfLinkException;

public abstract class NoMappingResourceDAO<T extends NoMappingHALResource> implements ResourceDAO<T>{
	private final NoMappingRepository<T, UUID> repo;
	
	public NoMappingResourceDAO(NoMappingRepository<T, UUID> repo) {
		this.repo = repo;
	}

	@Override
	public long count() {
		return repo.count();
	}

	@Override
	public List<T> findAllForPage(int pageNumber, int pageSize, URI absoluteWebResourceURI) {
		List<T> resources = repo.findAll(PageRequest.of(pageNumber-1, pageSize)).getContent();
		for (T resource : resources) {
			addSelfLink(resource, absoluteWebResourceURI);
		}
		return resources;
	}

	@Override
	public T findByURI(URI resourceURI, URI absoluteWebResourceURI) {
		URI relativizedURI = absoluteWebResourceURI.relativize(resourceURI);
		UUID resourceId = UUID.fromString(relativizedURI.getPath());
		Optional<T> result = repo.findByUuid(resourceId);
		if (result.isPresent()) {
			T retrievedResource = result.get();
			addSelfLink(retrievedResource, absoluteWebResourceURI);
			return retrievedResource;
		}
		return null;
	}

	@Override
	public T persist(T resource, URI absoluteWebResourceURI) throws InvalidResourceException, InvalidSelfLinkException {
		if (resource.getUUID() == null) {
			resource.setUUID(UUID.randomUUID());
		}
		T persistedResource = repo.save(resource);
		addSelfLink(persistedResource, absoluteWebResourceURI);
		return persistedResource;
	}

	@Override
	public T remove(URI resourceURI, URI absoluteWebResourceURI) {
		T previousValue = findByURI(resourceURI, absoluteWebResourceURI);
		if (previousValue == null) {
			return null;
		}
		repo.delete(previousValue);
		addSelfLink(previousValue, absoluteWebResourceURI);
		return previousValue;
	}

	private void addSelfLink(T persistedResource, URI absoluteWebResourceURI) {
		persistedResource.setSelf(new HALLink.Builder(UriBuilder.fromUri(absoluteWebResourceURI).path(persistedResource.getUUID().toString()).build()).build());
	}
}
