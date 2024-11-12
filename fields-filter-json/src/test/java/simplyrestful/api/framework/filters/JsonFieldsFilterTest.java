package simplyrestful.api.framework.filters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import jakarta.json.Json;
import jakarta.json.JsonStructure;
import simplyrestful.api.framework.outputstream.json.JsonFieldsFilterOutputStream;

public class JsonFieldsFilterTest {
	public static final String ARRAY_ORIGINAL_PLAIN_JSON = "/array/plain_original.json";
	public static final String ARRAY_ORIGINAL_HAL_JSON = "/array/hal_original.json";
	public static final String COLLECTION_ORIGINAL_PLAIN_JSON = "/collection/plain_original.json";
	public static final String COLLECTION_ORIGINAL_HAL_JSON = "/collection/hal_original.json";
	public static final String OPENAPI_ORIGINAL_JSON = "/openapi/original.json";
	public static final String RESOURCE_ORIGINAL_PLAIN_JSON = "/resource/plain_original.json";
	public static final String RESOURCE_ORIGINAL_HAL_JSON = "/resource/hal_original.json";

    private static JsonStructure loadTestJson(String testJson) throws IOException {
        try(InputStream collectionStream = JsonFieldsFilterTest.class.getResourceAsStream(testJson)){
        	Assertions.assertNotNull(collectionStream);
        	return Json.createReader(collectionStream).read();
        }
    }

    private static Stream<Arguments> getAllTestConfigurations(){
        return Stream.of(
                Arguments.of(ARRAY_ORIGINAL_PLAIN_JSON, ARRAY_ORIGINAL_PLAIN_JSON, null),
                Arguments.of(ARRAY_ORIGINAL_HAL_JSON, ARRAY_ORIGINAL_HAL_JSON, null),
                Arguments.of(COLLECTION_ORIGINAL_PLAIN_JSON, COLLECTION_ORIGINAL_PLAIN_JSON, null),
                Arguments.of(COLLECTION_ORIGINAL_HAL_JSON, COLLECTION_ORIGINAL_HAL_JSON, null),
                Arguments.of(OPENAPI_ORIGINAL_JSON, OPENAPI_ORIGINAL_JSON, null),
                Arguments.of(RESOURCE_ORIGINAL_PLAIN_JSON, RESOURCE_ORIGINAL_PLAIN_JSON, null),
                Arguments.of(RESOURCE_ORIGINAL_HAL_JSON, RESOURCE_ORIGINAL_HAL_JSON, null),
                Arguments.of(
                        ARRAY_ORIGINAL_PLAIN_JSON,
                        "/array/plain_fields_dateTime.json",
                        List.of("dateTime")),
                Arguments.of(
                        ARRAY_ORIGINAL_PLAIN_JSON,
                        "/array/plain_fields_description_self.type_complexAttribute.name.json",
                        List.of("description", "self.type", "complexAttribute.name")),
                Arguments.of(
                        ARRAY_ORIGINAL_PLAIN_JSON,
                        "/array/plain_fields_self.href.json",
                        List.of("self.href")),
                Arguments.of(
                        COLLECTION_ORIGINAL_PLAIN_JSON,
                        "/collection/plain_fields_item.description_item.complexAttribute.name.json",
                        List.of("item.description", "item.complexAttribute.name")),
                Arguments.of(COLLECTION_ORIGINAL_PLAIN_JSON,
                        "/collection/plain_fields_self_first_item.json",
                        List.of("self", "first", "item")),
                Arguments.of(COLLECTION_ORIGINAL_PLAIN_JSON,
                        "/collection/plain_fields_self_total.json",
                        List.of("self", "total")),
                Arguments.of(COLLECTION_ORIGINAL_HAL_JSON,
                        "/collection/hal_fields_embedded.item.description_embedded.item.complexAttribute.name.json",
                        List.of("_embedded.item.description", "_embedded.item.complexAttribute.name")),
                Arguments.of(COLLECTION_ORIGINAL_HAL_JSON,
                        "/collection/hal_fields_links.self_links.first_embedded.item.json",
                        List.of("_links.self", "_links.first", "_embedded.item")),
                Arguments.of(COLLECTION_ORIGINAL_HAL_JSON,
                        "/collection/hal_fields_links.self_total.json",
                        List.of("_links.self", "total")),
                Arguments.of(OPENAPI_ORIGINAL_JSON,
                        "/openapi/fields_openapi_tags.json",
                        List.of("openapi", "tags")),
                Arguments.of(OPENAPI_ORIGINAL_JSON,
                        "/openapi/fields_paths.resources.get.json",
                        List.of("paths./resources.get")),
                Arguments.of(OPENAPI_ORIGINAL_JSON,
                        "/openapi/fields_paths.json",
                        List.of("paths")),
                Arguments.of(OPENAPI_ORIGINAL_JSON,
                        "/openapi/fields_tags.json",
                        List.of("tags")),
                Arguments.of(OPENAPI_ORIGINAL_JSON,
                        "/openapi/fields_paths.resourcesid.get.parameters.name.json",
                        List.of("paths./resources/{id}.get.parameters.name")),
                Arguments.of(RESOURCE_ORIGINAL_HAL_JSON,
                        "/resource/hal_fields_complexAttribute_dateTime.json",
                        List.of("complexAttribute", "dateTime")),
                Arguments.of(RESOURCE_ORIGINAL_HAL_JSON,
                        "/resource/hal_fields_links.self.href_links.self.profile.json",
                        List.of("_links.self.href", "_links.self.profile")),
                Arguments.of(RESOURCE_ORIGINAL_HAL_JSON,
                        "/resource/hal_fields_links.self.profile.json",
                        List.of("_links.self.profile"))
                );
    }

    @ParameterizedTest
    @MethodSource("getAllTestConfigurations")
    public void testFieldsFilter(String originalJsonFile, String filteredJsonFile, List<String> fields) throws IOException {
        JsonStructure original = loadTestJson(originalJsonFile);
        JsonStructure filtered = loadTestJson(filteredJsonFile);
        String actualString = new JsonFieldsFilter().filterFieldsInJson(original.toString(), fields);
        JsonStructure actualJson = Json.createReader(new StringReader(actualString)).read();
        Assertions.assertEquals(filtered, actualJson);
    }

    @Disabled("Broken when run with Gradle but works in IDE")
    @ParameterizedTest
    @MethodSource("getAllTestConfigurations")
    public void testFieldsFilterOutputStream(String originalJsonFile, String filteredJsonFile, List<String> fields) throws IOException {
    	try(
    			InputStream originalJsonInputStream = JsonFieldsFilterTest.class.getResourceAsStream(originalJsonFile);
    			ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
    			JsonFieldsFilterOutputStream jsonFieldsFilterOutputStream = new JsonFieldsFilterOutputStream(byteOutputStream, fields);
    	){
    		Assertions.assertNotNull(originalJsonInputStream);

    		// FIXME When this test is run in Gradle, some of the OpenAPI-based JSON files are empty when loaded. Those same files
    		// are fine in the test above. Needs to be looked into further.
    		originalJsonInputStream.transferTo(jsonFieldsFilterOutputStream);
    		String filteredJsonString = byteOutputStream.toString(StandardCharsets.UTF_8);
    		JsonStructure filteredJsonStructure = Json.createReader(new StringReader(filteredJsonString)).read();
    		JsonStructure expectedFilteredJsonStructure = loadTestJson(filteredJsonFile);
    		Assertions.assertEquals(expectedFilteredJsonStructure, filteredJsonStructure);
    	}
    }
}
