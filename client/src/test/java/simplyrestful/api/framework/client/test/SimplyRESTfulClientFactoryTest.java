package simplyrestful.api.framework.client.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;

import jakarta.ws.rs.client.Client;
import simplyrestful.api.framework.client.SimplyRESTfulClientFactory;
import simplyrestful.api.framework.client.test.implementation.TestResource;
import simplyrestful.api.framework.providers.ObjectMapperProvider;

@ExtendWith(MockitoExtension.class)
public class SimplyRESTfulClientFactoryTest {
	@Mock
	Client client;

	@Test
	public void clientFactory_shouldUseProvidedJaxrsClientInClientAndRegisterRequiredProviders() {
		new SimplyRESTfulClientFactory<TestResource>(client).newClient(null, TestResource.class);
		Mockito.verify(client).register(JacksonJsonProvider.class);
		Mockito.verify(client).register(ObjectMapperProvider.class);
	}

}
