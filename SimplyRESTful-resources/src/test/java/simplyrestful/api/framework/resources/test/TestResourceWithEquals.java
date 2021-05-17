package simplyrestful.api.framework.resources.test;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import simplyrestful.api.framework.resources.HALResource;

public class TestResourceWithEquals extends HALResource {
    public static final String PROFILE_STRING = "local://docs/resources/testresource";
    public static final UUID TEST_RESOURCE_ID = UUID.fromString("bb2adabf-effe-4fb4-900b-d3b32cd9eed3");
    public static final URI TEST_RESOURCE_PROFILE_URI = URI.create(PROFILE_STRING);
    public static final URI TEST_REQUEST_URI = URI.create("local://resources/testresources/");
    public static final URI TEST_RESOURCE_URI = TEST_REQUEST_URI.resolve(TEST_RESOURCE_ID.toString());

    private TestResourceWithEquals(URI resourceUri) {
	super();
    }

    public TestResourceWithEquals() {
	this(TEST_RESOURCE_URI);
    }

    @Override
    public URI getProfile() {
	return TEST_RESOURCE_PROFILE_URI;
    }

    @Override
    public MediaType getCustomJsonMediaType() {
	return new MediaType("application", "x.testresource-v1+json");
    }

    @Override
    public int hashCode() {
	return Objects.hash(getProfile()) + super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof TestResourceWithEquals) {
	    TestResourceWithEquals resource = (TestResourceWithEquals) obj;
	    return super.equals(obj) && Objects.equals(this.getProfile(), resource.getProfile());
	}
	return false;
    }

    @Override
    public boolean canEqual(Object obj) {
	return (obj instanceof TestResourceWithEquals);
    }
}
