package simplyrestful.api.framework.core.hal;

import java.net.URI;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import simplyrestful.api.framework.core.AdditionalMediaTypes;
import simplyrestful.api.framework.resources.HALCollectionV1;

@Deprecated(since = "0.12.0")
public class HALCollectionBuilderV1FromFullListTest extends AbstractHALCollectionV1BuilderTest {
    @BeforeEach
    public void initializeBuilder() {
	builder = HALCollectionV1Builder.fromFull(testResourcesList, requestURI);
    }

    @Test
    public void test_createPagedCollectionFromFullList_DefaultValues() {
	int page = 1;
	int maxPageSize = 100;
	boolean compact = true;
	HALCollectionV1<TestResource> actual = builder.page(page).maxPageSize(maxPageSize).compact(compact).build();
	HALCollectionV1<TestResource> expected = createExpectedCollection(page, maxPageSize, 1, 10, -1, 2, 0, 100,
		compact);
	Assertions.assertEquals(expected, actual);
    }

    @Test
    public void test_createPagedCollectionFromFullList_WithPrevPage() {
	int page = 3;
	int maxPageSize = 100;
	boolean compact = true;
	HALCollectionV1<TestResource> actual = builder.page(page).maxPageSize(maxPageSize).compact(compact).build();
	HALCollectionV1<TestResource> expected = createExpectedCollection(page, maxPageSize, 1, 10, 2, 4, 200, 300,
		compact);
	Assertions.assertEquals(expected, actual);
    }

    @Test
    public void test_createPagedCollectionFromFullList_WithoutNextPage() {
	int page = 10;
	int maxPageSize = 100;
	boolean compact = true;
	HALCollectionV1<TestResource> actual = builder.page(page).maxPageSize(maxPageSize).compact(compact).build();
	HALCollectionV1<TestResource> expected = createExpectedCollection(page, maxPageSize, 1, 10, 9, -1, 900, 1000,
		compact);
	Assertions.assertEquals(expected, actual);
    }

    @Test
    public void test_createPagedCollectionFromFullList_Embedded() {
	int page = 10;
	int maxPageSize = 100;
	boolean compact = false;
	HALCollectionV1<TestResource> actual = builder.page(page).maxPageSize(maxPageSize).compact(compact).build();
	HALCollectionV1<TestResource> expected = createExpectedCollection(page, maxPageSize, 1, 10, 9, -1, 900, 1000,
		compact);
	Assertions.assertEquals(expected, actual);
    }

    @Test
    public void test_createPagedCollectionFromFullList_WithLastPageNotEqualToMaxPageSize() {
	int page = 4;
	int maxPageSize = 300;
	boolean compact = true;
	HALCollectionV1<TestResource> actual = builder.page(page).maxPageSize(maxPageSize).compact(compact).build();
	Assertions.assertEquals(100, actual.getItem().size());
	Assertions.assertEquals(300, actual.getMaxPageSize());
    }

    @Test
    public void test_createPagedCollectionFromFullList_UsesCorrectProfile() {
	int page = 1;
	int maxPageSize = 100;
	boolean compact = true;
	HALCollectionV1<TestResource> actual = builder.page(page).maxPageSize(maxPageSize).compact(compact).build();
	Assertions.assertEquals(URI.create(HALCOLLECTION_PROFILE), actual.getProfile());
    }

    @Test
    public void test_createPagedCollectionFromFullList_UsesCorrectMediaType() {
	int page = 1;
	int maxPageSize = 100;
	boolean compact = true;
	HALCollectionV1<TestResource> actual = builder.page(page).maxPageSize(maxPageSize).compact(compact).build();
	Assertions.assertEquals(AdditionalMediaTypes.APPLICATION_HAL_JSON, actual.getSelf().getType());
    }
}
