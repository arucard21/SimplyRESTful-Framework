package simplyrestful.api.framework.test.implementation;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import simplyrestful.api.framework.core.AdditionalMediaTypes;
import simplyrestful.api.framework.core.DefaultWebResource;

@Path("testresources")
@Produces(AdditionalMediaTypes.APPLICATION_HAL_JSON + "; profile=\"" + TestResource.PROFILE_STRING+ "\"")
@Consumes(AdditionalMediaTypes.APPLICATION_HAL_JSON + "; profile=\"" + TestResource.PROFILE_STRING+ "\"")
public class TestWebResource extends DefaultWebResource<TestResource>{
	public static final URI TEST_REQUEST_BASE_URI = URI.create("local://resources/testresources/");
	public static final TestResource TEST_RESOURCE = new TestResource();
	
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

	@Override
	public TestResource create(TestResource resource, UUID resourceUUID) {
		return resource;
	}

	@Override
	public TestResource read(UUID resourceUUID) {
		if(Objects.equals(resourceUUID, TestResource.TEST_RESOURCE_ID)) {
			return TEST_RESOURCE;
		}
		return null;
	}

	@Override
	public TestResource update(TestResource resource, UUID resourceUUID) {
		return TEST_RESOURCE;
	}

	@Override
	public TestResource delete(UUID resourceUUID) {
		if(Objects.equals(resourceUUID, TestResource.TEST_RESOURCE_ID)) {
			return TEST_RESOURCE;
		}
		return null;
	}

	@Override
	public List<TestResource> listing(long pageNumber, long pageSize) {
		return Arrays.asList(TEST_RESOURCE, TestResource.random());
	}	
}
