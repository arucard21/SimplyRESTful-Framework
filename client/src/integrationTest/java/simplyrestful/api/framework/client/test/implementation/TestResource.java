package simplyrestful.api.framework.client.test.implementation;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

import javax.ws.rs.core.UriBuilder;

import io.openapitools.jackson.dataformat.hal.HALLink;
import simplyrestful.api.framework.core.MediaTypeUtils;
import simplyrestful.api.framework.resources.HALResource;

public class TestResource extends HALResource {
    public static final String TEST_RESOURCE_PROFILE = "http://localhost/profiles/testresource/v1";
    public static final URI TEST_RESOURCE_PROFILE_URI = UriBuilder.fromUri(TEST_RESOURCE_PROFILE).build();
    public static final UUID TEST_RESOURCE_ID = UUID.randomUUID();

    public static URI getResourceUri(UUID id) {
	return UriBuilder.fromUri(TestWebResource.getBaseUri()).path(TestWebResource.class).path(id.toString()).build();
    }

    private TestResource(URI resourceUri) {
	this.setSelf(new HALLink.Builder(resourceUri).type(MediaTypeUtils.APPLICATION_HAL_JSON)
		.profile(TEST_RESOURCE_PROFILE_URI).build());
    }

    public TestResource() {}

    public static TestResource testInstance() {
	return new TestResource(TestResource.getResourceUri(TEST_RESOURCE_ID));
    }

    public static TestResource random() {
	return TestResource.withId(UUID.randomUUID());
    }

    public static TestResource withId(UUID resourceId) {
	return new TestResource(getResourceUri(resourceId));
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
