package simplyrestful.api.framework.test.implementation;

import java.net.URI;

import io.openapitools.jackson.dataformat.hal.HALLink;
import simplyrestful.api.framework.core.AbstractWebResourceTest;
import simplyrestful.api.framework.core.MediaType;
import simplyrestful.api.framework.resources.HALResource;

public class TestResource extends HALResource{
	public static final String PROFILE_STRING = "local://docs/resources/testresource";
	public static final URI TEST_RESOURCE_PROFILE_URI = URI.create(PROFILE_STRING);
	public static final String TEST_RESOURCE_ID = "123456789";
	public static final URI TEST_RESOURCE_URI = AbstractWebResourceTest.TEST_REQUEST_URI.resolve(TEST_RESOURCE_ID);
	
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
