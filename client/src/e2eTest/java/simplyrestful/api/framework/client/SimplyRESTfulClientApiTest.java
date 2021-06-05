package simplyrestful.api.framework.client;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import example.resources.jpa.ExampleComplexAttribute;
import example.resources.jpa.ExampleResource;

/**
 * This e2e test requires the example API from "examples/springboot-jersey-nomapping-springdata" to be running
 *
 */
@ExtendWith(MockitoExtension.class)
public class SimplyRESTfulClientApiTest {
//    private static final UUID UUID_NIL = UUID.fromString("00000000-0000-0000-0000-000000000000");
//    private static final URI INVALID_RESOURCE_URI_DIFFERENT_HOST = URI
//            .create("http://invalid-host/testresources/" + UUID_NIL.toString());
//    private static final URI INVALID_RESOURCE_URI_DIFFERENT_PATH = URI
//            .create(TestWebResource.getBaseUri() + "/different/path/testresources/" + UUID_NIL.toString());

    private static URI baseUri;
    private static SimplyRESTfulClient<ExampleResource> simplyRESTfulClient;
    private static Client client = ClientBuilder.newClient();

    @BeforeAll
    public static void createClient() {
        baseUri = URI.create("http://localhost:8888");
        configureSimplyRESTfulClient();
    }

    public static void configureSimplyRESTfulClient() {
        simplyRESTfulClient = Assertions.assertDoesNotThrow(
                () -> new SimplyRESTfulClientFactory<ExampleResource>(client).newClient(baseUri, ExampleResource.class));
        Assertions.assertNotNull(simplyRESTfulClient, "The SimplyRESTful client could not be created correctly");
    }

    @Test
    public void client_shouldDiscoverTheResourceURI_whenItIsCreated() {
        Assertions.assertDoesNotThrow(() -> simplyRESTfulClient.listResources(-1, -1, Collections.emptyList(), "", Collections.emptyList()));
    }

    @Test
    public void listResources_shouldReturnTestResources() {
        List<ExampleResource> listOfResources = simplyRESTfulClient.listResources(-1, -1, "", "", "");
        Assertions.assertNotNull(listOfResources);
        Assertions.assertEquals(3, listOfResources.size());
    }

    @Test
    public void read_shouldReturnTestResource() {
        List<ExampleResource> listOfResources = simplyRESTfulClient.listResources(-1, -1, Collections.emptyList(), "", Collections.emptyList());
        ExampleResource resourceFromList = listOfResources.get(0);
        ExampleResource resourceFromOwnWebResource = simplyRESTfulClient.read(URI.create(resourceFromList.getSelf().getHref()));
        Assertions.assertEquals(resourceFromList.getSelf(), resourceFromOwnWebResource.getSelf());
        Assertions.assertEquals(resourceFromList.getDescription(), resourceFromOwnWebResource.getDescription());
        Assertions.assertEquals(resourceFromList.getComplexAttribute().getName(), resourceFromOwnWebResource.getComplexAttribute().getName());
    }

