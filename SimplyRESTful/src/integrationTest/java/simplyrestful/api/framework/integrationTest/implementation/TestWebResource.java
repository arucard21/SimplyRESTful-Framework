package simplyrestful.api.framework.integrationTest.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import simplyrestful.api.framework.DefaultWebResource;
import simplyrestful.api.framework.queryparams.SortOrder;
import simplyrestful.api.framework.resources.Link;
import simplyrestful.api.framework.webresource.api.implementation.DefaultCollectionGetEventStream;

@Path("testresources")
@Produces(TestResource.MEDIA_TYPE_JSON)
@Consumes(TestResource.MEDIA_TYPE_JSON)
public class TestWebResource implements DefaultWebResource<TestResource>, DefaultCollectionGetEventStream<TestResource>{
	public static final List<TestResource> TEST_RESOURCES = new ArrayList<>();
	public static final UUID TEST_RESOURCE_ID = UUID.randomUUID();

	@Context
	UriInfo uriInfo;

	@Override
	public TestResource create(TestResource resource) {
		resource.setSelf(new Link(
				uriInfo.getBaseUriBuilder().path(TestWebResource.class).path(TEST_RESOURCE_ID.toString()).build(),
    			resource.customJsonMediaType()));
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
	public TestResource update(TestResource resource) {
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
