package example.jetty.resources.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import example.jetty.resources.ExampleEmbeddedResource;
import example.jetty.resources.ExampleResource;

public class ExampleEntityDAOImpl implements ExampleEntityDAO {
	private List<ExampleResource> unmappedDatastore;

	public ExampleEntityDAOImpl() {
		List<ExampleResource> datastore = new ArrayList<>();

		ExampleResource resource1 =  new ExampleResource();
		resource1.setUUID(UUID.randomUUID());
        resource1.setDescription("This is the first stored object");
        ExampleResource resource2 =  new ExampleResource();
        resource2.setUUID(UUID.randomUUID());
        resource2.setDescription("This is the second stored object");

        ExampleEmbeddedResource embedded1 = new ExampleEmbeddedResource();
        embedded1.setName("Embedded 1");
        ExampleEmbeddedResource embedded2 = new ExampleEmbeddedResource();
        embedded2.setName("Embedded 2");

        resource1.setEmbeddedResource(embedded1);
        resource2.setEmbeddedResource(embedded2);

        datastore.add(resource1);
        datastore.add(resource2);

        this.unmappedDatastore = datastore;
	}

	public long count() {
		return unmappedDatastore.size();
	}

	public List<ExampleResource> findAllForPage(int pageStart, int pageSize) {
		int pageEnd = pageStart+pageSize;
		if(pageEnd > count()) {
			pageEnd = unmappedDatastore.size();
		}
		if(pageStart > pageEnd) {
			pageStart = pageEnd;
		}
		return unmappedDatastore.subList(pageStart, pageEnd);
	}

	public ExampleResource findByUUID(UUID entityID) {
		return unmappedDatastore.stream()
				.filter(resource -> resource.getUUID().equals(entityID))
				.findFirst()
				.orElseGet(() -> null);
	}

	public ExampleResource persist(ExampleResource entity) {
		int index = 0;
		while(index < count()) {
			if (unmappedDatastore.get(index).getUUID().equals(entity.getUUID())) {
				break;
			}
			index++;
		}
		if (index >= unmappedDatastore.size()) {
			unmappedDatastore.add(entity);
		}
		else {
			unmappedDatastore.set(index, entity);
		}
		return findByUUID(entity.getUUID());
	}

	public ExampleResource remove(UUID entityID) {
		int index = 0;
		while(index < count()) {
			if (unmappedDatastore.get(index).getUUID().equals(entityID)) {
				break;
			}
			index++;
		}
		if (index >= unmappedDatastore.size()) {
			return null;
		}
		else {
			return unmappedDatastore.remove(index);
		}
	}

	@Override
	public boolean exists(UUID entityID) {
	    return this.findByUUID(entityID) != null;
	}

	@Override
	public Stream<ExampleResource> stream() {
	    return this.unmappedDatastore.stream();
	}
}
