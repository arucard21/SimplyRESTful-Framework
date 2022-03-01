package simplyrestful.api.framework.client;

import java.net.URI;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import example.resources.jpa.ExampleComplexAttribute;
import example.resources.jpa.ExampleResource;
import simplyrestful.api.framework.webresource.api.implementation.DefaultCollectionGet;

/**
 * This e2e test requires the example API from "examples/springboot-jersey-nomapping-springdata" to be running
 *
 */
public class SimplyRESTfulClientApiTest {
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
    public void client_shouldDiscoverTheResourceURI_whenFirstAccessingTheApi() {
        Assertions.assertDoesNotThrow(() -> simplyRESTfulClient.listResources(-1, -1, Collections.emptyList(), "", Collections.emptyList()));
    }

    @Test
    public void listResources_shouldReturnListOfResources() {
        List<ExampleResource> listOfResources = simplyRESTfulClient.listResources(null, null, null);
        Assertions.assertNotNull(listOfResources);
        Assertions.assertEquals(simplyRESTfulClient.getTotalAmountOfLastRetrievedCollection(), listOfResources.size());
    }

    @Test
    public void listResources_shouldReturnListOfResourcesWithAllFields_whenFieldsIsAll() {
        List<ExampleResource> listOfResources = simplyRESTfulClient.listResources(Collections.singletonList(DefaultCollectionGet.QUERY_PARAM_FIELDS_ALL), null, null);
        Assertions.assertNotNull(listOfResources);
        Assertions.assertEquals(simplyRESTfulClient.getTotalAmountOfLastRetrievedCollection(), listOfResources.size());
        Assertions.assertNotNull(listOfResources.get(0));
        Assertions.assertNotNull(listOfResources.get(0).getSelf());
        Assertions.assertNotNull(listOfResources.get(0).getDescription());
        Assertions.assertNotNull(listOfResources.get(0).getDateTime());
        Assertions.assertNotNull(listOfResources.get(0).getComplexAttribute());
        Assertions.assertNotNull(listOfResources.get(0).getComplexAttribute().getName());
    }

    @Test
    public void create_shouldCreateTheResourceAndReturnTheCorrectLocationForThatResource() {
        ExampleResource resource = new ExampleResource();
        String description = "This is test resource";
        resource.setDescription(description);
        ExampleComplexAttribute complexAttribute = new ExampleComplexAttribute();
        String complexAttributeName = "complex attribute of test resource";
        complexAttribute.setName(complexAttributeName);
        resource.setComplexAttribute(complexAttribute);
        URI createdResourceLocation = simplyRESTfulClient.create(resource);
        ExampleResource createdResource = simplyRESTfulClient.read(createdResourceLocation);
        Assertions.assertEquals(resource.getDescription(), createdResource.getDescription());
        Assertions.assertEquals(resource.getComplexAttribute().getName(), createdResource.getComplexAttribute().getName());
        Assertions.assertNotNull(createdResource.getSelf());
        Assertions.assertNotNull(createdResource.getSelf().getHref());
        Assertions.assertFalse(createdResource.getSelf().getHref().isBlank());
        Assertions.assertEquals(createdResourceLocation, URI.create(createdResource.getSelf().getHref()));
        Assertions.assertDoesNotThrow(() -> simplyRESTfulClient.delete(createdResourceLocation));
        Assertions.assertFalse(simplyRESTfulClient.exists(createdResourceLocation));
    }

    @Test
    public void read_shouldReturnTheResource() {
        List<ExampleResource> listOfResources = simplyRESTfulClient.listResources(Collections.singletonList(DefaultCollectionGet.QUERY_PARAM_FIELDS_ALL), "", Collections.emptyList());
        ExampleResource resourceFromList = listOfResources.get(0);
        ExampleResource resourceFromOwnWebResource = simplyRESTfulClient.read(URI.create(resourceFromList.getSelf().getHref()));
        Assertions.assertEquals(resourceFromList.getSelf(), resourceFromOwnWebResource.getSelf());
        Assertions.assertEquals(resourceFromList.getDescription(), resourceFromOwnWebResource.getDescription());
        Assertions.assertEquals(resourceFromList.getComplexAttribute().getName(), resourceFromOwnWebResource.getComplexAttribute().getName());
    }

    @Test
    public void exists_shouldReturnWhetherTheResourceExists() {
        List<ExampleResource> listOfResources = simplyRESTfulClient.listResources(-1, -1, Collections.emptyList(), "", Collections.emptyList());
        Assertions.assertTrue(simplyRESTfulClient.exists(URI.create(listOfResources.get(0).getSelf().getHref())));
        Assertions.assertFalse(simplyRESTfulClient.exists(simplyRESTfulClient.createResourceUriFromUuid(UUID.randomUUID())));
    }

    @Test
    public void update_shouldUpdateTheResource() {
        List<ExampleResource> listOfResources = simplyRESTfulClient.listResources(Collections.singletonList(DefaultCollectionGet.QUERY_PARAM_FIELDS_ALL), "", Collections.emptyList());
        String modifiedDescription = "modified description";
        String modifiedComplexAttributeName = "modified name of complex attribute";
        ExampleResource modifiedResource = listOfResources.get(0);
        modifiedResource.setDescription(modifiedDescription);
        ExampleComplexAttribute modifiedComplexAttribute = modifiedResource.getComplexAttribute();
        modifiedComplexAttribute.setName(modifiedComplexAttributeName);
        modifiedResource.setComplexAttribute(modifiedComplexAttribute);
        Assertions.assertDoesNotThrow(() -> simplyRESTfulClient.update(modifiedResource));
        ExampleResource updatedResource = simplyRESTfulClient.read(modifiedResource.getSelf());
        Assertions.assertEquals(modifiedDescription, updatedResource.getDescription());
        Assertions.assertEquals(modifiedComplexAttributeName, updatedResource.getComplexAttribute().getName());
    }

    @Test
    public void delete_shouldRemoveTheResource() {
    	ExampleResource resource = new ExampleResource();
        String description = "This is test resource";
        resource.setDescription(description);
        ExampleComplexAttribute complexAttribute = new ExampleComplexAttribute();
        String complexAttributeName = "complex attribute of test resource";
        complexAttribute.setName(complexAttributeName);
        resource.setComplexAttribute(complexAttribute);
        resource.setDateTime(ZonedDateTime.now(ZoneOffset.UTC));
        URI createdResourceLocation = simplyRESTfulClient.create(resource);

        Assertions.assertTrue(simplyRESTfulClient.exists(createdResourceLocation));
        Assertions.assertDoesNotThrow(() -> simplyRESTfulClient.delete(createdResourceLocation));
        Assertions.assertFalse(simplyRESTfulClient.exists(createdResourceLocation));
    }
}
