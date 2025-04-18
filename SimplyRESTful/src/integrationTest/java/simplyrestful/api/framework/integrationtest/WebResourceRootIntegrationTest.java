package simplyrestful.api.framework.integrationtest;

import java.net.URI;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;

import simplyrestful.api.framework.integrationTest.implementation.TestWebResource;
import simplyrestful.api.framework.providers.ObjectMapperProvider;
import simplyrestful.api.framework.resources.ApiServiceDocument;
import simplyrestful.api.framework.servicedocument.WebResourceRoot;

@ExtendWith(MockitoExtension.class)
public class WebResourceRootIntegrationTest extends JerseyTest {
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
        return new ResourceConfig(
        	TestWebResource.class,
        	WebResourceRoot.class,
        	JacksonJsonProvider.class,
        	ObjectMapperProvider.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
	config.register(JacksonJsonProvider.class);
	config.register(ObjectMapperProvider.class);
    }

    @Test
    public void webResource_shouldReturnServiceDocument_whenGETReceivedOnRootURI() {
	Response response = target()
		.request()
		.get();
	Assertions.assertEquals(200, response.getStatus());

	String serviceDocument = response.readEntity(String.class);
	Assertions.assertTrue(serviceDocument.contains("describedBy"));
    }

    @Test
    public void webResource_shouldReturnServiceDocumentContainingLinkToOpenAPISpecification_whenGETReceivedOnRootURI() {
	ApiServiceDocument serviceDocument = target()
		.request()
		.get(ApiServiceDocument.class);
	URI expected = UriBuilder.fromUri(getBaseUri()).path("openapi.json").build();
	URI actual = serviceDocument.getDescribedBy().getHref();
	Assertions.assertEquals(expected, actual);
    }
}
