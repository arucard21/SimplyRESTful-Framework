package simplyrestful.api.framework.client.test.implementation;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.UriBuilder;

import io.swagger.annotations.Api;
import simplyrestful.api.framework.core.AdditionalMediaTypes;
import simplyrestful.api.framework.core.DefaultWebResource;

@Path(TestWebResource.WEBRESOURCE_PATH)
@Api("Test Resources")
@Produces(AdditionalMediaTypes.APPLICATION_HAL_JSON + "; profile=" + TestWebResource.TEST_HOST_STRING + "/"+TestResource.TEST_RESOURCE_PROFILE_PATH)
@Consumes(AdditionalMediaTypes.APPLICATION_HAL_JSON + "; profile=" + TestWebResource.TEST_HOST_STRING + "/"+TestResource.TEST_RESOURCE_PROFILE_PATH)
public class TestWebResource extends DefaultWebResource<TestResource> {
    public static final String TEST_HOST_STRING = "http://localhost:9999";
    public static final URI TEST_HOST = URI.create(TEST_HOST_STRING);
    public static final String WEBRESOURCE_PATH = "testresources";
    public static final UUID ERROR_READ_RESOURCE_ID = UUID.randomUUID();
    public static final UUID ERROR_UPDATE_RESOURCE_ID = UUID.randomUUID();
    public static final URI TEST_REQUEST_BASE_URI = UriBuilder.fromUri(TEST_HOST).path(WEBRESOURCE_PATH).build();
    public static final TestResource TEST_RESOURCE = TestResource.testInstance();
    public static final TestResource TEST_RESOURCE_RANDOM = TestResource.random();

    @Override
    public TestResource create(TestResource resource, UUID resourceUUID) {
	// The provided resource is not actually stored anywhere in this test API.
	return resource;
    }

    @Override
    public TestResource read(UUID resourceUUID) {
	if (Objects.equals(resourceUUID, TestResource.TEST_RESOURCE_ID)) {
	    return TEST_RESOURCE;
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
    public List<TestResource> list(int pageStart, int pageSize, List<String> fields, String query, Map<String, String> sort) {
	return Arrays.asList(TEST_RESOURCE, TEST_RESOURCE_RANDOM);
    }

    @Override
    public int count(String query) {
	return 2;
    }
}
