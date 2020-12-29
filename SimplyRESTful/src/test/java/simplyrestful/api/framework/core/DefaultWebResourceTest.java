package simplyrestful.api.framework.core;

import java.net.URI;
import java.util.UUID;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import simplyrestful.api.framework.test.implementation.TestResource;
import simplyrestful.api.framework.test.implementation.TestWebResource;

@ExtendWith(MockitoExtension.class)
public class DefaultWebResourceTest {
    private static final URI TEST_BASE_URI = URI.create("local://testhost/");
    private static final URI TEST_REQUEST_URI = TEST_BASE_URI.resolve("testresources");
    private static TestResource testInstance;

    @BeforeAll
    public static void addTestResources() {
	testInstance = TestResource.testInstance(TEST_BASE_URI);
	TestWebResource.TEST_RESOURCES.add(testInstance);
	TestWebResource.TEST_RESOURCES.add(TestResource.random(TEST_BASE_URI));
    }

    @AfterAll
    public static void clearTestResources() {
	TestWebResource.TEST_RESOURCES.clear();
    }

    @Mock
    private UriInfo uriInfo;
    @InjectMocks
    public TestWebResource testEndpoint;

    @Test
    public void endpoint_shouldCreateLinkWithCorrectMediaType() {
	Assertions.assertEquals(MediaTypeUtils.APPLICATION_HAL_JSON,
		testEndpoint.createLink(TEST_REQUEST_URI, TestResource.TEST_RESOURCE_PROFILE_URI).getType());
    }

    @Test
    public void endpoint_shouldCreateLinkWithCorrectProfile() {
	Assertions.assertEquals(TestResource.TEST_RESOURCE_PROFILE_URI,
		testEndpoint.createLink(TEST_REQUEST_URI, TestResource.TEST_RESOURCE_PROFILE_URI).getProfile());
    }

    @Test
    public void endpoint_shouldCreateLinkWithCorrectRequestURI() {
	Assertions.assertEquals(TEST_REQUEST_URI, URI
		.create(testEndpoint.createLink(TEST_REQUEST_URI, TestResource.TEST_RESOURCE_PROFILE_URI).getHref()));
    }

    @Test
    public void endpoint_shouldThrowNotFoundExceptionWhenResourceDoesNotExist_withGETonResource() {
	Assertions.assertThrows(NotFoundException.class, () -> testEndpoint.getHALResource(UUID.randomUUID()));
    }

    @Test
    public void endpoint_shouldThrowClientErrorExceptionWhenResourceAlreadyExists_withPOSTonResource() {
	Mockito.when(uriInfo.getBaseUriBuilder()).thenReturn(UriBuilder.fromUri(TEST_BASE_URI));
	Assertions.assertThrows(ClientErrorException.class,
		() -> testEndpoint.postHALResource(TestResource.testInstance(TEST_BASE_URI)));
    }

    @Test
    public void endpoint_shouldThrowBadRequestWhenIDDoesNotMatchResource_withPUTonResource() {
	Mockito.when(uriInfo.getBaseUriBuilder()).thenReturn(UriBuilder.fromUri(TEST_BASE_URI));
	Assertions.assertThrows(BadRequestException.class,
		() -> testEndpoint.putHALResource(UUID.randomUUID(), TestResource.testInstance(TEST_BASE_URI)));
    }

    @Test
    public void endpoint_shouldRemoveResource_withDELETEonResource() {
	Response deleteResponse = testEndpoint.deleteHALResource(TestResource.TEST_RESOURCE_ID);
	Assertions.assertEquals(Status.NO_CONTENT.getStatusCode(), deleteResponse.getStatus());
    }

    @Test
    public void endpoint_shouldThrowNotFoundWhenResourceNonexisting_withDELETEonResource() {
	Assertions.assertThrows(NotFoundException.class, () -> testEndpoint.deleteHALResource(UUID.randomUUID()));
    }
}
