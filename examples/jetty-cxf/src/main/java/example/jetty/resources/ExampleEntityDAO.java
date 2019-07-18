package example.jetty.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import simplyrestful.api.framework.core.EntityDAO;

public class ExampleEntityDAO implements EntityDAO<ExampleResource> {
	private List<ExampleResource> unmappedDatastore;

	public ExampleEntityDAO() {
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

	@Override
	public long count() {
		return unmappedDatastore.size();
	}

	@Override
	public List<ExampleResource> findAllForPage(int pageNumber, int pageSize) {
		if(pageNumber - 1 < 0) {
			pageNumber = 0;
		}
		if(pageSize > count()) {
			pageSize = unmappedDatastore.size();
		}
		return unmappedDatastore.subList((pageNumber - 1) * pageSize, pageSize);
	}

	@Override
	public ExampleResource findByUUID(UUID entityID) {
		return unmappedDatastore.stream()
				.filter(resource -> resource.getUUID().equals(entityID))
				.findFirst()
				.orElseGet(() -> null);
	}

	@Override
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

	@Override
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

}
