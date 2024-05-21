package simplyrestful.api.framework.client.integrationtest;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;

import io.swagger.v3.jaxrs2.integration.resources.AcceptHeaderOpenApiResource;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.GenericType;
import simplyrestful.api.framework.client.SimplyRESTfulClient;
import simplyrestful.api.framework.client.SimplyRESTfulClientFactory;
import simplyrestful.api.framework.client.integrationtest.integrationtest.implementation.TestResource;
import simplyrestful.api.framework.client.integrationtest.integrationtest.implementation.TestWebResource;
import simplyrestful.api.framework.filters.UriCustomizer;
import simplyrestful.api.framework.providers.ObjectMapperProvider;
import simplyrestful.api.framework.resources.APICollection;
import simplyrestful.api.framework.resources.Link;
import simplyrestful.api.framework.servicedocument.WebResourceRoot;

@ExtendWith(MockitoExtension.class)
public class SimplyRESTfulClientIntegrationTest extends JerseyTest {
    public static final UUID UUID_NIL = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final URI INVALID_RESOURCE_URI_DIFFERENT_HOST = URI
            .create("http://invalid-host/testresources/" + UUID_NIL.toString());
    public static final URI INVALID_RESOURCE_URI_DIFFERENT_PATH = URI
            .create(TestWebResource.getBaseUri() + "/different/path/testresources/" + UUID_NIL.toString());
    private SimplyRESTfulClient<TestResource> simplyRESTfulClient;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        configureSimplyRESTfulClient();
        TestWebResource.setBaseUri(getBaseUri());
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void configureSimplyRESTfulClient() {
    	Client testClient = client();
    	testClient.register(JacksonJsonProvider.class);
    	testClient.register(ObjectMapperProvider.class);
        simplyRESTfulClient = Assertions.assertDoesNotThrow(() -> new SimplyRESTfulClientFactory<TestResource>(testClient)
                .newClient(getBaseUri(), new GenericType<APICollection<TestResource>>() {}));
        Assertions.assertNotNull(simplyRESTfulClient, "The SimplyRESTful client could not be created correctly");
        simplyRESTfulClient.discoverResourceUri(null);
    }

    @Override
    protected Application configure() {
        ResourceConfig config = new ResourceConfig(
                TestWebResource.class,
                WebResourceRoot.class,
                ObjectMapperProvider.class,
                JacksonJsonProvider.class,
                UriCustomizer.class,
                OpenApiResource.class,
                AcceptHeaderOpenApiResource.class);
        config.property(ServerProperties.WADL_FEATURE_DISABLE, true);
        config.property(ServerProperties.FEATURE_AUTO_DISCOVERY_DISABLE, true);
        return config;
    }

    @Test
    public void client_shouldDiscoverTheResourceURI_whenItIsCreated() {
        Assertions.assertDoesNotThrow(() -> simplyRESTfulClient.listResources());
    }

    @Test
    public void listResources_shouldReturnTestResources() {
        List<TestResource> listOfResources = simplyRESTfulClient.listResources();
        Assertions.assertNotNull(listOfResources);
        Assertions.assertEquals(2, listOfResources.size());
        Assertions.assertTrue(listOfResources.contains(TestResource.testInstance()));
        Assertions.assertEquals(TestResource.ADDITIONAL_FIELD_TEST_VALUE, listOfResources.get(0).getAdditionalField());
        Assertions.assertEquals(TestResource.ADDITIONAL_FIELD_TEST_VALUE, listOfResources.get(1).getAdditionalField());
    }

    @Test
    public void streamResources_shouldReturnTestResources() {
        List<TestResource> listOfResources = simplyRESTfulClient.streamResourcesFromCollection(List.of(), "", List.of(), null, null, 1000);
        Assertions.assertNotNull(listOfResources);
        Assertions.assertEquals(2, listOfResources.size());
        Assertions.assertTrue(listOfResources.contains(TestResource.testInstance()));
        Assertions.assertEquals(TestResource.ADDITIONAL_FIELD_TEST_VALUE, listOfResources.get(0).getAdditionalField());
        Assertions.assertEquals(TestResource.ADDITIONAL_FIELD_TEST_VALUE, listOfResources.get(1).getAdditionalField());
    }

