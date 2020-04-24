package simplyrestful.api.framework.core;

import java.net.URI;
import java.util.Arrays;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriBuilder;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.HALMapper;
import simplyrestful.api.framework.core.servicedocument.WebResourceRoot;
import simplyrestful.api.framework.resources.HALServiceDocument;
import simplyrestful.api.framework.test.implementation.TestResource;
import simplyrestful.api.framework.test.implementation.TestWebResource;

@ExtendWith(MockitoExtension.class)
public class WebResourceRootIntegrationTest{
	private Server server;
	private WebClient client;
	
	@BeforeEach
	private void createServerAndClient() {
		startServer();
		createClient();
	}
	
	private void startServer() {
		JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();	   
		sf.setResourceClasses(TestWebResource.class, WebResourceRoot.class);
		sf.setProvider(new JacksonJsonProvider(new HALMapper()));
		sf.setResourceClasses(TestWebResource.class, WebResourceRoot.class);
		sf.setAddress(TestResource.TEST_REQUEST_BASE_URI.toString());
		server = sf.create();
	}

	private void createClient() {
		client = WebClient.create(TestResource.TEST_REQUEST_BASE_URI.toString(), Arrays.asList(new JacksonJsonProvider(new HALMapper())));
		client.accept(AdditionalMediaTypes.APPLICATION_HAL_JSON);
		client.header(HttpHeaders.CONTENT_TYPE, AdditionalMediaTypes.APPLICATION_HAL_JSON);
	}

	@AfterEach
	private void removeServer() {
		server.stop();
		server.destroy();
	}

	@Test
	public void webResource_shouldReturnServiceDocument_whenGETReceivedOnRootURI(){
		HALServiceDocument serviceDocument = client.get(HALServiceDocument.class);
		URI pathToOpenAPISpecification = UriBuilder.fromUri(TestResource.TEST_REQUEST_BASE_URI).path("swagger.json").build();
		Assertions.assertEquals(new HALLink.Builder(pathToOpenAPISpecification).build(), serviceDocument.getDescribedby());
	}
}
