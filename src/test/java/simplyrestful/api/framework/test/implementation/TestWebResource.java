package simplyrestful.api.framework.test.implementation;

import java.net.URI;

import simplyrestful.api.framework.core.HALResourceAccess;
import simplyrestful.api.framework.core.WebResourceBase;

public class TestWebResource extends WebResourceBase<TestResource>{
	public static final URI TEST_REQUEST_URI = URI.create("local://resources/testresources");

	@Override
	protected URI getAbsoluteResourceURI(Class<?> resourceEndpoint, String id) {
		if (TestResource.TEST_RESOURCE_ID.equals(id)) {
			return TestResource.TEST_RESOURCE_URI;
		}
		return null;
	}

	public TestWebResource(HALResourceAccess<TestResource> resourceAccess) {
		super(resourceAccess);
	}	
}
