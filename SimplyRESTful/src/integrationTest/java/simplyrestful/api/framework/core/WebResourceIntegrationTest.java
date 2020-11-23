package simplyrestful.api.framework.core;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import simplyrestful.api.framework.core.providers.JacksonHALJsonProvider;
import simplyrestful.api.framework.core.providers.ObjectMapperProvider;
import simplyrestful.api.framework.core.servicedocument.WebResourceRoot;
import simplyrestful.api.framework.resources.HALCollectionV1;
import simplyrestful.api.framework.resources.HALCollectionV2;
import simplyrestful.api.framework.test.implementation.TestResource;
import simplyrestful.api.framework.test.implementation.TestWebResource;

@ExtendWith(MockitoExtension.class)
public class WebResourceIntegrationTest extends JerseyTest {
    private static final String WEB_RESOURCE_PATH = "testresources";
    private static final String QUERY_PARAM_COMPACT = "compact";
    private static final MediaType MEDIA_TYPE_HALCOLLECTION_V1_HAL_JSON_TYPE = MediaType.valueOf(HALCollectionV1.MEDIA_TYPE_HAL_JSON);
    private static final MediaType MEDIA_TYPE_HALCOLLECTION_V2_HAL_JSON_TYPE = MediaType.valueOf(HALCollectionV2.MEDIA_TYPE_HAL_JSON);
    private static final MediaType MEDIA_TYPE_HALCOLLECTION_V2_JSON_TYPE = MediaType.valueOf(HALCollectionV2.MEDIA_TYPE_JSON);

    @BeforeEach
    @Override
    public void setUp() throws Exception {
	super.setUp();
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
	super.tearDown();
    }

    @Override
    protected Application configure() {
        ResourceConfig config = new ResourceConfig(
        	TestWebResource.class,
        	WebResourceRoot.class,
        	ObjectMapperProvider.class,
        	JacksonHALJsonProvider.class,
        	JacksonJsonProvider.class);
        return config;
    }

    @Override
    protected void configureClient(ClientConfig config) {
	config.register(ObjectMapperProvider.class);
	config.register(JacksonHALJsonProvider.class);
	config.register(JacksonJsonProvider.class);
    }

    // FIXME: Switch server setup to Jersey
    @Test
    @Deprecated(since="0.12.0")
    public void webResource_shouldReturnAllResourcesInV1Collection_whenGETReceivedOnWebResourcePath() {
	Response response = target()
		.path(WEB_RESOURCE_PATH)
		.request()
		.accept(MEDIA_TYPE_HALCOLLECTION_V1_HAL_JSON_TYPE)
		.get();
	Assertions.assertEquals(200, response.getStatus());
	Assertions.assertEquals(MEDIA_TYPE_HALCOLLECTION_V1_HAL_JSON_TYPE, response.getMediaType());

	HALCollectionV1<TestResource> collection = response.readEntity(new GenericType<HALCollectionV1<TestResource>>() {});
	Assertions.assertEquals(2, collection.getTotal());
	Assertions.assertTrue(collection.getItem().contains(TestWebResource.TEST_RESOURCE.getSelf()));
    }

    @Test
    @Deprecated(since="0.12.0")
    public void webResource_shouldReturnAllResourcesEmbeddedInV1Collection_whenGETReceivedOnWebResourcePathAndCompactIsFalse() {
	Response response = target()
		.path(WEB_RESOURCE_PATH)
		.queryParam(QUERY_PARAM_COMPACT, Boolean.toString(false))
		.request()
		.accept(MEDIA_TYPE_HALCOLLECTION_V1_HAL_JSON_TYPE)
		.get();
	Assertions.assertEquals(200, response.getStatus());
	Assertions.assertEquals(MEDIA_TYPE_HALCOLLECTION_V1_HAL_JSON_TYPE, response.getMediaType());

	HALCollectionV1<TestResource> collection = response.readEntity(new GenericType<HALCollectionV1<TestResource>>() {});
	Assertions.assertEquals(2, collection.getTotal());
	Assertions.assertTrue(collection.getItemEmbedded().contains(TestWebResource.TEST_RESOURCE));
    }

