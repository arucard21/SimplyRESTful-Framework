package simplyrestful.api.framework.integrationtest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.Response.Status.Family;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.sse.SseEventSource;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;

import simplyrestful.api.framework.filters.UriCustomizer;
import simplyrestful.api.framework.integrationTest.implementation.TestResource;
import simplyrestful.api.framework.integrationTest.implementation.TestWebResource;
import simplyrestful.api.framework.providers.ObjectMapperProvider;
import simplyrestful.api.framework.resources.APICollection;
import simplyrestful.api.framework.resources.APIServiceDocument;
import simplyrestful.api.framework.servicedocument.WebResourceRoot;

public class WebResourceIntegrationTest extends JerseyTest {
    public static final String HTTP_HEADER_NAME_CUSTOM_URI = "X-Original-URL";
    public static final String WEB_RESOURCE_PATH = "testresources";
    public static final MediaType MEDIA_TYPE_COLLECTION_V2_JSON_TYPE = MediaType.valueOf(APICollection.MEDIA_TYPE_JSON);
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
        	JacksonJsonProvider.class,
        	UriCustomizer.class);
        return config;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(ObjectMapperProvider.class);
        config.register(JacksonJsonProvider.class);
    }

    @Test
    public void webResource_shouldReturnPlainJsonV2CollectionAsDefault_whenNoSpecificVersionIsRequested() {
    	Response response = target()
    		.path(WEB_RESOURCE_PATH)
    		.request()
    		.get();
    	Assertions.assertEquals(200, response.getStatus());
    	Assertions.assertEquals(MEDIA_TYPE_COLLECTION_V2_JSON_TYPE, response.getMediaType());

    	APICollection<TestResource> collection = response.readEntity(new GenericType<APICollection<TestResource>>() {});
    	Assertions.assertEquals(2, collection.getTotal());
    	Assertions.assertTrue(collection.getItem().contains(testInstance));
    }

    @Test
    public void webResource_shouldReturn406NotAcceptable_whenHALV1IsRequested() {
        Response response = target()
        	.path(WEB_RESOURCE_PATH)
        	.request()
        	.accept("application/hal+json;profile=\"https://arucard21.github.io/SimplyRESTful-Framework/HALCollection/v1\"")
        	.get();
        Assertions.assertEquals(406, response.getStatus());
    }

    @Test
    public void webResource_shouldReturn406NotAcceptable_whenHALV2IsRequested() {
        Response response = target()
        	.path(WEB_RESOURCE_PATH)
        	.request()
        	.accept("application/hal+json;profile=\"https://arucard21.github.io/SimplyRESTful-Framework/HALCollection/v2\"")
        	.get();
        Assertions.assertEquals(406, response.getStatus());
    }

    @Test
    public void webResource_shouldReturnAllResourcesEmbeddedInCollection_whenGETToPathAndAcceptIsCustomJson() {
    	Response response = target()
    		.path(WEB_RESOURCE_PATH)
    		.request()
    		.accept(MEDIA_TYPE_COLLECTION_V2_JSON_TYPE)
    		.get();
    	Assertions.assertEquals(200, response.getStatus());
    	Assertions.assertEquals(MEDIA_TYPE_COLLECTION_V2_JSON_TYPE, response.getMediaType());

    	APICollection<TestResource> collection = response.readEntity(new GenericType<APICollection<TestResource>>() {});
    	Assertions.assertEquals(2, collection.getTotal());
    	Assertions.assertTrue(collection.getItem().contains(testInstance));
    }

    @Test
    public void webResource_shouldNotContainLinkAndEmbeddedFieldsInCollection_whenGETToPathAndAcceptIsCustomJson() {
    	Response response = target()
    		.path(WEB_RESOURCE_PATH)
    		.request()
    		.accept(MEDIA_TYPE_COLLECTION_V2_JSON_TYPE).get();
    	Assertions.assertEquals(200, response.getStatus());
    	Assertions.assertEquals(MEDIA_TYPE_COLLECTION_V2_JSON_TYPE, response.getMediaType());

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
                        receivedSelfLinks.add(event.readData(TestResource.class).getSelf().getHref().toString());
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
    		.post(Entity.entity(expectedResource, TestResource.MEDIA_TYPE_JSON));
    	Assertions.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
    	Assertions.assertTrue(response.getLocation().toString().startsWith(getBaseUri().toString()));
    }

    @Test
    public void webResource_shouldReturn409Conflict_whenPOSTReceivedWithExistingResource() {
    	Response response = target()
    		.path(WEB_RESOURCE_PATH)
    		.request()
    		.post(Entity.entity(testInstance, TestResource.MEDIA_TYPE_JSON));
    	Assertions.assertEquals(Status.CONFLICT.getStatusCode(), response.getStatus());
    }

    @Test
    public void webResource_shouldUpdateResource_whenPUTReceivedWithExistingResource() {
    	Response response = target()
    		.path(WEB_RESOURCE_PATH)
    		.path(TestResource.TEST_RESOURCE_ID.toString())
    		.request()
    		.put(Entity.entity(testInstance, TestResource.MEDIA_TYPE_JSON));
    	Assertions.assertEquals(Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void webResource_shouldThrow404NotFoundError_whenPUTReceivedWithNewResource() {
    	Response response = target()
    		.path(WEB_RESOURCE_PATH)
    		.path(UUID.randomUUID().toString())
    		.request()
    		.put(Entity.entity(new TestResource(), TestResource.MEDIA_TYPE_JSON));
    	Assertions.assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
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
    		.post(Entity.entity(new TestResource(), TestResource.MEDIA_TYPE_JSON));
    	Assertions.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
    	Assertions.assertTrue(response.getLocation().toString().startsWith(customUri.toString()));
    	System.clearProperty(UriCustomizer.CONFIGURATION_PROPERTY_NAME);
    }

    @Test
    public void webResource_shouldUseCustomUriInTheCollection_whenUriCustomizerPropertyAndHeaderAreProvided() {
    	System.setProperty(UriCustomizer.CONFIGURATION_PROPERTY_NAME, HTTP_HEADER_NAME_CUSTOM_URI);
    	URI customUri = UriBuilder.fromUri("https://simplyrestful-testhost.org/services/")
    		.path(WEB_RESOURCE_PATH)
    		.build();
    	APICollection<TestResource> collection = target()
    		.path(WEB_RESOURCE_PATH)
    		.request()
    		.accept(MEDIA_TYPE_COLLECTION_V2_JSON_TYPE)
    		.header(HTTP_HEADER_NAME_CUSTOM_URI, customUri)
    		.get(new GenericType<APICollection<TestResource>>() {});
    	Assertions.assertEquals(customUri, collection.getSelf().getHref());
    	System.clearProperty(UriCustomizer.CONFIGURATION_PROPERTY_NAME);
    }

    @Test
    public void webResource_shouldUseCustomUriInTheServiceDocument_whenUriCustomizerPropertyAndHeaderAreProvided() {
    	System.setProperty(UriCustomizer.CONFIGURATION_PROPERTY_NAME, HTTP_HEADER_NAME_CUSTOM_URI);
    	URI customUri = UriBuilder.fromUri("https://simplyrestful-testhost.org/services/").build();
    	APIServiceDocument serviceDocument = target()
    		.request()
    		.header(HTTP_HEADER_NAME_CUSTOM_URI, customUri)
    		.get(APIServiceDocument.class);
    	Assertions.assertEquals(customUri, serviceDocument.getSelf().getHref());
    	System.clearProperty(UriCustomizer.CONFIGURATION_PROPERTY_NAME);
    }
}
