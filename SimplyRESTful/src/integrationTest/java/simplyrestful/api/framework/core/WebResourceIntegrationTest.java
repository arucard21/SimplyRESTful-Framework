package simplyrestful.api.framework.core;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.sse.SseEventSource;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

@SuppressWarnings("deprecation")
public class WebResourceIntegrationTest extends JerseyTest {
    private static final String HTTP_HEADER_NAME_CUSTOM_URI = "X-Original-URL";
    private static final String WEB_RESOURCE_PATH = "testresources";
    private static final String QUERY_PARAM_COMPACT = "compact";
    private static final MediaType MEDIA_TYPE_HALCOLLECTION_V1_HAL_JSON_TYPE = MediaType.valueOf(HALCollectionV1.MEDIA_TYPE_HAL_JSON);
    private static final MediaType MEDIA_TYPE_HALCOLLECTION_V2_HAL_JSON_TYPE = MediaType.valueOf(HALCollectionV2.MEDIA_TYPE_HAL_JSON);
    private static final MediaType MEDIA_TYPE_HALCOLLECTION_V2_JSON_TYPE = MediaType.valueOf(HALCollectionV2.MEDIA_TYPE_JSON);
    private TestResource testInstance;
    private TestResource testInstanceWithRandomId;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        addTestResources();
    }

    private void addTestResources() {
        testInstance = TestResource.testInstance(getBaseUri());
        TestWebResource.TEST_RESOURCES.add(testInstance);
        testInstanceWithRandomId = TestResource.random(getBaseUri());
        TestWebResource.TEST_RESOURCES.add(testInstanceWithRandomId);
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
    public void webResource_shouldReturnPlainJsonV2CollectionAsDefault_whenNoSpecificVersionIsRequested() {
    	Response response = target()
    		.path(WEB_RESOURCE_PATH)
    		.request()
    		.get();
    	Assertions.assertEquals(200, response.getStatus());
    	Assertions.assertEquals(MEDIA_TYPE_HALCOLLECTION_V2_JSON_TYPE, response.getMediaType());

    	HALCollectionV2<TestResource> collection = response.readEntity(new GenericType<HALCollectionV2<TestResource>>() {});
    	Assertions.assertEquals(2, collection.getTotal());
    	Assertions.assertTrue(collection.getItem().contains(testInstance));
    }

    @Test
    public void webResource_shouldReturnHALV2Collection_whenHALV2IsRequested() {
        Response response = target()
        	.path(WEB_RESOURCE_PATH)
        	.request()
        	.accept(MEDIA_TYPE_HALCOLLECTION_V2_HAL_JSON_TYPE)
        	.get();
        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertEquals(MEDIA_TYPE_HALCOLLECTION_V2_HAL_JSON_TYPE, response.getMediaType());
        HALCollectionV2<TestResource> collection = response.readEntity(new GenericType<HALCollectionV2<TestResource>>() {});
        Assertions.assertEquals(2, collection.getTotal());
        Assertions.assertTrue(collection.getItem().contains(testInstance));
    }

    @Test
    @Deprecated(since="0.12.0")
    public void webResource_shouldReturnHALV1CollectionWithLinkedResourcesByDefault_whenV1IsRequested() {
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
    public void webResource_shouldReturnHALV1CollectionWithEmbeddedResources_whenV1IsRequestedAndCompactIsFalse() {
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
    public void webResource_shouldReturnResourcesInCollectionAsEventsInTheSameOrderAsTheList_whenRequestingEventStream() {
        List<String> receivedSelfLinks = new ArrayList<>();
        try (SseEventSource eventSource = SseEventSource.target(target().path(WEB_RESOURCE_PATH)).build()) {
            eventSource.register(
                    event -> {
                        receivedSelfLinks.add(event.readData(TestResource.class).getSelf().getHref());
                        },
                    error -> {
                        Assertions.fail("An error occurred while receiving events.");
                    },
                    () -> {
                        Assertions.assertEquals(2, receivedSelfLinks.size());
                        Assertions.assertEquals(testInstance.getSelf().getHref(), receivedSelfLinks.get(0));
                        Assertions.assertEquals(testInstanceWithRandomId.getSelf().getHref(), receivedSelfLinks.get(1));
                    });
            eventSource.open();
            Thread.sleep(10); // wait to ensure all events have been received before closing the SseEventSource
        } catch (InterruptedException e) { /* do nothing when interrupted */ }
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
    public void webResource_shouldReturn409Conflict_whenPOSTReceivedWithExistingResource() {
    	Response response = target()
    		.path(WEB_RESOURCE_PATH)
    		.request()
    		.post(Entity.entity(testInstance, TestResource.MEDIA_TYPE_HAL_JSON));
    	Assertions.assertEquals(Status.CONFLICT.getStatusCode(), response.getStatus());
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
