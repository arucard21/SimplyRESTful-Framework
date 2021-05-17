package simplyrestful.api.framework.client;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import io.openapitools.jackson.dataformat.hal.HALLink;
import io.swagger.v3.jaxrs2.integration.resources.AcceptHeaderOpenApiResource;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import simplyrestful.api.framework.client.test.implementation.TestResource;
import simplyrestful.api.framework.client.test.implementation.TestWebResource;
import simplyrestful.api.framework.core.filters.UriCustomizer;
import simplyrestful.api.framework.core.providers.JacksonHALJsonProvider;
import simplyrestful.api.framework.core.providers.ObjectMapperProvider;
import simplyrestful.api.framework.core.servicedocument.WebResourceRoot;

@SuppressWarnings("deprecation")
@ExtendWith(MockitoExtension.class)
public class SimplyRESTfulClientTest extends JerseyTest {
    private static final UUID UUID_NIL = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static final URI INVALID_RESOURCE_URI_DIFFERENT_HOST = URI.create("http://invalid-host/testresources/" + UUID_NIL.toString());
    private static final URI INVALID_RESOURCE_URI_DIFFERENT_PATH = URI.create(TestWebResource.getBaseUri() + "/different/path/testresources/" + UUID_NIL.toString());
    private static SimplyRESTfulClient<TestResource> simplyRESTfulClient;

    private static TestResource testResource;
    private static TestResource testResourceRandom;

    public static TestResource getTestResource() {
        return testResource;
    }

    public static TestResource getTestResourceRandom() {
        return testResourceRandom;
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
	super.setUp();
	configureSimplyRESTfulClient();
	TestWebResource.setBaseUri(getBaseUri());
	testResource = TestResource.testInstance();
	testResourceRandom = TestResource.random();
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
	super.tearDown();
    }

    public void configureSimplyRESTfulClient() {
	simplyRESTfulClient = Assertions.assertDoesNotThrow(
		() -> new SimplyRESTfulClientFactory<TestResource>(client()).newClient(getBaseUri(), TestResource.class));
	Assertions.assertNotNull(simplyRESTfulClient, "The SimplyRESTful client could not be created correctly");
    }

    @Override
    protected Application configure() {
	ResourceConfig config = new ResourceConfig(
		TestWebResource.class,
		WebResourceRoot.class,
		ObjectMapperProvider.class,
		JacksonJsonProvider.class,
		JacksonHALJsonProvider.class,
		UriCustomizer.class,
		OpenApiResource.class,
		AcceptHeaderOpenApiResource.class);
	config.property(ServerProperties.WADL_FEATURE_DISABLE, true);
	config.property(ServerProperties.FEATURE_AUTO_DISCOVERY_DISABLE, true);
	return config;
    }

    @Test
    public void client_shouldDiscoverTheResourceURI_whenItIsCreated() {
	Assertions.assertDoesNotThrow(() -> simplyRESTfulClient.listResources(-1, -1, "", "", ""));
    }

    @Test
    public void listResources_shouldReturnTestResources() {
	List<TestResource> listOfResourceIdentifiers = simplyRESTfulClient.listResources(-1, -1, "", "", "");
	Assertions.assertNotNull(listOfResourceIdentifiers);
	Assertions.assertEquals(2, listOfResourceIdentifiers.size());
	Assertions.assertTrue(listOfResourceIdentifiers.contains(testResource));
    }

    @Test
    public void read_shouldReturnTestResource() {
	TestResource actual = simplyRESTfulClient.read(TestResource.TEST_RESOURCE_ID);
	Assertions.assertEquals(testResource, actual);
    }

    @Test
    public void read_shouldReturnTestResource_whenAnNonExistingIdIsUsed() {
	Assertions.assertThrows(NotFoundException.class, () -> simplyRESTfulClient.read(UUID_NIL));
    }

    @Test
    public void read_shouldThrowNullPointerException_whenProvidingNullAsArgument() {
	Assertions.assertThrows(NullPointerException.class, () -> simplyRESTfulClient.read(null));
    }

