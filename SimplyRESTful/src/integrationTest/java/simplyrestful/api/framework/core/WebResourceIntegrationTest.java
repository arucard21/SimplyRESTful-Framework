package simplyrestful.api.framework.core;

import java.net.URI;
import java.util.Arrays;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import io.openapitools.jackson.dataformat.hal.HALMapper;
import simplyrestful.api.framework.core.servicedocument.WebResourceRoot;
import simplyrestful.api.framework.resources.HALCollection;
import simplyrestful.api.framework.test.implementation.TestResource;
import simplyrestful.api.framework.test.implementation.TestWebResource;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public class WebResourceIntegrationTest{
	private static final String WEB_RESOURCE_PATH = "testresources";
	private static final String QUERY_PARAM_COMPACT = "compact";
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
	public void webResource_shouldReturnAllResources_whenGETReceivedOnWebResourcePath(){
		TestResource expectedResource = new TestResource();
		Mockito.when(mockDAO.findAllForPage(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.any(URI.class))).thenReturn(Arrays.asList(expectedResource, expectedResource));
		Mockito.when(mockDAO.count()).thenReturn(2L);
		HALCollection<TestResource> collection = (HALCollection<TestResource>) client.path(WEB_RESOURCE_PATH).get(new GenericType<Object>(new TypeReference<HALCollection<TestResource>>() {}.getType()));
		Assertions.assertEquals(2, collection.getTotal());
		Assertions.assertEquals(expectedResource.getSelf(), collection.getItem().get(0));
		Assertions.assertEquals(expectedResource.getSelf(), collection.getItem().get(1));
	}
	
	@Test
	public void webResource_shouldReturnAllResourcesEmbedded_whenGETReceivedOnWebResourcePathAndCompactIsFalse(){
		TestResource expectedResource = new TestResource();
		Mockito.when(mockDAO.findAllForPage(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.any(URI.class))).thenReturn(Arrays.asList(expectedResource, expectedResource));
		Mockito.when(mockDAO.count()).thenReturn(2L);
		HALCollection<TestResource> collection = (HALCollection<TestResource>) client
				.path(WEB_RESOURCE_PATH)
				.query(QUERY_PARAM_COMPACT, new Boolean(false).toString())
				.get(new GenericType<Object>(new TypeReference<HALCollection<TestResource>>() {}.getType()));
		Assertions.assertEquals(2, collection.getTotal());
		Assertions.assertEquals(expectedResource, collection.getItemEmbedded().get(0));
		Assertions.assertEquals(expectedResource, collection.getItemEmbedded().get(1));
	}
	
	@Test
	public void webResource_shouldReturnSingleResource_whenGETReceivedWithID(){
		TestResource expectedResource = new TestResource();
		Mockito.when(mockDAO.findByURI(ArgumentMatchers.any(), ArgumentMatchers.any(URI.class))).thenReturn(expectedResource);
		TestResource testResource = client.path(WEB_RESOURCE_PATH).path(TestResource.TEST_RESOURCE_ID).get(TestResource.class);
		Assertions.assertEquals(expectedResource, testResource);
	}
	
	@Test
	public void webResource_shouldCreateResourceAndReturnLocationURI_whenPOSTReceivedWithNewResource(){
		TestResource expectedResource = new TestResource();
		Mockito.when(mockDAO.persist(ArgumentMatchers.any(TestResource.class), ArgumentMatchers.any(URI.class))).thenReturn(expectedResource);
		Response response = client.path(WEB_RESOURCE_PATH).post(expectedResource);

		Mockito.verify(mockDAO).persist(ArgumentMatchers.eq(expectedResource), ArgumentMatchers.any(URI.class));
		Assertions.assertEquals(TestResource.TEST_RESOURCE_URI, response.getLocation());
	}
	
	@Test
	public void webResource_shouldRemoveResource_whenDELETEReceived(){
		TestResource expectedResource = new TestResource();
		client.path(WEB_RESOURCE_PATH).path(TestResource.TEST_RESOURCE_ID).delete();
		Mockito.verify(mockDAO).remove(ArgumentMatchers.eq(URI.create(expectedResource.getSelf().getHref())), ArgumentMatchers.any(URI.class));
	}
}
