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

    @BeforeAll
    private static void startServer() {
	server = Assertions.assertDoesNotThrow(() -> new ServerBuilder()
		.withAddress(TestWebResource.TEST_HOST.toString())
		.withWebResource(TestWebResource.class)
		.build(), 
		"The test server could not be started");
	Assertions.assertNotNull(server, "The test server failed to start correctly");
    }

    @AfterAll
    private static void removeServer() {
	server.stop();
	server.destroy();
    }

    @Test
    public void client_shouldDiscoverTheResourceURI_whenItIsCreated() {
	SimplyRESTfulClient<TestResource> client = new SimplyRESTfulClient<>(TestWebResource.TEST_HOST, TestResource.class);
	List<UUID> listOfResourceIdentifiers = client.listResourceIdentifiers(-1, -1);
	Assertions.assertTrue(listOfResourceIdentifiers.contains(TestResource.TEST_RESOURCE_ID));
    }
}