    @Test
    public void read_shouldReturnTestResource() {
        TestResource actual = simplyRESTfulClient.read(simplyRESTfulClient.createResourceUriFromUuid(TestResource.TEST_RESOURCE_ID));
        Assertions.assertEquals(TestResource.testInstance(), actual);
    }

    @Test
    public void read_shouldReturnTestResource_whenAnNonExistingIdIsUsed() {
        Assertions.assertThrows(NotFoundException.class, () -> simplyRESTfulClient.read(simplyRESTfulClient.createResourceUriFromUuid(UUID_NIL)));
    }

    @Test
    public void read_shouldThrowNullPointerException_whenProvidingNullAsArgument() {
        Assertions.assertThrows(NullPointerException.class, () -> simplyRESTfulClient.read((URI) null));
    }

    @Test
    public void create_shouldReturnTheIdOfTheCreatedTestResource() {
        URI createdResourceLocation = simplyRESTfulClient.create(new TestResource());
        Assertions.assertNotEquals(TestResource.TEST_RESOURCE_ID, simplyRESTfulClient.createResourceUuidFromUri(createdResourceLocation));
    }

    @Test
    public void create_shouldThrowNullPointerException_whenProvidingNullAsArgument() {
        Assertions.assertThrows(NullPointerException.class, () -> simplyRESTfulClient.create(null));
    }

    @Test
    public void exists_shouldReturnTrue_whenProvidingAnExistingIdAsArgument() {
        Assertions.assertTrue(simplyRESTfulClient.exists(simplyRESTfulClient.createResourceUriFromUuid(TestResource.TEST_RESOURCE_ID)));
    }

    @Test
    public void exists_shouldReturnFalse_whenProvidingAnNonExistingIdAsArgument() {
        Assertions.assertFalse(simplyRESTfulClient.exists(simplyRESTfulClient.createResourceUriFromUuid(UUID_NIL)));
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
        Assertions.assertDoesNotThrow(() -> simplyRESTfulClient.update(TestResource.testInstance()));
    }

    @Test
    public void update_shouldThrowIllegalArgumentException_whenTheProvidedResourceRefersToANonExistingId() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> simplyRESTfulClient.update(TestResource.withId(UUID_NIL)));
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
        invalidResource.setSelf(new Link(INVALID_RESOURCE_URI_DIFFERENT_HOST, null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> simplyRESTfulClient.update(invalidResource));
    }

    @Test
    public void update_shouldThrowIllegalArgumentException_whenResourceContainsAResourceUriWithDifferentPath() {
        TestResource invalidResource = new TestResource();
        invalidResource.setSelf(new Link(INVALID_RESOURCE_URI_DIFFERENT_PATH, null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> simplyRESTfulClient.update(invalidResource));
    }

    @Test
    public void delete_shouldReturnWithoutExceptions_whenAnExistingIdIsProvided() {
        Assertions.assertDoesNotThrow(() -> simplyRESTfulClient.delete(simplyRESTfulClient.createResourceUriFromUuid(TestResource.TEST_RESOURCE_ID)));
    }

    @Test
    public void delete_shouldThrowNotFoundException_whenANonExistingIdIsProvided() {
        Assertions.assertThrows(NotFoundException.class, () -> simplyRESTfulClient.delete(simplyRESTfulClient.createResourceUriFromUuid(UUID_NIL)));
    }

    @Test
    public void delete_shouldThrowNullPointerException_whenNullIsProvidedAsArgument() {
        Assertions.assertThrows(NullPointerException.class, () -> simplyRESTfulClient.delete((URI) null));
    }
}
