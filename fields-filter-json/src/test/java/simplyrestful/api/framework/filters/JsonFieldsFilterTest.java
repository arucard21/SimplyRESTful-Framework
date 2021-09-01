package simplyrestful.api.framework.filters;

import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonStructure;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class JsonFieldsFilterTest {
    private static final String COLLECTION_ORIGINAL_PLAIN_JSON = "/collection/plain_original.json";
    private static final String COLLECTION_ORIGINAL_HAL_JSON = "/collection/hal_original.json";
    private static final String RESOURCE_ORIGINAL_PLAIN_JSON = "/resource/plain_original.json";
    private static final String RESOURCE_ORIGINAL_HAL_JSON = "/resource/hal_original.json";

    private static JsonStructure loadTestJson(String testJson) {
        InputStream collectionStream = JsonFieldsFilterTest.class.getResourceAsStream(testJson);
        Assertions.assertNotNull(collectionStream);
        return Json.createReader(collectionStream).read();
    }

    private static Stream<Arguments> getAllTestConfigurations(){
        return Stream.of(
                Arguments.of(COLLECTION_ORIGINAL_PLAIN_JSON, COLLECTION_ORIGINAL_PLAIN_JSON, null),
                Arguments.of(COLLECTION_ORIGINAL_HAL_JSON, COLLECTION_ORIGINAL_HAL_JSON, null),
                Arguments.of(RESOURCE_ORIGINAL_PLAIN_JSON, RESOURCE_ORIGINAL_PLAIN_JSON, null),
                Arguments.of(RESOURCE_ORIGINAL_HAL_JSON, RESOURCE_ORIGINAL_HAL_JSON, null),
//                Arguments.of(COLLECTION_ORIGINAL_PLAIN_JSON, "/collection/plain_fields_self_total.json", List.of("self", "total")),
                Arguments.of(COLLECTION_ORIGINAL_HAL_JSON, "/collection/hal_fields_self_total.json", List.of("_links.self", "total")));
    }

    @ParameterizedTest
    @MethodSource("getAllTestConfigurations")
    public void testFieldsFilter(String originalJsonFile, String filteredJsonFile, List<String> fields) {
        JsonStructure original = loadTestJson(originalJsonFile);
        JsonStructure filtered = loadTestJson(filteredJsonFile);
        String actualString = new JsonFieldsFilter().filterFieldsInJson(original.toString(), fields);
        JsonStructure actualJson = Json.createReader(new StringReader(actualString)).read();
        Assertions.assertEquals(filtered, actualJson);
    }
}