package simplyrestful.api.framework.client.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.GenericType;
import simplyrestful.api.framework.client.SimplyRESTfulClient;
import simplyrestful.api.framework.client.test.implementation.TestResource;
import simplyrestful.api.framework.resources.APICollection;

@ExtendWith(MockitoExtension.class)
public class SimplyRESTfulClientTest {
	@Mock
	Client client;

	@Test
	public void clientFactory_shouldUseProvidedJaxrsClientInClientAndRegisterRequiredProviders() {
		Assertions.assertDoesNotThrow(() -> new SimplyRESTfulClient<TestResource>(client, null, new GenericType<APICollection<TestResource>>() {}));
	}
}
