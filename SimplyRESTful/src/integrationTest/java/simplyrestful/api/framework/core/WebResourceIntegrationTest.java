package simplyrestful.api.framework.core;

import java.net.URI;
import java.util.UUID;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.UriBuilder;

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

import simplyrestful.api.framework.core.filters.UriCustomizer;
import simplyrestful.api.framework.core.providers.JacksonHALJsonProvider;
import simplyrestful.api.framework.core.providers.ObjectMapperProvider;
import simplyrestful.api.framework.core.servicedocument.WebResourceRoot;
import simplyrestful.api.framework.resources.HALCollectionV1;
import simplyrestful.api.framework.resources.HALCollectionV2;
import simplyrestful.api.framework.resources.HALServiceDocument;
import simplyrestful.api.framework.test.implementation.TestResource;
import simplyrestful.api.framework.test.implementation.TestWebResource;

@ExtendWith(MockitoExtension.class)
public class WebResourceIntegrationTest extends JerseyTest {
    private static final String HTTP_HEADER_NAME_CUSTOM_URI = "X-Original-URL";
    private static final String WEB_RESOURCE_PATH = "testresources";
    private static final String QUERY_PARAM_COMPACT = "compact";
    private static final MediaType MEDIA_TYPE_HALCOLLECTION_V1_HAL_JSON_TYPE = MediaType.valueOf(HALCollectionV1.MEDIA_TYPE_HAL_JSON);
    private static final MediaType MEDIA_TYPE_HALCOLLECTION_V2_HAL_JSON_TYPE = MediaType.valueOf(HALCollectionV2.MEDIA_TYPE_HAL_JSON);
    private static final MediaType MEDIA_TYPE_HALCOLLECTION_V2_JSON_TYPE = MediaType.valueOf(HALCollectionV2.MEDIA_TYPE_JSON);
    private TestResource testInstance;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
	super.setUp();
	addTestResources();
    }

    private void addTestResources() {
	testInstance = TestResource.testInstance(getBaseUri());
	TestWebResource.TEST_RESOURCES.add(testInstance);
	TestWebResource.TEST_RESOURCES.add(TestResource.random(getBaseUri()));
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
	super.tearDown();
	clearTestResources();
    }

    private void clearTestResources() {
	TestWebResource.TEST_RESOURCES.clear();
    }

    @Override
    protected Application configure() {
        ResourceConfig config = new ResourceConfig(
        	TestWebResource.class,
        	WebResourceRoot.class,
        	ObjectMapperProvider.class,
        	JacksonHALJsonProvider.class,
        	JacksonJsonProvider.class,
        	UriCustomizer.class);
        return config;
    }

    @Override
    protected void configureClient(ClientConfig config) {
	config.register(ObjectMapperProvider.class);
	config.register(JacksonHALJsonProvider.class);
	config.register(JacksonJsonProvider.class);
    }

    @Test
    @Deprecated(since="0.12.0")
    public void webResource_shouldReturnV1CollectionAsDefault_whenNoSpecificVersionIsRequested() {
	Response response = target()
		.path(WEB_RESOURCE_PATH)
		.request()
		.get();
	Assertions.assertEquals(200, response.getStatus());
	Assertions.assertEquals(MEDIA_TYPE_HALCOLLECTION_V1_HAL_JSON_TYPE, response.getMediaType());

	HALCollectionV1<TestResource> collection = response.readEntity(new GenericType<HALCollectionV1<TestResource>>() {});
	Assertions.assertEquals(2, collection.getTotal());
	Assertions.assertTrue(collection.getItem().contains(testInstance.getSelf()));
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
	Assertions.assertTrue(collection.getItem().contains(testInstance.getSelf()));
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
	Assertions.assertTrue(collection.getItemEmbedded().contains(testInstance));
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
	Assertions.assertTrue(collection.getItem().contains(testInstance));
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
	Assertions.assertTrue(collection.getItem().contains(testInstance));
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
	Assertions.assertEquals(testInstance, testResource);
    }

    @Test
    public void webResource_shouldCreateResourceAndReturnLocationURI_whenPOSTReceivedWithNewResource() {
	TestResource expectedResource = new TestResource();
	Response response = target()
		.path(WEB_RESOURCE_PATH)
		.request()
		.post(Entity.entity(expectedResource, TestResource.MEDIA_TYPE_HAL_JSON));
	Assertions.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
	Assertions.assertTrue(response.getLocation().toString().startsWith(getBaseUri().toString()));
    }

    @Test
    public void webResource_shouldUpdateResource_whenPUTReceivedWithExistingResource() {
	Response response = target()
		.path(WEB_RESOURCE_PATH)
		.path(TestResource.TEST_RESOURCE_ID.toString())
		.request()
		.put(Entity.entity(testInstance, TestResource.MEDIA_TYPE_HAL_JSON));
	Assertions.assertEquals(Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void webResource_shouldCreateResourceAndReturnLocationURI_whenPUTReceivedWithNewResource() {
	Response response = target()
		.path(WEB_RESOURCE_PATH)
		.path(UUID.randomUUID().toString())
		.request()
		.put(Entity.entity(new TestResource(), TestResource.MEDIA_TYPE_HAL_JSON));
	Assertions.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
	Assertions.assertTrue(response.getLocation().toString().startsWith(getBaseUri().toString()));
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

    @Test
    public void webResource_shouldUseCustomUriInTheLocationHeader_whenCreatingWithPostAndUriCustomizerPropertyAndHeaderAreProvided() {
	System.setProperty(UriCustomizer.CONFIGURATION_PROPERTY_NAME, HTTP_HEADER_NAME_CUSTOM_URI);
	String customUriBase = "https://simplyrestful-testhost.org/services/";
	URI customUri = UriBuilder.fromUri(customUriBase)
		.path(WEB_RESOURCE_PATH)
		.build();
	Response response = target()
		.path(WEB_RESOURCE_PATH)
		.request()
		.header(HTTP_HEADER_NAME_CUSTOM_URI, customUri)
		.post(Entity.entity(new TestResource(), TestResource.MEDIA_TYPE_HAL_JSON));
	Assertions.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
	Assertions.assertTrue(response.getLocation().toString().startsWith(customUri.toString()));
	System.clearProperty(UriCustomizer.CONFIGURATION_PROPERTY_NAME);
    }

    @Test
    public void webResource_shouldUseCustomUriInTheLocationHeader_whenCreatingWithPutAndUriCustomizerPropertyAndHeaderAreProvided() {
	System.setProperty(UriCustomizer.CONFIGURATION_PROPERTY_NAME, HTTP_HEADER_NAME_CUSTOM_URI);
	String customUriBase = "https://simplyrestful-testhost.org/services/";
	UUID customUuid = UUID.randomUUID();
	URI customUri = UriBuilder.fromUri(customUriBase)
		.path(WEB_RESOURCE_PATH)
		.path(customUuid.toString())
		.build();
	Response response = target()
		.path(WEB_RESOURCE_PATH)
		.path(customUuid.toString())
		.request()
		.header(HTTP_HEADER_NAME_CUSTOM_URI, customUri)
		.put(Entity.entity(TestResource.custom(URI.create(customUriBase), customUuid), TestResource.MEDIA_TYPE_HAL_JSON));
	Assertions.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
	Assertions.assertEquals(customUri, response.getLocation());
	System.clearProperty(UriCustomizer.CONFIGURATION_PROPERTY_NAME);
    }

    @Test
    public void webResource_shouldUseCustomUriInTheCollection_whenUriCustomizerPropertyAndHeaderAreProvided() {
	System.setProperty(UriCustomizer.CONFIGURATION_PROPERTY_NAME, HTTP_HEADER_NAME_CUSTOM_URI);
	URI customUri = UriBuilder.fromUri("https://simplyrestful-testhost.org/services/")
		.path(WEB_RESOURCE_PATH)
		.build();
	HALCollectionV2<TestResource> collection = target()
		.path(WEB_RESOURCE_PATH)
		.request()
		.accept(MEDIA_TYPE_HALCOLLECTION_V2_HAL_JSON_TYPE)
		.header(HTTP_HEADER_NAME_CUSTOM_URI, customUri)
		.get(new GenericType<HALCollectionV2<TestResource>>() {});
	Assertions.assertEquals(customUri, URI.create(collection.getSelf().getHref()));
	System.clearProperty(UriCustomizer.CONFIGURATION_PROPERTY_NAME);
    }

    @Test
    public void webResource_shouldUseCustomUriInTheServiceDocument_whenUriCustomizerPropertyAndHeaderAreProvided() {
	System.setProperty(UriCustomizer.CONFIGURATION_PROPERTY_NAME, HTTP_HEADER_NAME_CUSTOM_URI);
	URI customUri = UriBuilder.fromUri("https://simplyrestful-testhost.org/services/").build();
	HALServiceDocument serviceDocument = target()
		.request()
		.header(HTTP_HEADER_NAME_CUSTOM_URI, customUri)
		.get(HALServiceDocument.class);
	Assertions.assertEquals(customUri, URI.create(serviceDocument.getSelf().getHref()));
	System.clearProperty(UriCustomizer.CONFIGURATION_PROPERTY_NAME);
    }
}
