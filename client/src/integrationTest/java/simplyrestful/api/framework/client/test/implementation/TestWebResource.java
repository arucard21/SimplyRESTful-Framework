package simplyrestful.api.framework.client.test.implementation;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import javax.ws.rs.Consumes;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.tags.Tag;
import simplyrestful.api.framework.client.SimplyRESTfulClientTest;
import simplyrestful.api.framework.core.DefaultWebResource;
import simplyrestful.api.framework.core.MediaTypeUtils;
import simplyrestful.api.framework.core.SortOrder;
import simplyrestful.api.framework.core.webresource.api.implementation.DefaultCollectionGetEventStream;

@Path(TestWebResource.WEBRESOURCE_PATH)
@OpenAPIDefinition(
    tags = {
	    @Tag(name = "Test Resources")
    }
)
@Produces(MediaTypeUtils.APPLICATION_HAL_JSON + "; profile=\"" +TestResource.TEST_RESOURCE_PROFILE +"\"")
@Consumes(MediaTypeUtils.APPLICATION_HAL_JSON + "; profile=\"" +TestResource.TEST_RESOURCE_PROFILE +"\"")
public class TestWebResource implements DefaultWebResource<TestResource>, DefaultCollectionGetEventStream<TestResource>{
    public static final String WEBRESOURCE_PATH = "testresources";
    public static final UUID ERROR_READ_RESOURCE_ID = UUID.randomUUID();
    public static final UUID ERROR_UPDATE_RESOURCE_ID = UUID.randomUUID();

    private static URI baseUri;

    public static URI getBaseUri() {
        return baseUri;
    }

    public static void setBaseUri(URI baseUri) {
	TestWebResource.baseUri = baseUri;
    }

    @Override
    public TestResource create(TestResource resource, UUID resourceUUID) {
	// The provided resource is not actually stored anywhere in this test API.
	return resource;
    }

    @Override
    public TestResource read(UUID resourceUUID) {
	if (Objects.equals(resourceUUID, TestResource.TEST_RESOURCE_ID)) {
	    return SimplyRESTfulClientTest.getTestResource();
	}
	if (Objects.equals(resourceUUID, ERROR_READ_RESOURCE_ID)) {
	    throw new InternalServerErrorException("Pretending that something went wrong on the server");
	}
	return null;
    }

    @Override
    public TestResource update(TestResource resource, UUID resourceUUID) {
	if (Objects.equals(resourceUUID, ERROR_UPDATE_RESOURCE_ID)) {
	    throw new InternalServerErrorException("Pretending that something went wrong on the server");
	}
	return SimplyRESTfulClientTest.getTestResource();
    }

    @Override
    public TestResource delete(UUID resourceUUID) {
	if (Objects.equals(resourceUUID, TestResource.TEST_RESOURCE_ID)) {
	    return SimplyRESTfulClientTest.getTestResource();
	}
	return null;
    }

    @Override
    public List<TestResource> list(int pageStart, int pageSize, List<String> fields, String query, List<SortOrder> sort) {
	return Arrays.asList(SimplyRESTfulClientTest.getTestResource(), SimplyRESTfulClientTest.getTestResourceRandom());
    }

    @Override
    public int count(String query) {
	return 2;
    }

    @Override
    public Stream<TestResource> stream(List<String> fields, String query, List<SortOrder> sort) {
	return Stream.of(SimplyRESTfulClientTest.getTestResource(), SimplyRESTfulClientTest.getTestResourceRandom());
    }

    @Override
    public boolean exists(UUID resourceUUID) {
	return this.read(resourceUUID) != null;
    }
}
