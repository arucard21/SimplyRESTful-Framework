package simplyrestful.api.framework.test.implementation;

import java.net.URI;

import javax.ws.rs.Path;

import simplyrestful.api.framework.core.AbstractWebResource;
import simplyrestful.api.framework.core.ResourceDAO;

@Path("testresources")
public class TestWebResource extends AbstractWebResource<TestResource>{
	public static final URI TEST_REQUEST_BASE_URI = URI.create("local://resources/");

	@Override
	protected URI getAbsoluteURI(Class<?> resourceEndpoint, String id) {
		if (id == null) {
			return TEST_REQUEST_BASE_URI;
		}
		if (TestResource.TEST_RESOURCE_ID.equals(id)) {
			return TestResource.TEST_RESOURCE_URI;
		}
		return null;
	}

	public TestWebResource(ResourceDAO<TestResource> resourceDao) {
		super(resourceDao);
	}	
}
