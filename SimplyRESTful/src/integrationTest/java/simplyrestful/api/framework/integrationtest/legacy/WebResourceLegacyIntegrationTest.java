package simplyrestful.api.framework.integrationtest.legacy;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import simplyrestful.api.framework.filters.UriCustomizer;
import simplyrestful.api.framework.integrationtest.WebResourceIntegrationTest;
import simplyrestful.api.framework.legacy.HALCollectionV1;
import simplyrestful.api.framework.providers.JacksonHALJsonProvider;
import simplyrestful.api.framework.providers.ObjectMapperProvider;
import simplyrestful.api.framework.servicedocument.WebResourceRoot;
import simplyrestful.api.framework.test.implementation.TestResource;
import simplyrestful.api.framework.test.legacy.LegacyWebResource;

@SuppressWarnings("deprecation")
public class WebResourceLegacyIntegrationTest extends JerseyTest {
	public static final String QUERY_PARAM_COMPACT = "compact";
    public static final MediaType MEDIA_TYPE_HALCOLLECTION_V1_HAL_JSON_TYPE = MediaType.valueOf(HALCollectionV1.MEDIA_TYPE_HAL_JSON);

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
        LegacyWebResource.TEST_RESOURCES.add(testInstance);
        testInstanceWithRandomId = TestResource.random(getBaseUri());
        LegacyWebResource.TEST_RESOURCES.add(testInstanceWithRandomId);
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        clearTestResources();
    }

    private void clearTestResources() {
    	LegacyWebResource.TEST_RESOURCES.clear();
    }

    @Override
    protected Application configure() {
        ResourceConfig config = new ResourceConfig(
        	LegacyWebResource.class,
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
    public void webResource_shouldReturnHALV1CollectionWithLinkedResourcesByDefault_whenV1IsRequested() {
    	Response response = target()
    		.path(WebResourceIntegrationTest.WEB_RESOURCE_PATH)
    		.queryParam(QUERY_PARAM_COMPACT, true)
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
    		.path(WebResourceIntegrationTest.WEB_RESOURCE_PATH)
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
}
