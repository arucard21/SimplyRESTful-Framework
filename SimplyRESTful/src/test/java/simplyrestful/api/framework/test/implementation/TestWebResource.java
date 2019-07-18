package simplyrestful.api.framework.test.implementation;

import java.net.URI;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import simplyrestful.api.framework.core.DefaultWebResource;
import simplyrestful.api.framework.core.MediaType;
import simplyrestful.api.framework.core.ResourceDAO;

@Path("testresources")
@Produces(MediaType.APPLICATION_HAL_JSON + "; profile=\"" + TestResource.PROFILE_STRING+ "\"")
@Consumes(MediaType.APPLICATION_HAL_JSON + "; profile=\"" + TestResource.PROFILE_STRING+ "\"")
public class TestWebResource extends DefaultWebResource<TestResource>{
	public static final URI TEST_REQUEST_BASE_URI = URI.create("local://resources/testresources/");

	@Override
	protected URI getAbsoluteWebResourceURI(Class<?> resourceEndpoint, UUID id) {
		if (id == null) {
			return TEST_REQUEST_BASE_URI;
		}
		if (TestResource.TEST_RESOURCE_ID.equals(id)) {
			return TestResource.TEST_RESOURCE_URI;
		}
		return null;
	}
	
	@Override
	protected URI getRequestURI() {
		return TEST_REQUEST_BASE_URI;
	}

	public TestWebResource(ResourceDAO<TestResource> resourceDao) {
		super(resourceDao);
	}	
}
