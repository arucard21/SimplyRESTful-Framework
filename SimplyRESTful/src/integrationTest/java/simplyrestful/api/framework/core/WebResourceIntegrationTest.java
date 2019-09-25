package simplyrestful.api.framework.core;

import java.net.URI;
import java.util.Arrays;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.UriInfo;

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

import io.openapitools.jackson.dataformat.hal.HALMapper;
import simplyrestful.api.framework.core.servicedocument.WebResourceRoot;
import simplyrestful.api.framework.resources.HALCollection;
import simplyrestful.api.framework.test.implementation.TestResource;
import simplyrestful.api.framework.test.implementation.TestWebResource;

@ExtendWith(MockitoExtension.class)
public class WebResourceIntegrationTest{
	private static final String WEB_RESOURCE_PATH = "testresources";
	private static final String QUERY_PARAM_COMPACT = "compact";
	private Server server;
	private WebClient client;
	@Mock
	private UriInfo uriInfo;
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
		sf.setAddress(DefaultWebResourceTest.TEST_REQUEST_BASE_URI.toString());
		server = sf.create();
	}

	private void createClient() {
		client = WebClient.create(DefaultWebResourceTest.TEST_REQUEST_BASE_URI.toString(), Arrays.asList(new JacksonJsonProvider(new HALMapper())));
		client.accept(AdditionalMediaTypes.APPLICATION_HAL_JSON);
		client.header(HttpHeaders.CONTENT_TYPE, AdditionalMediaTypes.APPLICATION_HAL_JSON);
	}

	@AfterEach
	private void removeServer() {
		server.stop();
		server.destroy();
	}

	@Test
	public void webResource_shouldReturnAllResources_whenGETReceivedOnWebResourcePath(){
		HALCollection<TestResource> collection = (HALCollection<TestResource>) client
				.path(WEB_RESOURCE_PATH)
				.get(new GenericType<HALCollection<TestResource>>() {});
		Assertions.assertEquals(2, collection.getTotal());
		Assertions.assertTrue(collection.getItem().contains(TestWebResource.TEST_RESOURCE.getSelf()));
	}
	
	@Test
	public void webResource_shouldReturnAllResourcesEmbedded_whenGETReceivedOnWebResourcePathAndCompactIsFalse(){
		HALCollection<TestResource> collection = (HALCollection<TestResource>) client
				.path(WEB_RESOURCE_PATH)
				.query(QUERY_PARAM_COMPACT, new Boolean(false).toString())
				.get(new GenericType<HALCollection<TestResource>>() {});
		Assertions.assertEquals(2, collection.getTotal());
		Assertions.assertTrue(collection.getItemEmbedded().contains(TestWebResource.TEST_RESOURCE));
	}
	
	@Test
	public void webResource_shouldReturnSingleResource_whenGETReceivedWithID(){
		TestResource testResource = client.path(WEB_RESOURCE_PATH).path(TestResource.TEST_RESOURCE_ID).get(TestResource.class);
		Assertions.assertEquals(TestWebResource.TEST_RESOURCE, testResource);
	}
	
	@Test
	public void webResource_shouldCreateResourceAndReturnLocationURI_whenPOSTReceivedWithNewResource(){
		TestResource expectedResource = TestResource.random();
		Response response = client.path(WEB_RESOURCE_PATH).post(expectedResource);
		Assertions.assertEquals(response.getStatusInfo().getFamily(), Family.SUCCESSFUL);
		Assertions.assertEquals(URI.create(expectedResource.getSelf().getHref()), response.getLocation());
	}

	@Test
	public void webResource_shouldRemoveResource_whenDELETEReceived(){
		Response response = client.path(WEB_RESOURCE_PATH).path(TestResource.TEST_RESOURCE_ID).delete();
		Assertions.assertEquals(response.getStatusInfo().getFamily(), Family.SUCCESSFUL);
		Assertions.assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());
	}
}
