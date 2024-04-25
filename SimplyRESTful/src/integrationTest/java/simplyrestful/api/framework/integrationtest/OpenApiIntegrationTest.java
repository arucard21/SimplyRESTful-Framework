package simplyrestful.api.framework.integrationtest;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.jaxrs2.integration.resources.AcceptHeaderOpenApiResource;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.responses.ApiResponse;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import simplyrestful.api.framework.filters.UriCustomizer;
import simplyrestful.api.framework.providers.ObjectMapperProvider;
import simplyrestful.api.framework.resources.APICollection;
import simplyrestful.api.framework.resources.APIResource;
import simplyrestful.api.framework.servicedocument.WebResourceRoot;
import simplyrestful.api.framework.swagger.SimplyRESTfulOpenApiFilter;

public class OpenApiIntegrationTest extends JerseyTest {
    public static final String OPENAPI_PATH_JSON = "openapi.json";

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
                WebResourceRoot.class,
                JacksonJsonProvider.class,
                UriCustomizer.class,
                OpenApiResource.class,
                AcceptHeaderOpenApiResource.class,
                ObjectMapperProvider.class);

        return config;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(ObjectMapperProvider.class);
        config.register(JacksonJsonProvider.class);
    }

    @Test
    public void openApi_shouldNotContainASchemaForTheAPIResourceParent() throws JsonMappingException, JsonProcessingException {
        Assertions.assertFalse(retrieveOpenAPI().getComponents().getSchemas().containsKey(APIResource.class.getSimpleName()));
    }

    @Test
    public void openApi_shouldNotContainASchemaForTheAPICollectionParent() throws JsonMappingException, JsonProcessingException {
        Assertions.assertFalse(retrieveOpenAPI().getComponents().getSchemas().containsKey(APICollection.class.getSimpleName()));
    }

    @Test
    public void openApi_shouldNotContainASchemaForTheAPIResourceTypedAPICollection() throws JsonMappingException, JsonProcessingException {
    	Assertions.assertFalse(retrieveOpenAPI().getComponents().getSchemas().containsKey(APICollection.class.getSimpleName()+APIResource.class.getSimpleName()));
    }

    @Test
    public void openApi_shouldNotContainMediaTypesWithQsParameterName() throws JsonMappingException, JsonProcessingException {
        List<String> allMediaTypeParameterNames = retrieveOpenAPI()
                .getPaths().values().stream()
                .flatMap(pathItem -> pathItem.readOperations().stream())
                .flatMap(operation -> operation.getResponses().values().stream())
                .map(ApiResponse::getContent)
                .filter(Objects::nonNull)
                .flatMap(content -> content.keySet().stream())
                .map(MediaType::valueOf)
                .flatMap(mediaType -> mediaType.getParameters().keySet().stream())
                .collect(Collectors.toList());
        Assertions.assertFalse(allMediaTypeParameterNames.contains(SimplyRESTfulOpenApiFilter.MEDIA_TYPE_PARAMETER_NAME_QS));
    }

    private OpenAPI retrieveOpenAPI() throws JsonMappingException, JsonProcessingException {
        Response response = target().path(OPENAPI_PATH_JSON).request().get();
        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        String openApiString = response.readEntity(String.class);
        // OpenAPI 3.x requires a custom-configured ObjectMapper (provided by Swagger) to deserialize correctly to the OpenAPI class
        OpenAPI openApi = Json.mapper().readValue(openApiString, OpenAPI.class);
        return openApi;
    }
}
