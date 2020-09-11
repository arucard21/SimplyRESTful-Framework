package simplyrestful.api.framework.client;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.UriBuilder;

import org.apache.cxf.endpoint.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.openapitools.jackson.dataformat.hal.HALLink;
import simplyrestful.api.framework.client.test.implementation.TestResource;
import simplyrestful.api.framework.client.test.implementation.TestWebResource;
import simplyrestful.jetty.deploy.ServerBuilder;

@ExtendWith(MockitoExtension.class)
public class SimplyRESTfulClientTest {
	private static final UUID UUID_NIL = UUID.fromString("00000000-0000-0000-0000-000000000000");
	private static final URI INVALID_RESOURCE_URI_DIFFERENT_HOST = URI.create("http://invalid-host/testresources/" + UUID_NIL.toString());
	private static final URI INVALID_RESOURCE_URI_DIFFERENT_PATH = URI.create(TestWebResource.TEST_HOST_STRING + "/different/path/testresources/" + UUID_NIL.toString());
	private static Server server;
	private static SimplyRESTfulClient<TestResource> client;

	@BeforeAll
	private static void initializeServerAndClient() {
		server = Assertions.assertDoesNotThrow(
				() -> new ServerBuilder().withAddress(TestWebResource.TEST_HOST.toString()).withWebResource(TestWebResource.class).build(),
				"The test server could not be started");
		Assertions.assertNotNull(server, "The test server failed to start correctly");
		client = Assertions.assertDoesNotThrow(() -> new SimplyRESTfulClientFactory<TestResource>(ClientBuilder.newClient()).newClient(TestWebResource.TEST_HOST, TestResource.class));
		Assertions.assertNotNull(client, "The SimplyRESTful client could not be created correctly");
	}

	@AfterAll
	private static void removeServer() {
		server.stop();
		server.destroy();
	}

	@Test
	public void client_shouldDiscoverTheResourceURI_whenItIsCreated() {
		Assertions.assertDoesNotThrow(() -> client.listResources(-1, -1, "", "", ""));
	}

	@Test
	public void listResources_shouldReturnTestResources() {
		List<TestResource> listOfResourceIdentifiers = client.listResources(-1, -1, "", "", "");
		Assertions.assertEquals(2, listOfResourceIdentifiers.size());
		Assertions.assertTrue(listOfResourceIdentifiers.contains(TestWebResource.TEST_RESOURCE));
	}

	@Test
	public void read_shouldReturnTestResource() {
		TestResource actual = client.read(TestResource.TEST_RESOURCE_ID);
		Assertions.assertEquals(TestWebResource.TEST_RESOURCE, actual);
	}

	@Test
	public void read_shouldReturnTestResource_whenAnNonExistingIdIsUsed() {
		Assertions.assertThrows(NotFoundException.class, () -> client.read(UUID_NIL));
	}

	@Test
	public void read_shouldThrowNullPointerException_whenProvidingNullAsArgument() {
		Assertions.assertThrows(NullPointerException.class, () -> client.read(null));
	}

	@Test
	public void create_shouldReturnTheIdOfTheCreatedTestResource() {
		UUID actual = client.create(new TestResource());
		Assertions.assertNotEquals(TestResource.TEST_RESOURCE_ID, actual);
	}

