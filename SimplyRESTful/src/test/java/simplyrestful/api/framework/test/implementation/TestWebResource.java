package simplyrestful.api.framework.test.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import simplyrestful.api.framework.DefaultWebResource;
import simplyrestful.api.framework.queryparams.SortOrder;
import simplyrestful.api.framework.webresource.api.implementation.DefaultCollectionGetEventStream;

@Path("testresources")
@Produces(TestResource.MEDIA_TYPE_JSON)
@Consumes(TestResource.MEDIA_TYPE_JSON)
public class TestWebResource implements DefaultWebResource<TestResource>, DefaultCollectionGetEventStream<TestResource>{
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
	public List<TestResource> list(int pageStart, int pageSize, List<String> fields, String query, List<SortOrder> sort) {
		return TEST_RESOURCES;
	}

	@Override
	public int count(String query) {
	    return TEST_RESOURCES.size();
	}

	@Override
	public Stream<TestResource> stream(List<String> fields, String query, List<SortOrder> sort) {
	    return TEST_RESOURCES.stream();
	}

	@Override
	public boolean exists(UUID resourceUUID) {
	    return this.read(resourceUUID) != null;
	}
}
