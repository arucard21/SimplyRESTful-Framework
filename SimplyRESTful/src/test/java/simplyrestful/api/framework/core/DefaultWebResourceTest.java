package simplyrestful.api.framework.core;

import java.net.URI;
import java.util.UUID;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import simplyrestful.api.framework.test.implementation.TestResource;
import simplyrestful.api.framework.test.implementation.TestWebResource;

@ExtendWith(MockitoExtension.class)
public class DefaultWebResourceTest{
	public static final URI TEST_REQUEST_BASE_URI = URI.create("local://resources/");
	public static final URI TEST_REQUEST_URI = URI.create("local://resources/testresources/");
	private TestResource testResource = new TestResource();
	@Mock
	private ResourceDAO<TestResource> mockDAO;
	@Mock
	private UriInfo uriInfo;
	@InjectMocks
	public TestWebResource testEndpoint;

	@Test
	public void endpoint_shouldCreateLinkWithCorrectMediaType(){
		Assertions.assertEquals(
				MediaType.APPLICATION_HAL_JSON,
				testEndpoint.createLink(TEST_REQUEST_URI, TestResource.TEST_RESOURCE_PROFILE_URI).getType());
	}

	@Test
	public void endpoint_shouldCreateLinkWithCorrectProfile(){
		Assertions.assertEquals(
				TestResource.TEST_RESOURCE_PROFILE_URI,
				testEndpoint.createLink(TEST_REQUEST_URI, TestResource.TEST_RESOURCE_PROFILE_URI).getProfile());
	}

	@Test
	public void endpoint_shouldCreateLinkWithCorrectRequestURI(){
		Assertions.assertEquals(
				TEST_REQUEST_URI,
				URI.create(testEndpoint.createLink(TEST_REQUEST_URI, TestResource.TEST_RESOURCE_PROFILE_URI).getHref()));
	}

	@Test
	public void endpoint_shouldRetrieveResourceCollection_withGETonRoot(){
		int page = 0;
		int pageSize = 100;
		boolean compact = true;
		testEndpoint.getHALResources(page, pageSize, compact);
		Mockito.verify(mockDAO).findAllForPage(page, pageSize, TEST_REQUEST_URI);
	}

	@Test
	public void endpoint_shouldRetrieveResource_withGETonResource(){
		Mockito.when(mockDAO.findByURI(TestResource.TEST_RESOURCE_URI, TEST_REQUEST_URI)).thenReturn(testResource);
		testEndpoint.getHALResource(TestResource.TEST_RESOURCE_ID);
		Mockito.verify(mockDAO).findByURI(TestResource.TEST_RESOURCE_URI, TEST_REQUEST_URI);
	}

	@Test
	public void endpoint_shouldThrowNotFoundExceptionWhenResourceDoesNotExist_withGETonResource(){
		Mockito.when(mockDAO.findByURI(TestResource.TEST_RESOURCE_URI, TEST_REQUEST_URI)).thenReturn(null);
		Assertions.assertThrows(NotFoundException.class, 
				() -> testEndpoint.getHALResource(TestResource.TEST_RESOURCE_ID));
	}

	@Test
	public void endpoint_shouldCreateResource_withPOSTonResource(){
		Mockito.when(mockDAO.findByURI(TestResource.TEST_RESOURCE_URI, TEST_REQUEST_URI)).thenReturn(null);
		Mockito.when(mockDAO.persist(ArgumentMatchers.any(TestResource.class), ArgumentMatchers.any(URI.class))).thenReturn(testResource);
		Response postResponse = testEndpoint.postHALResource(testResource);
		Mockito.verify(mockDAO).persist(ArgumentMatchers.eq(testResource), ArgumentMatchers.any());
		Assertions.assertEquals(Status.CREATED.getStatusCode(), postResponse.getStatus());
		Assertions.assertEquals(TestResource.TEST_RESOURCE_URI, postResponse.getLocation());
	}

	@Test
	public void endpoint_shouldThrowClientErrorExceptionWhenResourceAlreadyExists_withPOSTonResource(){
		Mockito.when(mockDAO.findByURI(TestResource.TEST_RESOURCE_URI, TEST_REQUEST_URI)).thenReturn(testResource);
		Assertions.assertThrows(ClientErrorException.class, 
				() -> testEndpoint.postHALResource(testResource));
	}

	@Test
	public void endpoint_shouldUpdateResource_withPUTonResource() {
		Mockito.when(mockDAO.persist(ArgumentMatchers.eq(testResource), ArgumentMatchers.any(URI.class))).thenReturn(new TestResource());
		testEndpoint.putHALResource(TestResource.TEST_RESOURCE_ID, testResource);
		Mockito.verify(mockDAO).persist(ArgumentMatchers.eq(testResource), ArgumentMatchers.any());
	}

	@Test
	public void endpoint_shouldThrowBadRequestWhenIDDoesNotMatchResource_withPUTonResource(){
		Assertions.assertThrows(BadRequestException.class, 
				() -> testEndpoint.putHALResource(UUID.randomUUID(), testResource));
	}

	@Test
	public void endpoint_shouldUpdateResourceEvenWhenSelfLinkMissing_withPUTonResource() {
		Mockito.when(mockDAO.persist(ArgumentMatchers.eq(testResource), ArgumentMatchers.any())).thenReturn(new TestResource());
		testEndpoint.putHALResource(TestResource.TEST_RESOURCE_ID, testResource);
		Mockito.verify(mockDAO).persist(ArgumentMatchers.eq(testResource), ArgumentMatchers.any());
	}

	@Test
	public void endpoint_shouldThrowBadRequestWhenResourceInvalid_withPUTonResource() {
		Mockito.when(mockDAO.persist(ArgumentMatchers.eq(testResource), ArgumentMatchers.any())).thenThrow(BadRequestException.class);
		Assertions.assertThrows(BadRequestException.class, 
				() -> testEndpoint.putHALResource(TestResource.TEST_RESOURCE_ID, testResource));
	}

	@Test
	public void endpoint_shouldCreateNewResourceWhenNonexisting_withPUTonResource() {
		Mockito.when(mockDAO.persist(ArgumentMatchers.eq(testResource), ArgumentMatchers.any())).thenReturn(null);
		Response response = testEndpoint.putHALResource(TestResource.TEST_RESOURCE_ID, testResource);
		Mockito.verify(mockDAO).persist(ArgumentMatchers.eq(testResource), ArgumentMatchers.any());
		Assertions.assertEquals(201, response.getStatus());
	}

	@Test
	public void endpoint_shouldRemoveResource_withDELETEonResource(){
		Mockito.when(mockDAO.remove(TestResource.TEST_RESOURCE_URI, TEST_REQUEST_URI)).thenReturn(testResource);
		Response deleteResponse = testEndpoint.deleteHALResource(TestResource.TEST_RESOURCE_ID);
		Assertions.assertEquals(Status.NO_CONTENT.getStatusCode(), deleteResponse.getStatus());
	}

	@Test
	public void endpoint_shouldThrowNotFoundWhenResourceNonexisting_withDELETEonResource(){
		Mockito.when(mockDAO.remove(TestResource.TEST_RESOURCE_URI, TEST_REQUEST_URI)).thenReturn(null);
		Assertions.assertThrows(NotFoundException.class, 
				() -> testEndpoint.deleteHALResource(TestResource.TEST_RESOURCE_ID));
	}
}
