package simplyrestful.api.framework.core;

import java.net.URI;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
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

import simplyrestful.api.framework.core.providers.HALMapperProvider;
import simplyrestful.api.framework.core.providers.ObjectMapperProvider;
import simplyrestful.api.framework.core.servicedocument.WebResourceRoot;
import simplyrestful.api.framework.resources.HALServiceDocument;
import simplyrestful.api.framework.test.implementation.TestWebResource;

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
        	HALMapperProvider.class,
        	ObjectMapperProvider.class);
    }
    
    @Override
    protected void configureClient(ClientConfig config) {
	config.register(JacksonJsonProvider.class);
	config.register(HALMapperProvider.class);
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
	HALServiceDocument serviceDocument = target()
		.request()
		.get(HALServiceDocument.class);
	URI expected = UriBuilder.fromUri(getBaseUri()).path("openapi.json").build();
	URI actual = URI.create(serviceDocument.getDescribedBy().getHref());
	Assertions.assertEquals(expected, actual);
    }
}