	@Test
	public void create_shouldThrowIllegalArgumentException_whenTheProvidedResourceRefersToAnExistingId() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> client.create(TestWebResource.TEST_RESOURCE));
	}

	@Test
	public void create_shouldThrowWebApplicationException_whenTheExistsCheckFails() {
		Assertions.assertThrows(WebApplicationException.class,
				() -> client.create(TestResource.withId(TestWebResource.ERROR_READ_RESOURCE_ID)));
	}

	@Test
	public void create_shouldReturnTheSameId_whenTheProvidedTestResourceContainsAnId() {
		UUID actual = client.create(TestResource.withId(UUID_NIL));
		Assertions.assertEquals(UUID_NIL, actual);
	}

	@Test
	public void create_shouldThrowNullPointerException_whenProvidingNullAsArgument() {
		Assertions.assertThrows(NullPointerException.class, () -> client.create(null));
	}

	@Test
	public void create_shouldThrowIllegalArgumentException_whenResourceContainsAResourceUriWithDifferentHostname() {
		TestResource invalidResource = new TestResource();
		invalidResource.setSelf(new HALLink.Builder(INVALID_RESOURCE_URI_DIFFERENT_HOST).build());
		Assertions.assertThrows(IllegalArgumentException.class, () -> client.create(invalidResource));
	}

	@Test
	public void create_shouldThrowIllegalArgumentException_whenResourceContainsAResourceUriWithDifferentPath() {
		TestResource invalidResource = new TestResource();
		invalidResource.setSelf(new HALLink.Builder(INVALID_RESOURCE_URI_DIFFERENT_PATH).build());
		Assertions.assertThrows(IllegalArgumentException.class, () -> client.create(invalidResource));
	}

	@Test
	public void exists_shouldReturnTrue_whenProvidingAnExistingIdAsArgument() {
		Assertions.assertTrue(client.exists(TestResource.TEST_RESOURCE_ID));
	}

	@Test
	public void exists_shouldReturnFalse_whenProvidingAnNonExistingIdAsArgument() {
		Assertions.assertFalse(client.exists(UUID_NIL));
	}

	@Test
	public void exists_shouldReturnTrue_whenProvidingAnExistingResourceUriAsArgument() {
		Assertions.assertTrue(client.exists(TestResource.TEST_RESOURCE_URI));
	}

	@Test
	public void exists_shouldReturnFalse_whenProvidingAnNonExistingResourceUriAsArgument() {
		Assertions.assertFalse(client
				.exists(UriBuilder.fromUri(TestWebResource.TEST_REQUEST_BASE_URI).path(UUID_NIL.toString()).build()));
	}

	@Test
	public void update_shouldReturnWithoutExceptions_whenTheProvidedResourceRefersToAnExistingId() {
		Assertions.assertDoesNotThrow(() -> client.update(TestWebResource.TEST_RESOURCE));
	}

	@Test
	public void update_shouldThrowIllegalArgumentException_whenTheProvidedResourceRefersToANonExistingId() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> client.update(TestResource.withId(UUID_NIL)));
	}

	@Test
	public void update_shouldThrowWebApplicationException_whenTheServerReturnsAnErrorResponse() {
		Assertions.assertThrows(IllegalArgumentException.class,
				() -> client.update(TestResource.withId(TestWebResource.ERROR_UPDATE_RESOURCE_ID)));
	}

	@Test
	public void update_shouldThrowNullPointerException_whenNullIsProvidedAsArgument() {
		Assertions.assertThrows(NullPointerException.class, () -> client.update(null));
	}

	@Test
	public void update_shouldThrowIllegalArgumentException_whenResourceContainsAResourceUriWithDifferentHostname() {
		TestResource invalidResource = new TestResource();
		invalidResource.setSelf(new HALLink.Builder(INVALID_RESOURCE_URI_DIFFERENT_HOST).build());
		Assertions.assertThrows(IllegalArgumentException.class, () -> client.update(invalidResource));
	}

	@Test
	public void update_shouldThrowIllegalArgumentException_whenResourceContainsAResourceUriWithDifferentPath() {
		TestResource invalidResource = new TestResource();
		invalidResource.setSelf(new HALLink.Builder(INVALID_RESOURCE_URI_DIFFERENT_PATH).build());
		Assertions.assertThrows(IllegalArgumentException.class, () -> client.update(invalidResource));
	}

	@Test
	public void delete_shouldReturnWithoutExceptions_whenAnExistingIdIsProvided() {
		Assertions.assertDoesNotThrow(() -> client.delete(TestResource.TEST_RESOURCE_ID));
	}

	@Test
	public void delete_shouldThrowIllegalArgumentException_whenANonExistingIdIsProvided() {
		Assertions.assertThrows(WebApplicationException.class, () -> client.delete(UUID_NIL));
	}

	@Test
	public void delete_shouldThrowNullPointerException_whenNullIsProvidedAsArgument() {
		Assertions.assertThrows(NullPointerException.class, () -> client.delete(null));
	}
}
