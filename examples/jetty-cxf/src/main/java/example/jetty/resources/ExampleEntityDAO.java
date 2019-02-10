package example.jetty.resources;

import java.util.List;
import java.util.UUID;

import example.datastore.StoredObject;
import simplyrestful.api.framework.core.EntityDAO;

public class ExampleEntityDAO extends EntityDAO<StoredObject> {

	@Override
	public long count() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<StoredObject> findAllForPage(int pageNumber, int pageSize) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StoredObject findByUUID(UUID entityID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StoredObject persist(StoredObject entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StoredObject remove(UUID entityID) {
		// TODO Auto-generated method stub
		return null;
	}

}
