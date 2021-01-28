package simplyrestful.api.framework.core;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import simplyrestful.api.framework.core.filters.UriCustomizer;
import simplyrestful.api.framework.core.implementation.TestWebResourcePreferLegacy;
import simplyrestful.api.framework.core.providers.JacksonHALJsonProvider;
import simplyrestful.api.framework.core.providers.ObjectMapperProvider;
import simplyrestful.api.framework.core.servicedocument.WebResourceRoot;
import simplyrestful.api.framework.resources.HALCollectionV1;

@SuppressWarnings("deprecation")
public class WebResourcePreferLegacyIntegrationTest extends JerseyTest {
    private static final String WEB_RESOURCE_PATH = "testresources";
    private static final MediaType MEDIA_TYPE_HALCOLLECTION_V1_HAL_JSON_TYPE = MediaType.valueOf(HALCollectionV1.MEDIA_TYPE_HAL_JSON);

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
        	TestWebResourcePreferLegacy.class,
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
    public void webResourceThatPrefersLegacyCollection_shouldReturnLegacyHALV1CollectionAsDefault_whenNoSpecificVersionIsRequested() {
    	Response response = target().path(WEB_RESOURCE_PATH).request().get();
    	Assertions.assertEquals(200, response.getStatus());
    	Assertions.assertEquals(MEDIA_TYPE_HALCOLLECTION_V1_HAL_JSON_TYPE, response.getMediaType());
    }
}
