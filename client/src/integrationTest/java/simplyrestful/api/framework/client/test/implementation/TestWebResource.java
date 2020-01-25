package simplyrestful.api.framework.client.test.implementation;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.UriBuilder;

import simplyrestful.api.framework.core.AdditionalMediaTypes;
import simplyrestful.api.framework.core.DefaultWebResource;

@Path(TestWebResource.WEBRESOURCE_PATH)
@Produces(AdditionalMediaTypes.APPLICATION_HAL_JSON + "; profile=\"" + TestWebResource.TEST_HOST_STRING + "/"+TestResource.TEST_RESOURCE_PROFILE_PATH+"\"")
@Consumes(AdditionalMediaTypes.APPLICATION_HAL_JSON + "; profile=\"" + TestWebResource.TEST_HOST_STRING + "/"+TestResource.TEST_RESOURCE_PROFILE_PATH+"\"")
public class TestWebResource extends DefaultWebResource<TestResource> {
    public static final String TEST_HOST_STRING = "http://localhost:9999";
    public static final URI TEST_HOST = URI.create(TEST_HOST_STRING);
    public static final String WEBRESOURCE_PATH = "testresources";
    public static final URI TEST_REQUEST_BASE_URI = UriBuilder.fromUri(TEST_HOST).path(WEBRESOURCE_PATH).build();
    public static final TestResource TEST_RESOURCE = new TestResource();

    @Override
    public TestResource create(TestResource resource, UUID resourceUUID) {
	return resource;
    }

    @Override
    public TestResource read(UUID resourceUUID) {
	if (Objects.equals(resourceUUID, TestResource.TEST_RESOURCE_ID)) {
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
	if (Objects.equals(resourceUUID, TestResource.TEST_RESOURCE_ID)) {
	    return TEST_RESOURCE;
	}
	return null;
    }

    @Override
    public List<TestResource> list(long pageNumber, long pageSize) {
	return Arrays.asList(TEST_RESOURCE, TestResource.random());
    }
}
