package simplyrestful.api.framework.test.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import simplyrestful.api.framework.core.AdditionalMediaTypes;
import simplyrestful.api.framework.core.DefaultWebResource;

@Path("testresources")
@Produces(AdditionalMediaTypes.APPLICATION_HAL_JSON + "; profile=\"" + TestResource.PROFILE_STRING+ "\"")
@Consumes(AdditionalMediaTypes.APPLICATION_HAL_JSON + "; profile=\"" + TestResource.PROFILE_STRING+ "\"")
public class TestWebResource extends DefaultWebResource<TestResource>{
	public static final List<TestResource> TEST_RESOURCES = new ArrayList<>();

	@Override
	public TestResource create(TestResource resource, UUID resourceUUID) {
		return resource;
	}

	@Override
	public TestResource read(UUID resourceUUID) {
		if(Objects.equals(resourceUUID, TestResource.TEST_RESOURCE_ID)) {
			return TEST_RESOURCES.get(0);
		}
		return null;
	}

	@Override
	public TestResource update(TestResource resource, UUID resourceUUID) {
		return TEST_RESOURCES.get(0);
	}

	@Override
	public TestResource delete(UUID resourceUUID) {
		if(Objects.equals(resourceUUID, TestResource.TEST_RESOURCE_ID)) {
			return TEST_RESOURCES.get(0);
		}
		return null;
	}

	@Override
	public List<TestResource> list(int pageStart, int pageSize, List<String> fields, String query, Map<String, Boolean> sort) {
		return TEST_RESOURCES;
	}

	@Override
	public int count(String query) {
	    return TEST_RESOURCES.size();
	}

	@Override
	public Stream<TestResource> stream(List<String> fields, String query, Map<String, Boolean> sort) {
	    return TEST_RESOURCES.stream();
	}

	@Override
	public boolean exists(UUID resourceUUID) {
	    return this.read(resourceUUID) != null;
	}
}
