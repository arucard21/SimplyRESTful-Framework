package simplyrestful.api.framework.test.implementation;

import java.net.URI;
import java.util.UUID;

import io.openapitools.jackson.dataformat.hal.HALLink;
import simplyrestful.api.framework.core.DefaultWebResourceTest;
import simplyrestful.api.framework.core.MediaType;
import simplyrestful.api.framework.resources.HALResource;

public class TestResource extends HALResource{
	public static final String PROFILE_STRING = "local://docs/resources/testresource";
	public static final URI TEST_RESOURCE_PROFILE_URI = URI.create(PROFILE_STRING);
	public static final UUID TEST_RESOURCE_ID = UUID.randomUUID();
	public static final URI TEST_RESOURCE_URI = DefaultWebResourceTest.TEST_REQUEST_URI.resolve(TEST_RESOURCE_ID.toString());
	
	public TestResource() {
		super();
		this.setSelf(new HALLink.Builder(TEST_RESOURCE_URI)
				.type(MediaType.APPLICATION_HAL_JSON)
				.profile(TEST_RESOURCE_PROFILE_URI)
				.build());
	}
	
	@Override
	public URI getProfile() {
		return TEST_RESOURCE_PROFILE_URI;
	}
}
