package simplyrestful.api.framework.core;

import java.util.Arrays;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;

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

import io.openapitools.jackson.dataformat.hal.HALMapper;
import simplyrestful.api.framework.core.servicedocument.WebResourceRoot;
import simplyrestful.api.framework.resources.HALCollectionV1;
import simplyrestful.api.framework.test.implementation.TestResource;
import simplyrestful.api.framework.test.implementation.TestWebResource;

@ExtendWith(MockitoExtension.class)
public class WebResourceIntegrationTest{
	private static final String WEB_RESOURCE_PATH = "testresources";
	private static final String QUERY_PARAM_COMPACT = "compact";
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
	public void webResource_shouldReturnAllResources_whenGETReceivedOnWebResourcePath(){
		HALCollectionV1<TestResource> collection = client
				.path(WEB_RESOURCE_PATH)
				.get(new GenericType<HALCollectionV1<TestResource>>() {});
		Assertions.assertEquals(2, collection.getTotal());
		Assertions.assertTrue(collection.getItem().contains(TestWebResource.TEST_RESOURCE.getSelf()));
	}

	@Test
	public void webResource_shouldReturnAllResourcesEmbedded_whenGETReceivedOnWebResourcePathAndCompactIsFalse(){
		HALCollectionV1<TestResource> collection = client
				.path(WEB_RESOURCE_PATH)
				.query(QUERY_PARAM_COMPACT, Boolean.toString(false))
				.get(new GenericType<HALCollectionV1<TestResource>>() {});
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
		TestResource expectedResource = new TestResource();
		Response response = client.path(WEB_RESOURCE_PATH).post(expectedResource);
		Assertions.assertEquals(Family.SUCCESSFUL, response.getStatusInfo().getFamily());
		Assertions.assertTrue(response.getLocation().toString().startsWith(TestResource.TEST_REQUEST_URI.toString()));
	}

	@Test
	public void webResource_shouldRemoveResource_whenDELETEReceived(){
		Response response = client.path(WEB_RESOURCE_PATH).path(TestResource.TEST_RESOURCE_ID).delete();
		Assertions.assertEquals(response.getStatusInfo().getFamily(), Family.SUCCESSFUL);
		Assertions.assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());
	}
}
