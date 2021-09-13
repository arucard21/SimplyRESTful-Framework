package simplyrestful.api.framework.test;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
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

import simplyrestful.api.framework.MediaTypeUtils;
import simplyrestful.api.framework.WebResourceUtils;
import simplyrestful.api.framework.test.implementation.TestResource;
import simplyrestful.api.framework.test.implementation.TestWebResource;
import simplyrestful.api.framework.webresource.api.ResourceGet;

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
    private ContainerRequestContext requestContext;
    @Mock
    private ResourceInfo resourceInfo;
    @Mock
    private UriInfo uriInfo;
    @Mock
    private HttpHeaders httpHeaders;
    @InjectMocks
    public TestWebResource testEndpoint;

    @Test
    public void endpoint_shouldCreateLinkWithCorrectMediaType() {
        Assertions.assertEquals(MediaTypeUtils.APPLICATION_HAL_JSON, WebResourceUtils.createLink(TEST_REQUEST_URI,
                MediaTypeUtils.APPLICATION_HAL_JSON, TestResource.TEST_RESOURCE_PROFILE_URI).getType());
    }

    @Test
    public void endpoint_shouldCreateLinkWithCorrectProfile() {
        Assertions.assertEquals(TestResource.TEST_RESOURCE_PROFILE_URI, WebResourceUtils.createLink(TEST_REQUEST_URI,
                MediaTypeUtils.APPLICATION_HAL_JSON, TestResource.TEST_RESOURCE_PROFILE_URI).getProfile());
    }

    @Test
    public void endpoint_shouldCreateLinkWithCorrectRequestURI() {
        Assertions.assertEquals(TEST_REQUEST_URI, URI.create(WebResourceUtils.createLink(TEST_REQUEST_URI,
                MediaTypeUtils.APPLICATION_HAL_JSON, TestResource.TEST_RESOURCE_PROFILE_URI).getHref()));
    }

    @Test
    public void endpoint_shouldThrowNotFoundExceptionWhenResourceDoesNotExist_withGETonResource() {
    	Mockito.when(requestContext.getUriInfo()).thenReturn(uriInfo);
    	Mockito.when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());
        Assertions.assertThrows(NotFoundException.class,
                () -> testEndpoint.getHALResource(requestContext, resourceInfo, uriInfo, httpHeaders, UUID.randomUUID(), List.of(ResourceGet.QUERY_PARAM_FIELDS_DEFAULT)));
    }

    @Test
    public void endpoint_shouldThrowClientErrorExceptionWhenResourceAlreadyExists_withPOSTonResource() {
        Mockito.doReturn(TestWebResource.class).when(resourceInfo).getResourceClass();
        Mockito.when(uriInfo.getBaseUriBuilder()).thenReturn(UriBuilder.fromUri(TEST_BASE_URI));
        Assertions.assertThrows(ClientErrorException.class,
                () -> testEndpoint.postHALResource(resourceInfo, uriInfo, TestResource.testInstance(TEST_BASE_URI)));
    }

    @Test
    public void endpoint_shouldThrowBadRequestWhenIDDoesNotMatchResource_withPUTonResource() {
        Mockito.doReturn(TestWebResource.class).when(resourceInfo).getResourceClass();
        Mockito.when(uriInfo.getBaseUriBuilder()).thenReturn(UriBuilder.fromUri(TEST_BASE_URI));
        Assertions.assertThrows(BadRequestException.class, () -> testEndpoint.putHALResource(resourceInfo, uriInfo,
                TestResource.TEST_RESOURCE_ID, TestResource.custom(TEST_BASE_URI, UUID.randomUUID())));
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
