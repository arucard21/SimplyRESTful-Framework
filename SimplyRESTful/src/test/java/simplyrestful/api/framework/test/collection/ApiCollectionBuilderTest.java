package simplyrestful.api.framework.test.collection;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import simplyrestful.api.framework.collection.ApiCollectionBuilder;
import simplyrestful.api.framework.resources.ApiCollection;
import simplyrestful.api.framework.resources.ApiResource;
import simplyrestful.api.framework.resources.Link;

public class ApiCollectionBuilderTest {
    protected static final int TEST_RESOURCES_SIZE = 1000;
    protected static final URI requestURI = URI.create("local://resources/testresources/");
    protected List<TestResource> testResourcesList;
    private final MediaType customJson = new ApiCollection<TestResource>().customJsonMediaType(new TestResource().customJsonMediaType());

    @BeforeEach
    public void createSourceData() {
        testResourcesList = new ArrayList<TestResource>();
        for (int i = 0; i < TEST_RESOURCES_SIZE; i++) {
            TestResource testResource = new TestResource();
            testResource.setNumber(i);
            URI selfLink = UriBuilder.fromUri(requestURI).path(String.valueOf(i)).build();
            testResource.setSelf(new Link(selfLink, null));
            testResourcesList.add(testResource);
        }
    }

    protected ApiCollection<TestResource> createExpectedCollection(int startOfFirst, int startOfLast, int startOfPrev,
            int startOfNext, int sublistBegin, int sublistEnd) {
        ApiCollection<TestResource> expected = new ApiCollection<TestResource>();
        expected.setSelf(new Link(requestURI, customJson));
        expected.setTotal(TEST_RESOURCES_SIZE);
        Link firstPage = startOfFirst == -1 ? null
                : new Link(UriBuilder.fromUri(requestURI).replaceQueryParam("pageStart", startOfFirst).build(), customJson);
        Link lastPage = startOfLast == -1 ? null
                : new Link(
                        UriBuilder.fromUri(requestURI).replaceQueryParam("pageStart", startOfLast).build(), customJson);
        Link prevPage = startOfPrev == -1 ? null
                : new Link(
                        UriBuilder.fromUri(requestURI).replaceQueryParam("pageStart", startOfPrev).build(), customJson);
        Link nextPage = startOfNext == -1 ? null
                : new Link(
                        UriBuilder.fromUri(requestURI).replaceQueryParam("pageStart", startOfNext).build(), customJson);
        expected.setFirst(firstPage);
        expected.setLast(lastPage);
        expected.setPrev(prevPage);
        expected.setNext(nextPage);
        List<TestResource> resourcesForPage = testResourcesList.subList(sublistBegin, sublistEnd);
        expected.setItem(resourcesForPage);
        return expected;
    }

    protected class TestResource extends ApiResource {
        private int number;

        @Override
        public MediaType customJsonMediaType() {
            return new MediaType("application", "x.testresource-v1+json");
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }
    }

    @Test
    public void test_createPagedCollection_DefaultValues() {
        int pageStart = 0;
        int maxPageSize = 100;
        List<TestResource> resources = testResourcesList.subList(0, 100);
        ApiCollection<TestResource> actual = ApiCollectionBuilder.from(resources, requestURI)
                .collectionSize(TEST_RESOURCES_SIZE).withNavigation(pageStart, maxPageSize).build(customJson);
        ApiCollection<TestResource> expected = createExpectedCollection(0, 900, -1, 100, 0, 100);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void test_createPagedCollection_WithPrevPage() {
        int pageStart = 300;
        int maxPageSize = 100;
        List<TestResource> resources = testResourcesList.subList(200, 300);
        ApiCollection<TestResource> actual = ApiCollectionBuilder.from(resources, requestURI)
                .collectionSize(TEST_RESOURCES_SIZE).withNavigation(pageStart, maxPageSize).build(customJson);
        ApiCollection<TestResource> expected = createExpectedCollection(0, 900, 200, 400, 200, 300);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void test_createPagedCollection_WithoutNextPage() {
        int pageStart = 900;
        int maxPageSize = 100;
        List<TestResource> resources = testResourcesList.subList(900, 1000);
        ApiCollection<TestResource> actual = ApiCollectionBuilder.from(resources, requestURI)
                .collectionSize(TEST_RESOURCES_SIZE).withNavigation(pageStart, maxPageSize).build(customJson);
        ApiCollection<TestResource> expected = createExpectedCollection(0, 900, 800, -1, 900, 1000);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void test_createPagedCollection_WithLastPageNotEqualToMaxPageSize() {
        int pageStart = 4;
        int maxPageSize = 300;
        List<TestResource> resources = testResourcesList.subList(900, 1000);
        ApiCollection<TestResource> actual = ApiCollectionBuilder.from(resources, requestURI)
                .collectionSize(TEST_RESOURCES_SIZE).withNavigation(pageStart, maxPageSize).build(customJson);
        Assertions.assertEquals(100, actual.getItem().size());
    }

    @Test
    public void test_createPagedCollection_UsesCorrectMediaTypeForCustomJson() {
        int pageStart = 1;
        int maxPageSize = 100;
        List<TestResource> resources = testResourcesList.subList(200, 300);
        ApiCollection<TestResource> actual = ApiCollectionBuilder.from(resources, requestURI)
                .collectionSize(TEST_RESOURCES_SIZE).withNavigation(pageStart, maxPageSize).build(customJson);
        Assertions.assertEquals(customJson, actual.getSelf().getType());
    }

    @Test
    public void test_createEmptyCollection() {
        int pageStart = 0;
        int maxPageSize = 100;
        List<TestResource> resources = Collections.emptyList();
        ApiCollection<TestResource> actual = ApiCollectionBuilder.from(resources, requestURI)
                .collectionSize(0).withNavigation(pageStart, maxPageSize).build(customJson);
        Assertions.assertEquals(0, actual.getItem().size());
    }
}