    @Test
    public void webResource_shouldReturnAllResourcesEmbeddedInHALJsonV2Collection_whenGETToPathAndAcceptIsHALJson() {
	Response response = target()
		.path(WEB_RESOURCE_PATH)
		.request()
		.accept(MEDIA_TYPE_HALCOLLECTION_V2_HAL_JSON_TYPE)
		.get();
	Assertions.assertEquals(200, response.getStatus());
	Assertions.assertEquals(MEDIA_TYPE_HALCOLLECTION_V2_HAL_JSON_TYPE, response.getMediaType());
	HALCollectionV2<TestResource> collection = response
		.readEntity(new GenericType<HALCollectionV2<TestResource>>() {});
	Assertions.assertEquals(2, collection.getTotal());
	Assertions.assertTrue(collection.getItem().contains(TestWebResource.TEST_RESOURCE));
    }

    @Test
    public void webResource_shouldContainLinkAndEmbeddedFieldsInHALJsonV2Collection_whenGETToPathAndAcceptIsHALJson() {
	Response response = target()
		.path(WEB_RESOURCE_PATH)
		.request()
		.accept(MEDIA_TYPE_HALCOLLECTION_V2_HAL_JSON_TYPE)
		.get();
	Assertions.assertEquals(200, response.getStatus());
	Assertions.assertEquals(MEDIA_TYPE_HALCOLLECTION_V2_HAL_JSON_TYPE, response.getMediaType());
	String halJsonRepresentation = response.readEntity(String.class);
	Assertions.assertTrue(halJsonRepresentation.contains("_links"));
	Assertions.assertTrue(halJsonRepresentation.contains("_embedded"));
    }

    @Test
    public void webResource_shouldReturnAllResourcesEmbeddedInHALJsonV2Collection_whenGETToPathAndAcceptIsCustomJson() {
	Response response = target()
		.path(WEB_RESOURCE_PATH)
		.request()
		.accept(MEDIA_TYPE_HALCOLLECTION_V2_JSON_TYPE)
		.get();
	Assertions.assertEquals(200, response.getStatus());
	Assertions.assertEquals(MEDIA_TYPE_HALCOLLECTION_V2_JSON_TYPE, response.getMediaType());

	HALCollectionV2<TestResource> collection = response.readEntity(new GenericType<HALCollectionV2<TestResource>>() {});
	Assertions.assertEquals(2, collection.getTotal());
	Assertions.assertTrue(collection.getItem().contains(TestWebResource.TEST_RESOURCE));
    }

    @Test
    public void webResource_shouldNotContainLinkAndEmbeddedFieldsInHALJsonV2Collection_whenGETToPathAndAcceptIsCustomJson() {
	Response response = target()
		.path(WEB_RESOURCE_PATH)
		.request()
		.accept(MEDIA_TYPE_HALCOLLECTION_V2_JSON_TYPE).get();
	Assertions.assertEquals(200, response.getStatus());
	Assertions.assertEquals(MEDIA_TYPE_HALCOLLECTION_V2_JSON_TYPE, response.getMediaType());

	String jsonRepresentation = response.readEntity(String.class);
	Assertions.assertFalse(jsonRepresentation.contains("_links"));
	Assertions.assertFalse(jsonRepresentation.contains("_embedded"));
    }

    @Test
    public void webResource_shouldReturnSingleResource_whenGETReceivedWithID() {
	TestResource testResource = target()
		.path(WEB_RESOURCE_PATH)
		.path(TestResource.TEST_RESOURCE_ID.toString())
		.request()
		.get(TestResource.class);
	Assertions.assertEquals(TestWebResource.TEST_RESOURCE, testResource);
    }

    @Test
    public void webResource_shouldCreateResourceAndReturnLocationURI_whenPOSTReceivedWithNewResource() {
	TestResource expectedResource = new TestResource();
	Response response = target()
		.path(WEB_RESOURCE_PATH)
		.request()
		.post(Entity.entity(expectedResource, TestResource.MEDIA_TYPE_HAL_JSON));
	Assertions.assertEquals(Family.SUCCESSFUL, response.getStatusInfo().getFamily());
	Assertions.assertTrue(response.getLocation().toString().startsWith(TestResource.TEST_REQUEST_URI.toString()));
    }

    @Test
    public void webResource_shouldRemoveResource_whenDELETEReceived() {
	Response response = target()
		.path(WEB_RESOURCE_PATH)
		.path(TestResource.TEST_RESOURCE_ID.toString())
		.request()
		.delete();
	Assertions.assertEquals(response.getStatusInfo().getFamily(), Family.SUCCESSFUL);
	Assertions.assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }
}
