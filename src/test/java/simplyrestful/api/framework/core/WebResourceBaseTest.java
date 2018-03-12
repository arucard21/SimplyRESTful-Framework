package simplyrestful.api.framework.core;

import java.net.URI;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import io.openapitools.jackson.dataformat.hal.HALLink;
import simplyrestful.api.framework.core.exceptions.InvalidResourceException;
import simplyrestful.api.framework.core.exceptions.InvalidSelfLinkException;
import simplyrestful.api.framework.core.hal.HALResource;

@RunWith(MockitoJUnitRunner.class)
public class WebResourceBaseTest{
	private static final String TEST_RESOURCE_PROFILE = "local://docs/resources/testresource";
	private static final String TEST_REQUEST_URI = "local://resources/testresources";
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	@Mock
	private HALResource testResource;
	@Mock
	HALResourceAccess<HALResource> mockDAO;
	@Spy
	WebResourceBase<HALResource> testEndpoint;
	@Mock
	private HALResource existingResource;
	private HALLink testLink;
	private String resourceID = "123456789";
	private String absoluteResourceURI = TEST_REQUEST_URI+"/"+resourceID;

	@Before
	public void configureMocks(){
		Mockito.doReturn(URI.create(absoluteResourceURI)).when(testEndpoint).getAbsoluteResourceURI(ArgumentMatchers.any(Class.class), ArgumentMatchers.eq(resourceID));
		Mockito.doReturn(mockDAO).when(testEndpoint).getDataAccessObject();

		testLink = new HALLink.Builder(absoluteResourceURI)
				.type(MediaType.APPLICATION_HAL_JSON)
				.profile(URI.create(TEST_RESOURCE_PROFILE))
				.build();
		Mockito.when(testResource.getSelf()).thenReturn(testLink);
	}

	@Test
	public void endpoint_shouldCreateLinkWithCorrectMediaType(){
		Assert.assertEquals(
				MediaType.APPLICATION_HAL_JSON,
				testEndpoint.createLink(URI.create(TEST_REQUEST_URI), URI.create(TEST_RESOURCE_PROFILE)).getType());
	}

	@Test
	public void endpoint_shouldCreateLinkWithCorrectProfile(){
		Assert.assertEquals(
				URI.create(TEST_RESOURCE_PROFILE),
				testEndpoint.createLink(URI.create(TEST_REQUEST_URI), URI.create(TEST_RESOURCE_PROFILE)).getProfile());
	}

	@Test
	public void endpoint_shouldCreateLinkWithCorrectRequestURI(){
		Assert.assertEquals(
				TEST_REQUEST_URI,
				testEndpoint.createLink(URI.create(TEST_REQUEST_URI), URI.create(TEST_RESOURCE_PROFILE)).getHref());
	}

	@Test
	public void endpoint_shouldRetrieveResourceCollection_withGETonRoot(){
		int page = 0;
		int pageSize = 100;
		boolean compact = true;
		testEndpoint.getHALResources(page, pageSize, compact);
		Mockito.verify(mockDAO).retrieveResourcesFromDataStore(page, pageSize, compact);
	}

	@Test
	public void endpoint_shouldRetrieveResource_withGETonResource(){
		Mockito.when(mockDAO.retrieveResourceFromDataStore(absoluteResourceURI)).thenReturn(testResource);
		testEndpoint.getHALResource(resourceID);
		Mockito.verify(mockDAO).retrieveResourceFromDataStore(absoluteResourceURI);
	}

	@Test
	public void endpoint_shouldThrowNotFoundExceptionWhenResourceDoesNotExist_withGETonResource(){
		thrown.expect(NotFoundException.class);
		Mockito.when(mockDAO.retrieveResourceFromDataStore(absoluteResourceURI)).thenReturn(null);
		testEndpoint.getHALResource(resourceID);
	}

	@Test
	public void endpoint_shouldCreateResource_withPOSTonResource(){
		Mockito.when(mockDAO.exists(absoluteResourceURI)).thenReturn(false);
		Response postResponse = testEndpoint.postHALResource(testResource);
		Mockito.verify(mockDAO).addResourceToDataStore(testResource);
		Assert.assertEquals(Status.CREATED.getStatusCode(), postResponse.getStatus());
		Assert.assertEquals(URI.create(absoluteResourceURI), postResponse.getLocation());
	}

