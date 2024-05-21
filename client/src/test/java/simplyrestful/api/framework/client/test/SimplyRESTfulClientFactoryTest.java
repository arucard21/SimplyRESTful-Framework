package simplyrestful.api.framework.client.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.GenericType;
import simplyrestful.api.framework.client.SimplyRESTfulClientFactory;
import simplyrestful.api.framework.client.test.implementation.TestResource;
import simplyrestful.api.framework.providers.ObjectMapperProvider;
import simplyrestful.api.framework.resources.APICollection;

@ExtendWith(MockitoExtension.class)
public class SimplyRESTfulClientFactoryTest {
	@Mock
	Client client;

	@Test
	public void clientFactory_shouldUseProvidedJaxrsClientInClientAndRegisterRequiredProviders() {
		new SimplyRESTfulClientFactory<TestResource>(client).newClient(null, new GenericType<APICollection<TestResource>>() {});
		Mockito.verify(client).register(ObjectMapperProvider.class);
	}

}
