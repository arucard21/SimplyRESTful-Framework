package simplyrestful.api.framework.test;

import java.util.List;

import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import simplyrestful.api.framework.MediaTypeUtils;

public class MediaTypeUtilsTest {
    private static final MediaType MEDIATYPE_WITH_PARAMETER_V2 = MediaType.valueOf("application/hal+json;profile=\"http://some-host.local/resource-name/v2\"");
    private static final MediaType MEDIATYPE_CUSTOM_NO_PARAMETER_V2 = MediaType.valueOf("application/x.simplyrestful-resource-name-v2+json");
    private static final MediaType MEDIATYPE_WITH_PARAMETER_V1 = MediaType.valueOf("application/hal+json;profile=\"http://some-host.local/resource-name/v1\"");

	@Test
    public void selectMediaType_shouldReturnMediaTypeWithParameterV2_whenMediaTypeWithParameterV2IsProducible() {
    	MediaType selected = MediaTypeUtils.selectMediaType(
    			List.of(MEDIATYPE_WITH_PARAMETER_V2),
    			List.of(MEDIATYPE_WITH_PARAMETER_V2));
    	Assertions.assertEquals(MEDIATYPE_WITH_PARAMETER_V2, selected);
    }

	@Test
    public void selectMediaType_shouldReturnMediaTypeWithParameterV2_whenMediaTypeWithParameterV2AndMoreAreProducible() {
    	MediaType selected = MediaTypeUtils.selectMediaType(
    			List.of(MEDIATYPE_WITH_PARAMETER_V1, MEDIATYPE_WITH_PARAMETER_V2, MEDIATYPE_CUSTOM_NO_PARAMETER_V2),
    			List.of(MEDIATYPE_WITH_PARAMETER_V2));
    	Assertions.assertEquals(MEDIATYPE_WITH_PARAMETER_V2, selected);
    }

	@Test
    public void selectMediaType_shouldReturnMediaTypeWithParameterV1_whenMediaTypeWithParameterV1AndMoreAreProducible() {
    	MediaType selected = MediaTypeUtils.selectMediaType(
    			List.of(MEDIATYPE_WITH_PARAMETER_V1, MEDIATYPE_WITH_PARAMETER_V2, MEDIATYPE_CUSTOM_NO_PARAMETER_V2),
    			List.of(MEDIATYPE_WITH_PARAMETER_V1));
    	Assertions.assertEquals(MEDIATYPE_WITH_PARAMETER_V1, selected);
    }

	@Test
    public void selectMediaType_shouldReturnMediaTypeCustomNoParameterV2_whenMediaTypeCustomNoParameterV2AndMoreAreProducible() {
    	MediaType selected = MediaTypeUtils.selectMediaType(
    			List.of(MEDIATYPE_WITH_PARAMETER_V1, MEDIATYPE_WITH_PARAMETER_V2, MEDIATYPE_CUSTOM_NO_PARAMETER_V2),
    			List.of(MEDIATYPE_CUSTOM_NO_PARAMETER_V2));
    	Assertions.assertEquals(MEDIATYPE_CUSTOM_NO_PARAMETER_V2, selected);
    }

	@Test
    public void selectMediaType_shouldReturnFirstRequestedMediaType_whenMultipleMediaTypeWithEqualQAndQsValueAreRequested() {
    	MediaType selected = MediaTypeUtils.selectMediaType(
    			List.of(MEDIATYPE_WITH_PARAMETER_V1, MEDIATYPE_WITH_PARAMETER_V2, MEDIATYPE_CUSTOM_NO_PARAMETER_V2),
    			List.of(MEDIATYPE_WITH_PARAMETER_V1, MEDIATYPE_CUSTOM_NO_PARAMETER_V2));
    	Assertions.assertEquals(MEDIATYPE_WITH_PARAMETER_V1, selected);
    }

	@Test
    public void selectMediaType_shouldReturn406NotAcceptable_whenMediaTypeWithParameterV1IsRequestedButNotProducible() {
    	Assertions.assertThrows(NotAcceptableException.class, () -> MediaTypeUtils.selectMediaType(
    			List.of(MEDIATYPE_WITH_PARAMETER_V2, MEDIATYPE_CUSTOM_NO_PARAMETER_V2),
    			List.of(MEDIATYPE_WITH_PARAMETER_V1)));
    }

	@Test
    public void selectMediaType_shouldReturn406NotAcceptable_whenMediaTypeCustomNoParameterV2IsRequestedButNotProducible() {
    	Assertions.assertThrows(NotAcceptableException.class, () -> MediaTypeUtils.selectMediaType(
    			List.of(MEDIATYPE_WITH_PARAMETER_V1, MEDIATYPE_WITH_PARAMETER_V2),
    			List.of(MEDIATYPE_CUSTOM_NO_PARAMETER_V2)));
    }

