package simplyrestful.api.framework.test;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;
import simplyrestful.api.framework.test.implementation.TestResource;
import simplyrestful.api.framework.test.implementation.TestWebResource;
import simplyrestful.api.framework.webresource.api.implementation.DefaultResourceGet;

@ExtendWith(MockitoExtension.class)
public class DefaultWebResourceTest {
	public static final URI TEST_BASE_URI = URI.create("local://testhost/");
	public static final URI TEST_REQUEST_URI = TEST_BASE_URI.resolve("testresources");
	public static TestResource testInstance;

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
    private UriInfo uriInfo;
    @Mock
    private HttpHeaders httpHeaders;
    @InjectMocks
    public TestWebResource testEndpoint;

    @Test
    public void endpoint_shouldThrowNotFoundExceptionWhenResourceDoesNotExist_withGETonResource() {
        Assertions.assertThrows(NotFoundException.class,
                () -> testEndpoint.getAPIResource(UUID.randomUUID(), List.of(DefaultResourceGet.QUERY_PARAM_FIELDS_DEFAULT)));
    }

    @Test
    public void endpoint_shouldThrowClientErrorExceptionWhenResourceAlreadyExists_withPOSTonResource() {
        Assertions.assertThrows(ClientErrorException.class,
                () -> testEndpoint.postAPIResource(uriInfo, TestResource.testInstance(TEST_BASE_URI)));
    }

    @Test
    public void endpoint_shouldThrowBadRequestWhenIDDoesNotMatchResource_withPUTonResource() {
    	Assertions.assertThrows(BadRequestException.class, () -> testEndpoint.putAPIResource(uriInfo,
                TestResource.TEST_RESOURCE_ID, TestResource.custom(TEST_BASE_URI, UUID.randomUUID())));
    }

    @Test
    public void endpoint_shouldRemoveResource_withDELETEonResource() {
        Response deleteResponse = testEndpoint.deleteAPIResource(TestResource.TEST_RESOURCE_ID);
        Assertions.assertEquals(Status.NO_CONTENT.getStatusCode(), deleteResponse.getStatus());
    }

    @Test
    public void endpoint_shouldThrowNotFoundWhenResourceNonexisting_withDELETEonResource() {
        Assertions.assertThrows(NotFoundException.class, () -> testEndpoint.deleteAPIResource(UUID.randomUUID()));
    }
}
