package simplyrestful.api.framework.client;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.provider.MultipartProvider;
import org.apache.cxf.jaxrs.swagger.Swagger2Feature;
import org.apache.cxf.jaxrs.validation.JAXRSBeanValidationFeature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import io.openapitools.jackson.dataformat.hal.HALMapper;
import simplyrestful.api.framework.client.test.implementation.TestResource;
import simplyrestful.api.framework.client.test.implementation.TestWebResource;
import simplyrestful.api.framework.core.servicedocument.WebResourceRoot;

@ExtendWith(MockitoExtension.class)
public class SimplyRESTfulClientTest {
	private Server server;

	@BeforeEach
	private void startServer() {
		JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
		sf.setResourceClasses(TestWebResource.class, WebResourceRoot.class);
		sf.setAddress(TestResource.TEST_REQUEST_BASE_URI.toString());

		Swagger2Feature swagger = new Swagger2Feature();
	    swagger.setPrettyPrint(true);
	    sf.getFeatures().add(swagger);
	    sf.getFeatures().add(new JAXRSBeanValidationFeature());
	    sf.setProviders(Arrays.asList(
	    		new MultipartProvider(),
	    		new JacksonJsonProvider(new HALMapper())));
	    server = sf.create();
		Assertions.assertNotNull(server);
	}

	@AfterEach
	private void removeServer() {
		server.stop();
		server.destroy();
	}

	@Test
	public void client_shouldDiscoverTheResourceURI_whenItIsCreated() {
		SimplyRESTfulClient<TestResource> client = new SimplyRESTfulClient<>(TestResource.TEST_REQUEST_BASE_URI);
		List<UUID> listOfResourceIdentifiers = client.listResourceIdentifiers(-1, -1);
		Assertions.assertTrue(listOfResourceIdentifiers.contains(TestResource.TEST_RESOURCE_ID));
	}
}
