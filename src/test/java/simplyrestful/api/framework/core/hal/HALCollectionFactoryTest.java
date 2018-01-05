package simplyrestful.api.framework.core.hal;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.openapitools.jackson.dataformat.hal.HALLink;
import simplyrestful.api.framework.core.MediaType;

public class HALCollectionFactoryTest {

	private static final String HALCOLLECTION_PROFILE = "https://arucard21.github.io/SimplyRESTful/HALCollection/v1";
	private static final int TEST_RESOURCES_SIZE = 1000;
	private static final URI requestURI = URI.create("local://resources/testresources/");
	private List<TestResource> testResourcesList;
	private HALCollectionFactory<TestResource> collectionFactory;

	@Before
	public void createSourceData() {
		collectionFactory = new HALCollectionFactory<TestResource>();

		testResourcesList = new ArrayList<TestResource>();
		for (int i = 0; i <TEST_RESOURCES_SIZE; i++){
			TestResource testResource = new TestResource();
			testResource.setNumber(i);
			URI selfLink = UriBuilder.fromUri(requestURI).path(String.valueOf(i)).build();
			testResource.setSelf(new HALLink.Builder(selfLink).build());
			testResourcesList.add(testResource);
		}
	}

	@Test
	public void test_createPagedCollectionFromFullList_DefaultValues() {
		int page = 1;
		int pageSize = 100;
		boolean compact = true;
		HALCollection<TestResource> actual = collectionFactory.createPagedCollectionFromFullList(testResourcesList, page, pageSize, requestURI, compact);
		HALCollection<TestResource> expected = createExpectedCollection(page, pageSize, 1, 10, -1, 2, 0, 100, compact);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void test_createPagedCollectionFromFullList_WithPrevPage() {
		int page = 3;
		int pageSize = 100;
		boolean compact = true;
		HALCollection<TestResource> actual = collectionFactory.createPagedCollectionFromFullList(testResourcesList, page, pageSize, requestURI, compact);
		HALCollection<TestResource> expected = createExpectedCollection(page, pageSize, 1, 10, 2, 4, 200, 300, compact);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void test_createPagedCollectionFromFullList_WithoutNextPage() {
		int page = 10;
		int pageSize = 100;
		boolean compact = true;
		HALCollection<TestResource> actual = collectionFactory.createPagedCollectionFromFullList(testResourcesList, page, pageSize, requestURI, compact);
		HALCollection<TestResource> expected = createExpectedCollection(page, pageSize, 1, 10, 9, -1, 900, 1000, compact);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void test_createPagedCollectionFromFullList_Embedded() {
		int page = 10;
		int pageSize = 100;
		boolean compact = false;
		HALCollection<TestResource> actual = collectionFactory.createPagedCollectionFromFullList(testResourcesList, page, pageSize, requestURI, compact);
		HALCollection<TestResource> expected = createExpectedCollection(page, pageSize, 1, 10, 9, -1, 900, 1000, compact);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void test_createPagedCollectionFromFullList_WithLastPageNotEqualToMaxPageSize() {
		int page = 4;
		int pageSize = 300;
		boolean compact = true;
		HALCollection<TestResource> actual = collectionFactory.createPagedCollectionFromFullList(testResourcesList, page, pageSize, requestURI, compact);
		Assert.assertEquals(100, actual.getItem().size());
		Assert.assertEquals(300, actual.getMaxPageSize());
	}

	@Test
	public void test_createPagedCollectionFromFullList_UsesCorrectProfile() {
		int page = 1;
		int pageSize = 100;
		boolean compact = true;
		HALCollection<TestResource> actual = collectionFactory.createPagedCollectionFromFullList(testResourcesList, page, pageSize, requestURI, compact);
		Assert.assertEquals(URI.create(HALCOLLECTION_PROFILE), actual.getProfile());
	}

	@Test
	public void test_createPagedCollectionFromFullList_UsesCorrectMediaType() {
		int page = 1;
		int pageSize = 100;
		boolean compact = true;
		HALCollection<TestResource> actual = collectionFactory.createPagedCollectionFromFullList(testResourcesList, page, pageSize, requestURI, compact);
		Assert.assertEquals(MediaType.APPLICATION_HAL_JSON, actual.getSelf().getType());
	}

	@Test
	public void test_createPagedCollectionFromPartialList_DefaultValues() {
		int page = 1;
		int maxPageSize = 100;
		boolean compact = true;
		List<TestResource> partialList = testResourcesList.subList(0, 100);
		HALCollection<TestResource> actual = collectionFactory.createPagedCollectionFromPartialList(partialList, page, maxPageSize, TEST_RESOURCES_SIZE, requestURI, compact);
		HALCollection<TestResource> expected = createExpectedCollection(page, maxPageSize, 1, 10, -1, 2, 0, 100, compact);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void test_createPagedCollectionFromPartialList_WithPrevPage() {
		int page = 3;
		int maxPageSize = 100;
		boolean compact = true;
		List<TestResource> partialList = testResourcesList.subList(200, 300);
		HALCollection<TestResource> actual = collectionFactory.createPagedCollectionFromPartialList(partialList, page, maxPageSize, TEST_RESOURCES_SIZE, requestURI, compact);
		HALCollection<TestResource> expected = createExpectedCollection(page, 100, 1, 10, 2, 4, 200, 300, compact);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void test_createPagedCollectionFromPartialList_WithoutNextPage() {
		int page = 10;
		int maxPageSize = 100;
		boolean compact = true;
		List<TestResource> partialList = testResourcesList.subList(900, 1000);
		HALCollection<TestResource> actual = collectionFactory.createPagedCollectionFromPartialList(partialList, page, maxPageSize, TEST_RESOURCES_SIZE, requestURI, compact);
		HALCollection<TestResource> expected = createExpectedCollection(page, 100, 1, 10, 9, -1, 900, 1000, compact);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void test_createPagedCollectionFromPartialList_Embedded() {
		int page = 10;
		int maxPageSize = 100;
		boolean compact = false;
		List<TestResource> partialList = testResourcesList.subList(900, 1000);
		HALCollection<TestResource> actual = collectionFactory.createPagedCollectionFromPartialList(partialList, page, maxPageSize, TEST_RESOURCES_SIZE, requestURI, compact);
		HALCollection<TestResource> expected = createExpectedCollection(page, 100, 1, 10, 9, -1, 900, 1000, compact);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void test_createPagedCollectionFromPartialList_WithLastPageNotEqualToMaxPageSize() {
		int page = 4;
		int maxPageSize = 300;
		boolean compact = true;
		List<TestResource> partialList = testResourcesList.subList(900, 1000);
		HALCollection<TestResource> actual = collectionFactory.createPagedCollectionFromPartialList(partialList, page, maxPageSize, TEST_RESOURCES_SIZE, requestURI, compact);
		Assert.assertEquals(100, actual.getItem().size());
		Assert.assertEquals(300, actual.getMaxPageSize());
	}

	@Test
	public void test_createPagedCollectionFromPartialList_UsesCorrectProfile() {
		int page = 1;
		int maxPageSize = 100;
		boolean compact = true;
		HALCollection<TestResource> actual = collectionFactory.createPagedCollectionFromPartialList(testResourcesList, page, maxPageSize, TEST_RESOURCES_SIZE, requestURI, compact);
		Assert.assertEquals(URI.create(HALCOLLECTION_PROFILE), actual.getProfile());
	}

	@Test
	public void test_createPagedCollectionFromPartialList_UsesCorrectMediaType() {
		int page = 1;
		int maxPageSize = 100;
		boolean compact = true;
		HALCollection<TestResource> actual = collectionFactory.createPagedCollectionFromPartialList(testResourcesList, page, maxPageSize, TEST_RESOURCES_SIZE, requestURI, compact);
		Assert.assertEquals(MediaType.APPLICATION_HAL_JSON, actual.getSelf().getType());
	}

	private HALCollection<TestResource> createExpectedCollection(int page, int pageSize, int first, int last, int prev, int next, int sublistBegin, int sublistEnd, boolean compact) {
		HALCollection<TestResource> expected = new HALCollection<TestResource>();
		expected.setSelf(new HALLink.Builder(requestURI).type(MediaType.APPLICATION_HAL_JSON).profile(expected.getProfile()).build());
		expected.setPage(page);
		expected.setMaxPageSize(pageSize);
		expected.setTotal(TEST_RESOURCES_SIZE);
		HALLink firstPage = first == -1 ? null : new HALLink.Builder(UriBuilder.fromUri(requestURI).replaceQueryParam("page", first).build()).build();
		HALLink lastPage = last == -1 ? null : new HALLink.Builder(UriBuilder.fromUri(requestURI).replaceQueryParam("page", last).build()).build();
		HALLink prevPage = prev == -1 ? null : new HALLink.Builder(UriBuilder.fromUri(requestURI).replaceQueryParam("page", prev).build()).build();
		HALLink nextPage = next == -1 ? null : new HALLink.Builder(UriBuilder.fromUri(requestURI).replaceQueryParam("page", next).build()).build();
		expected.setFirst(firstPage);
		expected.setLast(lastPage);
		expected.setPrev(prevPage);
		expected.setNext(nextPage);
		List<TestResource> resourcesForPage = testResourcesList.subList(sublistBegin, sublistEnd);
		if (!compact){
			expected.setItemEmbedded(resourcesForPage);
			return expected;
		}
		ArrayList<HALLink> resourceLinksForPage = new ArrayList<HALLink>(pageSize);
		for(TestResource resourceForPage: resourcesForPage){
			resourceLinksForPage.add(resourceForPage.getSelf());
		}
		expected.setItem(resourceLinksForPage);
		return expected;
	}

	private class TestResource extends HALResource {
		private int number;

		@Override
		public URI getProfile() {
			return URI.create("local://docs/resources/testresource");
		}

		@SuppressWarnings("unused")
		public int getNumber() {
			return number;
		}

		public void setNumber(int number) {
			this.number = number;
		}

	}
}