	@Test
    public void selectMediaType_shouldReturnMediaTypeWithHighestQsValue_whenWildcardMediaTypeRequested() {
    	MediaType selected = MediaTypeUtils.selectMediaType(
    			List.of(
    					MediaType.valueOf(MEDIATYPE_WITH_PARAMETER_V1.toString() + ";qs=0.1"),
    					MediaType.valueOf(MEDIATYPE_WITH_PARAMETER_V2.toString() + ";qs=0.7"),
    					MediaType.valueOf(MEDIATYPE_CUSTOM_NO_PARAMETER_V2.toString() + ";qs=0.9")),
    			List.of(MediaType.WILDCARD_TYPE));
    	Assertions.assertEquals(MEDIATYPE_CUSTOM_NO_PARAMETER_V2, selected);
    }

	@Test
    public void selectMediaType_shouldReturnMediaTypeWithHighestQsValue_whenNoSpecificMediaTypeRequested() {
    	MediaType selected = MediaTypeUtils.selectMediaType(
    			List.of(
    					MediaType.valueOf(MEDIATYPE_WITH_PARAMETER_V1.toString() + ";qs=0.1"),
    					MediaType.valueOf(MEDIATYPE_WITH_PARAMETER_V2.toString() + ";qs=0.7"),
    					MediaType.valueOf(MEDIATYPE_CUSTOM_NO_PARAMETER_V2.toString() + ";qs=0.9")),
    			List.of());
    	Assertions.assertEquals(MEDIATYPE_CUSTOM_NO_PARAMETER_V2, selected);
    }

	@Test
    public void selectMediaType_shouldReturnMediaTypeWithHighestQsValueFromTheMatchingSubset_whenASpecificMediaTypeIsRequested() {
    	MediaType selected = MediaTypeUtils.selectMediaType(
    			List.of(
    					MediaType.valueOf(MEDIATYPE_WITH_PARAMETER_V1.toString() + ";qs=0.1"),
    					MediaType.valueOf(MEDIATYPE_WITH_PARAMETER_V2.toString() + ";qs=0.7"),
    					MediaType.valueOf(MEDIATYPE_CUSTOM_NO_PARAMETER_V2.toString() + ";qs=0.9")),
    			List.of(MediaTypeUtils.APPLICATION_HAL_JSON_TYPE));
    	Assertions.assertEquals(MEDIATYPE_WITH_PARAMETER_V2, selected);
    }

	@Test
    public void selectMediaType_shouldReturnMatchingMediaTypeEvenIfItsQsValueIsLow_whenASpecificMediaTypeIsRequested() {
    	MediaType selected = MediaTypeUtils.selectMediaType(
    			List.of(
    					MediaType.valueOf(MEDIATYPE_WITH_PARAMETER_V1.toString() + ";qs=0.1"),
    					MediaType.valueOf(MEDIATYPE_WITH_PARAMETER_V2.toString() + ";qs=0.7"),
    					MediaType.valueOf(MEDIATYPE_CUSTOM_NO_PARAMETER_V2.toString() + ";qs=0.9")),
    			List.of(MEDIATYPE_WITH_PARAMETER_V1));
    	Assertions.assertEquals(MEDIATYPE_WITH_PARAMETER_V1, selected);
    }

	@Test
    public void selectMediaType_shouldReturnMatchingMediaTypeBasedOnQAndQsValues_whenMultipleMediaTypeAreRequested() {
    	MediaType selected = MediaTypeUtils.selectMediaType(
    			List.of(
    					MediaType.valueOf(MEDIATYPE_WITH_PARAMETER_V1.toString() + ";qs=0.1"),
    					MediaType.valueOf(MEDIATYPE_WITH_PARAMETER_V2.toString() + ";qs=0.7"),
    					MediaType.valueOf(MEDIATYPE_CUSTOM_NO_PARAMETER_V2.toString() + ";qs=0.9")),
    			List.of(
    					MediaType.valueOf(MEDIATYPE_WITH_PARAMETER_V1.toString() + ";q=0.8"),
    					MediaType.valueOf(MEDIATYPE_WITH_PARAMETER_V2.toString() + ";q=0.3")));
    	Assertions.assertEquals(MEDIATYPE_WITH_PARAMETER_V1, selected);
    }

	@Test
    public void selectMediaType_shouldReturnMatchingMediaTypeIncludingMoreSpecificParameters_whenProducibleMediaTypeIsMoreSpecificInParametersThanRequested() {
    	MediaType selected = MediaTypeUtils.selectMediaType(
    			List.of(
    					MediaType.valueOf(MEDIATYPE_WITH_PARAMETER_V1.toString() + ";qs=0.1"),
    					MediaType.valueOf(MEDIATYPE_WITH_PARAMETER_V2.toString() + ";qs=0.7;charset=UTF-8"),
    					MediaType.valueOf(MEDIATYPE_CUSTOM_NO_PARAMETER_V2.toString() + ";qs=0.9")),
    			List.of(MEDIATYPE_WITH_PARAMETER_V2));
    	Assertions.assertEquals(MediaType.valueOf(MEDIATYPE_WITH_PARAMETER_V2.toString() + ";charset=UTF-8"), selected);
    }
}
