package simplyrestful.api.framework.test.implementation;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import io.openapitools.jackson.dataformat.hal.HALLink;
import simplyrestful.api.framework.core.MediaTypeUtils;
import simplyrestful.api.framework.resources.HALResource;

public class TestResource extends HALResource {
    public static final String PROFILE_STRING = "local://docs/resources/testresource/v1";
    public static final String MEDIA_TYPE_HAL_JSON = "application/hal+json;profile=\"" + PROFILE_STRING + "\"";
    public static final String MEDIA_TYPE_JSON = "application/x.testresource-v1+json";
    public static final String TEST_REQUEST_URI = "testresources/";
    public static final UUID TEST_RESOURCE_ID = UUID.fromString("bb2adabf-effe-4fb4-900b-d3b32cd9eed3");
    public static final URI TEST_RESOURCE_PROFILE_URI = URI.create(PROFILE_STRING);

    private TestResource(URI resourceUri) {
	super();
	this.setSelf(new HALLink.Builder(resourceUri)
		.type(MediaTypeUtils.APPLICATION_HAL_JSON)
		.profile(TEST_RESOURCE_PROFILE_URI).build());
    }

    public TestResource() {
    }

    public static TestResource testInstance(URI baseUri) {
	return new TestResource(baseUri.resolve(TEST_REQUEST_URI).resolve(TEST_RESOURCE_ID.toString()));
    }

    public static TestResource random(URI baseUri) {
	return new TestResource(baseUri.resolve(TEST_REQUEST_URI).resolve(UUID.randomUUID().toString()));
    }

    public static TestResource custom(URI baseUri, UUID uuid) {
	return new TestResource(baseUri.resolve(TEST_REQUEST_URI).resolve(uuid.toString()));
    }

    @Override
    public URI getProfile() {
	return TEST_RESOURCE_PROFILE_URI;
    }

    @Override
    public MediaType getCustomJsonMediaType() {
	return MediaType.valueOf(MEDIA_TYPE_JSON);
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
