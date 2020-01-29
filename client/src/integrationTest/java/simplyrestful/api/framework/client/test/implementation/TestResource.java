package simplyrestful.api.framework.client.test.implementation;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

import javax.ws.rs.core.UriBuilder;

import io.openapitools.jackson.dataformat.hal.HALLink;
import simplyrestful.api.framework.core.AdditionalMediaTypes;
import simplyrestful.api.framework.resources.HALResource;

public class TestResource extends HALResource {
    public static final String TEST_RESOURCE_PROFILE_PATH = "testresource";
    public static final String PROFILE_STRING = UriBuilder.fromUri(TestWebResource.TEST_HOST).path(TEST_RESOURCE_PROFILE_PATH).build().toString();
    public static final UUID TEST_RESOURCE_ID = UUID.fromString("bb2adabf-effe-4fb4-900b-d3b32cd9eed3");
    public static final URI TEST_RESOURCE_URI = UriBuilder.fromUri(TestWebResource.TEST_REQUEST_BASE_URI).path(TEST_RESOURCE_ID.toString()).build();
    public static final URI TEST_RESOURCE_PROFILE_URI = URI.create(PROFILE_STRING);

    private TestResource(URI resourceUri) {
	this.setSelf(new HALLink.Builder(resourceUri).type(AdditionalMediaTypes.APPLICATION_HAL_JSON)
		.profile(TEST_RESOURCE_PROFILE_URI).build());
    }
    
    public TestResource() {}
    
    public static TestResource testInstance() {
	return new TestResource(TEST_RESOURCE_URI);
    }

    public static TestResource random() {
	return TestResource.withId(UUID.randomUUID());
    }
    
    public static TestResource withId(UUID resourceId) {
	return new TestResource(UriBuilder.fromUri(TestWebResource.TEST_REQUEST_BASE_URI).path(resourceId.toString()).build());
    }

    @Override
    public URI getProfile() {
	return TEST_RESOURCE_PROFILE_URI;
    }

    @Override
    public int hashCode() {
	return Objects.hash(getProfile()) + super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof TestResource) {
	    TestResource resource = (TestResource) obj;
	    return super.equals(obj) && Objects.equals(this.getProfile(), resource.getProfile());
	}
	return false;
    }

    @Override
    public boolean canEqual(Object obj) {
	return (obj instanceof TestResource);
    }
}
