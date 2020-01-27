package simplyrestful.api.framework.client;

import java.util.List;
import java.util.UUID;

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
    public void client_shouldReturnTestResourceIdentifiers() {
	List<UUID> listOfResourceIdentifiers = client.listResourceIdentifiers(-1, -1);
	Assertions.assertEquals(2, listOfResourceIdentifiers.size());
	Assertions.assertTrue(listOfResourceIdentifiers.contains(TestResource.TEST_RESOURCE_ID));
    }
    
    @Test
    public void client_shouldReturnTestResources() {
	List<TestResource> listOfResourceIdentifiers = client.listResources(-1, -1);
	Assertions.assertEquals(2, listOfResourceIdentifiers.size());
	Assertions.assertTrue(listOfResourceIdentifiers.contains(TestWebResource.TEST_RESOURCE));
    }

    @Test
    public void client_shouldReturnTestResource() {
	TestResource actual = client.read(TestResource.TEST_RESOURCE_ID);
	Assertions.assertEquals(TestWebResource.TEST_RESOURCE, actual);
    }
}
