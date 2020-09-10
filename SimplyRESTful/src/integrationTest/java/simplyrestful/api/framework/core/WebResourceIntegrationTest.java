package simplyrestful.api.framework.core;

import java.util.Arrays;
import java.util.Map;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
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

import simplyrestful.api.framework.core.providers.HALMapperProvider;
import simplyrestful.api.framework.core.providers.ObjectMapperProvider;
import simplyrestful.api.framework.core.servicedocument.WebResourceRoot;
import simplyrestful.api.framework.resources.HALCollectionV1;
import simplyrestful.api.framework.resources.HALCollectionV2;
import simplyrestful.api.framework.test.implementation.TestResource;
import simplyrestful.api.framework.test.implementation.TestWebResource;

@ExtendWith(MockitoExtension.class)
public class WebResourceIntegrationTest {
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
	sf.setProvider(JacksonJsonProvider.class);
	sf.setProvider(HALMapperProvider.class);
	sf.setProvider(ObjectMapperProvider.class);
	sf.setAddress(TestResource.TEST_REQUEST_BASE_URI.toString());
	server = sf.create();
    }

    private void createClient() {
	client = WebClient.create(TestResource.TEST_REQUEST_BASE_URI.toString(),
		Arrays.asList(JacksonJsonProvider.class, HALMapperProvider.class, ObjectMapperProvider.class));
	client.header(HttpHeaders.CONTENT_TYPE, AdditionalMediaTypes.APPLICATION_HAL_JSON);
    }

    @AfterEach
    private void removeServer() {
	server.stop();
	server.destroy();
    }

    @Test
    @Deprecated(since="0.12.0")
    public void webResource_shouldReturnAllResourcesInV1Collection_whenGETReceivedOnWebResourcePath() {
	Response response = client
		.path(WEB_RESOURCE_PATH)
		.accept(HALCollectionV1.MEDIA_TYPE_HAL_JSON)
		.get();		
	Assertions.assertEquals(200, response.getStatus());
	HALCollectionV1<TestResource> collection = response.readEntity(new GenericType<HALCollectionV1<TestResource>>() {}); 
	Assertions.assertEquals(2, collection.getTotal());
	Assertions.assertTrue(collection.getItem().contains(TestWebResource.TEST_RESOURCE.getSelf()));
    }

    @Test
    @Deprecated(since="0.12.0")
    public void webResource_shouldReturnAllResourcesEmbeddedInV1Collection_whenGETReceivedOnWebResourcePathAndCompactIsFalse() {
	Response response = client
		.path(WEB_RESOURCE_PATH)
		.query(QUERY_PARAM_COMPACT, Boolean.toString(false))
		.accept(HALCollectionV1.MEDIA_TYPE_HAL_JSON)
		.get();
	Assertions.assertEquals(200, response.getStatus());
	Assertions.assertEquals(
		halJsonWithProfile(HALCollectionV1.PROFILE_STRING),
		response.getMediaType());
	HALCollectionV1<TestResource> collection = response.readEntity(new GenericType<HALCollectionV1<TestResource>>() {});
	Assertions.assertEquals(2, collection.getTotal());
	Assertions.assertTrue(collection.getItemEmbedded().contains(TestWebResource.TEST_RESOURCE));
    }

    @Test
    public void webResource_shouldReturnAllResourcesEmbeddedInHALJsonV2Collection_whenGETToPathAndAcceptIsHALJson() {
	Response response = client.path(WEB_RESOURCE_PATH)
		.accept(HALCollectionV2.MEDIA_TYPE_HAL_JSON)
		.get();
	Assertions.assertEquals(200, response.getStatus());
	Assertions.assertEquals(
		halJsonWithProfile(HALCollectionV2.PROFILE_STRING),
		response.getMediaType());
	HALCollectionV2<TestResource> collection = response
		.readEntity(new GenericType<HALCollectionV2<TestResource>>() {});
	Assertions.assertEquals(2, collection.getTotal());
	Assertions.assertTrue(collection.getItem().contains(TestWebResource.TEST_RESOURCE));
    }
    
    @Test
    public void webResource_shouldContainLinkAndEmbeddedFieldsInHALJsonV2Collection_whenGETToPathAndAcceptIsHALJson() {
	Response response = client.path(WEB_RESOURCE_PATH)
		.accept(HALCollectionV2.MEDIA_TYPE_HAL_JSON)
		.get();
	Assertions.assertEquals(200, response.getStatus());
	Assertions.assertEquals(
		halJsonWithProfile(HALCollectionV2.PROFILE_STRING),
		response.getMediaType());
	String halJsonRepresentation = response.readEntity(String.class);
	Assertions.assertTrue(halJsonRepresentation.contains("_links"));
	Assertions.assertTrue(halJsonRepresentation.contains("_embedded"));
    }

    @Test
    public void webResource_shouldReturnAllResourcesEmbeddedInHALJsonV2Collection_whenGETToPathAndAcceptIsCustomJson() {
	Response response = client.path(WEB_RESOURCE_PATH)
		.accept(HALCollectionV2.MEDIA_TYPE_JSON)
		.get();
	Assertions.assertEquals(200, response.getStatus());
	Assertions.assertEquals(
		MediaType.valueOf(HALCollectionV2.MEDIA_TYPE_JSON),
		response.getMediaType());
	HALCollectionV2<TestResource> collection = response
		.readEntity(new GenericType<HALCollectionV2<TestResource>>() {});
	Assertions.assertEquals(2, collection.getTotal());
	Assertions.assertTrue(collection.getItem().contains(TestWebResource.TEST_RESOURCE));
    }
    
    @Test
    public void webResource_shouldNotContainLinkAndEmbeddedFieldsInHALJsonV2Collection_whenGETToPathAndAcceptIsCustomJson() {
	Response response = client.path(WEB_RESOURCE_PATH)
		.header(HttpHeaders.ACCEPT, HALCollectionV2.MEDIA_TYPE_JSON).get();
	Assertions.assertEquals(200, response.getStatus());
	Assertions.assertEquals(
		MediaType.valueOf(HALCollectionV2.MEDIA_TYPE_JSON),
		response.getMediaType());
	String jsonRepresentation = response.readEntity(String.class);
	Assertions.assertFalse(jsonRepresentation.contains("_links"));
	Assertions.assertFalse(jsonRepresentation.contains("_embedded"));
    }

    @Test
    public void webResource_shouldReturnSingleResource_whenGETReceivedWithID() {
	TestResource testResource = client
		.path(WEB_RESOURCE_PATH)
		.path(TestResource.TEST_RESOURCE_ID)
		.get(TestResource.class);
	Assertions.assertEquals(TestWebResource.TEST_RESOURCE, testResource);
    }

    @Test
    public void webResource_shouldCreateResourceAndReturnLocationURI_whenPOSTReceivedWithNewResource() {
	TestResource expectedResource = new TestResource();
	Response response = client
		.path(WEB_RESOURCE_PATH)
		.post(expectedResource);
	Assertions.assertEquals(Family.SUCCESSFUL, response.getStatusInfo().getFamily());
	Assertions.assertTrue(response.getLocation().toString().startsWith(TestResource.TEST_REQUEST_URI.toString()));
    }

    @Test
    public void webResource_shouldRemoveResource_whenDELETEReceived() {
	Response response = client
		.path(WEB_RESOURCE_PATH)
		.path(TestResource.TEST_RESOURCE_ID)
		.delete();
	Assertions.assertEquals(response.getStatusInfo().getFamily(), Family.SUCCESSFUL);
	Assertions.assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    /**
     * Convenience method to create a MediaType instance for HAL+JSON with a profile attribute.
     * 
     * The default MediaType.valueOf() method breaks when the profile value has a ":" in it, 
     * as it does for our URI-based profile name. Using the constructor does allow the instance
     * to be created. 
     *  
     * @param profile is the HAL+JSON profile for this resource.
     * @return the MediaType instance for the provided profile.
     */
    private MediaType halJsonWithProfile(String profile) {
        return new MediaType("application", "hal+json", Map.of("profile", profile));
    }
}
