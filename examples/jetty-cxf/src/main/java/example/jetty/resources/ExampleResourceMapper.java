package example.jetty.resources;

import java.net.URI;

import example.datastore.StoredObject;
import simplyrestful.api.framework.core.mapper.ResourceMapper;

public class ExampleResourceMapper implements ResourceMapper<ExampleResource, StoredObject> {

	@Override
	public StoredObject map(ExampleResource resource) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExampleResource map(StoredObject entity, URI resourceURI) {
		// TODO Auto-generated method stub
		return null;
	}

}