    @Test
    public void create_shouldReturnTheIdOfTheCreatedTestResource() {
	UUID actual = simplyRESTfulClient.create(new TestResource());
	Assertions.assertNotEquals(TestResource.TEST_RESOURCE_ID, actual);
    }

    @Test
    public void create_shouldThrowNullPointerException_whenProvidingNullAsArgument() {
	Assertions.assertThrows(NullPointerException.class, () -> simplyRESTfulClient.create(null));
    }

    @Test
    public void exists_shouldReturnTrue_whenProvidingAnExistingIdAsArgument() {
	Assertions.assertTrue(simplyRESTfulClient.exists(TestResource.TEST_RESOURCE_ID));
    }

    @Test
    public void exists_shouldReturnFalse_whenProvidingAnNonExistingIdAsArgument() {
	Assertions.assertFalse(simplyRESTfulClient.exists(UUID_NIL));
    }

    @Test
    public void exists_shouldReturnTrue_whenProvidingAnExistingResourceUriAsArgument() {
	Assertions.assertTrue(simplyRESTfulClient.exists(TestResource.getResourceUri(TestResource.TEST_RESOURCE_ID)));
    }

    @Test
    public void exists_shouldReturnFalse_whenProvidingAnNonExistingResourceUriAsArgument() {
	Assertions.assertFalse(simplyRESTfulClient.exists(TestResource.getResourceUri(UUID_NIL)));
    }

    @Test
    public void update_shouldReturnWithoutExceptions_whenTheProvidedResourceRefersToAnExistingId() {
	Assertions.assertDoesNotThrow(() -> simplyRESTfulClient.update(testResource));
    }

    @Test
    public void update_shouldThrowIllegalArgumentException_whenTheProvidedResourceRefersToANonExistingId() {
	Assertions.assertThrows(IllegalArgumentException.class, () -> simplyRESTfulClient.update(TestResource.withId(UUID_NIL)));
    }

    @Test
    public void update_shouldThrowWebApplicationException_whenTheServerReturnsAnErrorResponse() {
	Assertions.assertThrows(IllegalArgumentException.class,
		() -> simplyRESTfulClient.update(TestResource.withId(TestWebResource.ERROR_UPDATE_RESOURCE_ID)));
    }

    @Test
    public void update_shouldThrowNullPointerException_whenNullIsProvidedAsArgument() {
	Assertions.assertThrows(NullPointerException.class, () -> simplyRESTfulClient.update(null));
    }

    @Test
    public void update_shouldThrowIllegalArgumentException_whenResourceContainsAResourceUriWithDifferentHostname() {
	TestResource invalidResource = new TestResource();
	invalidResource.setSelf(new HALLink.Builder(INVALID_RESOURCE_URI_DIFFERENT_HOST).build());
	Assertions.assertThrows(IllegalArgumentException.class, () -> simplyRESTfulClient.update(invalidResource));
    }

    @Test
    public void update_shouldThrowIllegalArgumentException_whenResourceContainsAResourceUriWithDifferentPath() {
	TestResource invalidResource = new TestResource();
	invalidResource.setSelf(new HALLink.Builder(INVALID_RESOURCE_URI_DIFFERENT_PATH).build());
	Assertions.assertThrows(IllegalArgumentException.class, () -> simplyRESTfulClient.update(invalidResource));
    }

    @Test
    public void delete_shouldReturnWithoutExceptions_whenAnExistingIdIsProvided() {
	Assertions.assertDoesNotThrow(() -> simplyRESTfulClient.delete(TestResource.TEST_RESOURCE_ID));
    }

    @Test
    public void delete_shouldThrowIllegalArgumentException_whenANonExistingIdIsProvided() {
	Assertions.assertThrows(WebApplicationException.class, () -> simplyRESTfulClient.delete(UUID_NIL));
    }

    @Test
    public void delete_shouldThrowNullPointerException_whenNullIsProvidedAsArgument() {
	Assertions.assertThrows(NullPointerException.class, () -> simplyRESTfulClient.delete(null));
    }
}
