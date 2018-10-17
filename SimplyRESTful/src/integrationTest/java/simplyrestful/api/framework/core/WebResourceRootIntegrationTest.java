package simplyrestful.api.framework.core;

import java.net.URI;
import java.util.Arrays;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriBuilder;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
	@Mock
	private ResourceDAO<TestResource> mockDAO;
	@InjectMocks
	private TestWebResource webResource;
	
	@BeforeEach
	private void createServerAndClient() {
		startServer();
		createClient();
	}
	
	private void startServer() {
		JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();	   
		sf.setResourceClasses(TestWebResource.class, WebResourceRoot.class);
		sf.setProvider(new JacksonJsonProvider(new HALMapper()));
		sf.setResourceProvider(TestWebResource.class, new SingletonResourceProvider(webResource, true));
		sf.setResourceProvider(WebResourceRoot.class, new SingletonResourceProvider(new WebResourceRoot(), true));
		sf.setAddress(AbstractWebResourceTest.TEST_REQUEST_BASE_URI.toString());
		server = sf.create();
	}

	private void createClient() {
		client = WebClient.create(AbstractWebResourceTest.TEST_REQUEST_BASE_URI.toString(), Arrays.asList(new JacksonJsonProvider(new HALMapper())));
		client.accept(MediaType.APPLICATION_HAL_JSON);
		client.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_HAL_JSON);
	}

	@AfterEach
	private void removeServer() {
		server.stop();
		server.destroy();
	}

	@Test
	public void webResource_shouldReturnServiceDocument_whenGETReceivedOnRootURI(){
		HALServiceDocument serviceDocument = client.get(HALServiceDocument.class);
		URI pathToOpenAPISpecification = UriBuilder.fromUri(AbstractWebResourceTest.TEST_REQUEST_BASE_URI).path("swagger.json").build();
		Assertions.assertEquals(new HALLink.Builder(pathToOpenAPISpecification).build(), serviceDocument.getDescribedby());
	}
}
