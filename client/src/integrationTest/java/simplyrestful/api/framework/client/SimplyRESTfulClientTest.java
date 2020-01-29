package simplyrestful.api.framework.client;

import java.util.List;
import java.util.UUID;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriBuilder;

import org.apache.cxf.endpoint.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import simplyrestful.api.framework.client.test.implementation.TestResource;
import simplyrestful.api.framework.client.test.implementation.TestWebResource;
import simplyrestful.jetty.deploy.ServerBuilder;

@ExtendWith(MockitoExtension.class)
public class SimplyRESTfulClientTest {
    private static final UUID UUID_NIL = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static Server server;
    private static SimplyRESTfulClient<TestResource> client;

    @BeforeAll
    private static void initializeServerAndClient() {
	server = Assertions.assertDoesNotThrow(() -> new ServerBuilder()
		.withAddress(TestWebResource.TEST_HOST.toString())
		.withWebResource(TestWebResource.class)
		.build(), 
		"The test server could not be started");
	Assertions.assertNotNull(server, "The test server failed to start correctly");
	client = Assertions.assertDoesNotThrow(() -> new SimplyRESTfulClient<>(TestWebResource.TEST_HOST, TestResource.class));
	Assertions.assertNotNull(client, "The SimplyRESTful client could not be created correctly");
    }

    @AfterAll
    private static void removeServer() {
	server.stop();
	server.destroy();
    }

    @Test
    public void client_shouldDiscoverTheResourceURI_whenItIsCreated() {
	Assertions.assertDoesNotThrow(() -> client.listResourceIdentifiers(-1, -1));
    }

    @Test
    public void listResourceIds_shouldReturnTestResourceIdentifiers() {
	List<UUID> listOfResourceIdentifiers = client.listResourceIdentifiers(-1, -1);
	Assertions.assertEquals(2, listOfResourceIdentifiers.size());
	Assertions.assertTrue(listOfResourceIdentifiers.contains(TestResource.TEST_RESOURCE_ID));
    }
    
    @Test
    public void listResources_shouldReturnTestResources() {
	List<TestResource> listOfResourceIdentifiers = client.listResources(-1, -1);
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
    public void create_shouldReturnTheSameId_whenTheProvidedTestResourceContainsAnId() {
	UUID actual = client.create(TestResource.withId(UUID_NIL));
	Assertions.assertEquals(UUID_NIL, actual);
    }
    
    @Test
    public void create_shouldThrowNullPointerException_whenProvidingNullAsArgument() {
	Assertions.assertThrows(NullPointerException.class, () -> client.create(null));
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
	Assertions.assertFalse(client.exists(UriBuilder.fromUri(TestWebResource.TEST_REQUEST_BASE_URI).path(UUID_NIL.toString()).build()));
    }
    
    @Test
    public void update_shouldReturnTrue_whenTheProvidedResourceContainsANonExistingId() {
	boolean wasCreated = client.update(TestResource.withId(UUID_NIL));
	Assertions.assertFalse(wasCreated);
    }
    
    @Test
    public void update_shouldReturnFalse_whenTheProvidedResourceContainsAnExistingId() {
	boolean wasCreated = client.update(TestWebResource.TEST_RESOURCE);
	Assertions.assertFalse(wasCreated);
    }
    
    @Test
    public void update_shouldThrowNullPointerException_whenNullIsProvidedAsArgument() {
	Assertions.assertThrows(NullPointerException.class, () -> client.update(null));
    }
    
    @Test
    public void delete_shouldReturnTrue_whenAnExistingIdIsProvided() {
	boolean wasDeleted = client.delete(TestResource.TEST_RESOURCE_ID);
	Assertions.assertFalse(wasDeleted);
    }
    
    @Test
    public void delete_shouldReturnFalse_whenANonExistingIdIsProvided() {
	boolean wasDeleted = client.delete(UUID_NIL);
	Assertions.assertFalse(wasDeleted);
    }
    
    @Test
    public void delete_shouldThrowNullPointerException_whenNullIsProvidedAsArgument() {
	Assertions.assertThrows(NullPointerException.class, () -> client.delete(null));
    }
}
