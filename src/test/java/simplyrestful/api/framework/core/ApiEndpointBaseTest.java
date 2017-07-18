package simplyrestful.api.framework.core;

import java.net.URI;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import dk.nykredit.jackson.dataformat.hal.HALLink;
import simplyrestful.api.framework.core.hal.HALResource;

@RunWith(MockitoJUnitRunner.class)
public class ApiEndpointBaseTest{
	private static final String TEST_RESOURCE_PROFILE = "local://docs/resources/testresource";
	private static final String TEST_REQUEST_URI = "local://resources/testresources";
	@Mock
	ApiEndpointBase<HALResource> testEndpoint;
	private HALLink testLink;

	@Before
	public void configureMocks(){
		Mockito.when(testEndpoint.createLink(ArgumentMatchers.any(), ArgumentMatchers.any())).thenCallRealMethod();
		testLink = testEndpoint.createLink(URI.create(TEST_REQUEST_URI), URI.create(TEST_RESOURCE_PROFILE));
	}

	@Test
	public void test_createLink_UsesCorrectMediaType(){
		Assert.assertEquals(MediaType.APPLICATION_HAL_JSON, testLink.getType());
	}

	@Test
	public void test_createLink_UsesCorrectProfile(){
		Assert.assertEquals(URI.create(TEST_RESOURCE_PROFILE), testLink.getProfile());
	}

	@Test
	public void test_createLink_UsesCorrectRequestURI(){
		Assert.assertEquals(TEST_REQUEST_URI, testLink.getHref());
	}
}