	@Test
	public void endpoint_shouldThrowClientErrorExceptionWhenResourceAlreadyExists_withPOSTonResource(){
		thrown.expect(ClientErrorException.class);
		Mockito.when(mockDAO.exists(absoluteResourceURI)).thenReturn(true);
		testEndpoint.postHALResource(testResource);
	}

	@Test
	public void endpoint_shouldUpdateResource_withPUTonResource() throws InvalidSelfLinkException, InvalidResourceException{
		Mockito.when(mockDAO.updateResourceInDataStore(testResource)).thenReturn(existingResource);
		testEndpoint.putHALResource(resourceID, testResource);
		Mockito.verify(mockDAO).updateResourceInDataStore(testResource);
	}

	@Test
	public void endpoint_shouldThrowBadRequestWhenIDDoesNotMatchResource_withPUTonResource(){
		thrown.expect(BadRequestException.class);
		testEndpoint.putHALResource("fakeID", testResource);
	}

	@Test
	public void endpoint_shouldUpdateResourceEvenWhenSelfLinkMissing_withPUTonResource() throws InvalidSelfLinkException, InvalidResourceException{
		Mockito.when(testResource.getSelf()).thenReturn(null);
		Mockito.when(mockDAO.updateResourceInDataStore(testResource)).thenReturn(existingResource);
		testEndpoint.putHALResource(resourceID, testResource);
		Mockito.verify(mockDAO).updateResourceInDataStore(testResource);
	}

	@Test
	public void endpoint_shouldThrowBadRequestWhenResourceInvalid_withPUTonResource() throws InvalidSelfLinkException, InvalidResourceException{
		thrown.expect(BadRequestException.class);
		Mockito.when(mockDAO.updateResourceInDataStore(testResource)).thenThrow(InvalidResourceException.class);
		testEndpoint.putHALResource(resourceID, testResource);
	}

	@Test
	public void endpoint_shouldCreateNewResourceWhenNonexisting_withPUTonResource() throws InvalidSelfLinkException, InvalidResourceException{
		Mockito.when(mockDAO.updateResourceInDataStore(testResource)).thenReturn(null);
		Mockito.when(mockDAO.addResourceToDataStore(testResource)).thenReturn(true);
		HALResource previousResource = testEndpoint.putHALResource(resourceID, testResource);
		Mockito.verify(mockDAO).updateResourceInDataStore(testResource);
		Mockito.verify(mockDAO).addResourceToDataStore(testResource);
		Assert.assertEquals(null, previousResource);
	}

	@Test
	public void endpoint_shouldThrowNotFoundWhenNonexistingResourceCanNotBeCreated_withPUTonResource() throws InvalidSelfLinkException, InvalidResourceException{
		thrown.expect(NotFoundException.class);
		Mockito.when(mockDAO.updateResourceInDataStore(testResource)).thenReturn(null);
		Mockito.when(mockDAO.addResourceToDataStore(testResource)).thenReturn(false);
		testEndpoint.putHALResource(resourceID, testResource);
	}

	@Test
	public void endpoint_shouldRemoveResource_withDELETEonResource(){
		Mockito.when(mockDAO.removeResourceFromDataStore(absoluteResourceURI)).thenReturn(testResource);
		Response deleteResponse = testEndpoint.deleteHALResource(resourceID);
		Mockito.verify(testEndpoint).getAbsoluteResourceURI(resourceID);
		Assert.assertEquals(Status.NO_CONTENT.getStatusCode(), deleteResponse.getStatus());
	}

	@Test
	public void endpoint_shouldThrowNotFoundWhenResourceNonexisting_withDELETEonResource(){
		thrown.expect(NotFoundException.class);
		Mockito.when(mockDAO.removeResourceFromDataStore(absoluteResourceURI)).thenReturn(null);
		testEndpoint.deleteHALResource(resourceID);
	}
}