    @Disabled("FIXME create currently returns 400 Bad Request")
    @Test
    public void create_shouldReturnTheIdOfTheCreatedTestResource() {
        ExampleResource resource = new ExampleResource();
        String description = "This is test resource";
        resource.setDescription(description);
        ExampleComplexAttribute complexAttribute = new ExampleComplexAttribute();
        String complexAttributeName = "complex attribute of test resource";
        complexAttribute.setName(complexAttributeName);
        resource.setComplexAttribute(complexAttribute);
        URI createdResourceLocation = simplyRESTfulClient.create(resource);
        ExampleResource createdResource = simplyRESTfulClient.read(createdResourceLocation);
        Assertions.assertNotEquals(resource, createdResource);
    }

//    @Test
//    public void create_shouldThrowNullPointerException_whenProvidingNullAsArgument() {
//        Assertions.assertThrows(NullPointerException.class, () -> simplyRESTfulClient.create(null));
//    }
//
//    @Test
//    public void exists_shouldReturnTrue_whenProvidingAnExistingIdAsArgument() {
//        Assertions.assertTrue(simplyRESTfulClient.exists(TestResource.TEST_RESOURCE_ID));
//    }
//
//    @Test
//    public void exists_shouldReturnFalse_whenProvidingAnNonExistingIdAsArgument() {
//        Assertions.assertFalse(simplyRESTfulClient.exists(UUID_NIL));
//    }
//
//    @Test
//    public void exists_shouldReturnTrue_whenProvidingAnExistingResourceUriAsArgument() {
//        Assertions.assertTrue(simplyRESTfulClient.exists(TestResource.getResourceUri(TestResource.TEST_RESOURCE_ID)));
//    }
//
//    @Test
//    public void exists_shouldReturnFalse_whenProvidingAnNonExistingResourceUriAsArgument() {
//        Assertions.assertFalse(simplyRESTfulClient.exists(TestResource.getResourceUri(UUID_NIL)));
//    }
//
//    @Test
//    public void update_shouldReturnWithoutExceptions_whenTheProvidedResourceRefersToAnExistingId() {
//        Assertions.assertDoesNotThrow(() -> simplyRESTfulClient.update(TestResource.testInstance()));
//    }
//
//    @Test
//    public void update_shouldThrowIllegalArgumentException_whenTheProvidedResourceRefersToANonExistingId() {
//        Assertions.assertThrows(IllegalArgumentException.class,
//                () -> simplyRESTfulClient.update(TestResource.withId(UUID_NIL)));
//    }
//
//    @Test
//    public void update_shouldThrowWebApplicationException_whenTheServerReturnsAnErrorResponse() {
//        Assertions.assertThrows(IllegalArgumentException.class,
//                () -> simplyRESTfulClient.update(TestResource.withId(TestWebResource.ERROR_UPDATE_RESOURCE_ID)));
//    }
//
//    @Test
//    public void update_shouldThrowNullPointerException_whenNullIsProvidedAsArgument() {
//        Assertions.assertThrows(NullPointerException.class, () -> simplyRESTfulClient.update(null));
//    }
//
//    @Test
//    public void update_shouldThrowIllegalArgumentException_whenResourceContainsAResourceUriWithDifferentHostname() {
//        TestResource invalidResource = new TestResource();
//        invalidResource.setSelf(new HALLink.Builder(INVALID_RESOURCE_URI_DIFFERENT_HOST).build());
//        Assertions.assertThrows(IllegalArgumentException.class, () -> simplyRESTfulClient.update(invalidResource));
//    }
//
//    @Test
//    public void update_shouldThrowIllegalArgumentException_whenResourceContainsAResourceUriWithDifferentPath() {
//        TestResource invalidResource = new TestResource();
//        invalidResource.setSelf(new HALLink.Builder(INVALID_RESOURCE_URI_DIFFERENT_PATH).build());
//        Assertions.assertThrows(IllegalArgumentException.class, () -> simplyRESTfulClient.update(invalidResource));
//    }
//
//    @Test
//    public void delete_shouldReturnWithoutExceptions_whenAnExistingIdIsProvided() {
//        Assertions.assertDoesNotThrow(() -> simplyRESTfulClient.delete(TestResource.TEST_RESOURCE_ID));
//    }
//
//    @Test
//    public void delete_shouldThrowIllegalArgumentException_whenANonExistingIdIsProvided() {
//        Assertions.assertThrows(WebApplicationException.class, () -> simplyRESTfulClient.delete(UUID_NIL));
//    }
//
//    @Test
//    public void delete_shouldThrowNullPointerException_whenNullIsProvidedAsArgument() {
//        Assertions.assertThrows(NullPointerException.class, () -> simplyRESTfulClient.delete(null));
//    }
}
