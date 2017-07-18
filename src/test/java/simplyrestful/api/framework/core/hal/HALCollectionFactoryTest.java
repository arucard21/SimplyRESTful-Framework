package simplyrestful.api.framework.core.hal;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.junit.Before;
import org.junit.Test;

import dk.nykredit.jackson.dataformat.hal.HALLink;
import simplyrestful.api.framework.core.MediaType;

public class HALCollectionFactoryTest {

	private static final String HALCOLLECTION_PROFILE = "https://arucard21.github.io/SimplyRESTful/HALCollection/v1";
	private static final int TEST_RESOURCES_SIZE = 1000;
	private List<TestResource> testResourcesList;
	private URI requestURI = URI.create("local://resources/testresources/");
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
		assertEquals(expected, actual);
	}

	@Test
	public void test_createPagedCollectionFromFullList_WithPrevPage() {
		int page = 3;
		int pageSize = 100;
		boolean compact = true;
		HALCollection<TestResource> actual = collectionFactory.createPagedCollectionFromFullList(testResourcesList, page, pageSize, requestURI, compact);
		HALCollection<TestResource> expected = createExpectedCollection(page, pageSize, 1, 10, 2, 4, 200, 300, compact);
		assertEquals(expected, actual);
	}

	@Test
	public void test_createPagedCollectionFromFullList_WithoutNextPage() {
		int page = 10;
		int pageSize = 100;
		boolean compact = true;
		HALCollection<TestResource> actual = collectionFactory.createPagedCollectionFromFullList(testResourcesList, page, pageSize, requestURI, compact);
		HALCollection<TestResource> expected = createExpectedCollection(page, pageSize, 1, 10, 9, -1, 900, 1000, compact);
		assertEquals(expected, actual);
	}

	@Test
	public void test_createPagedCollectionFromFullList_Embedded() {
		int page = 10;
		int pageSize = 100;
		boolean compact = false;
		HALCollection<TestResource> actual = collectionFactory.createPagedCollectionFromFullList(testResourcesList, page, pageSize, requestURI, compact);
		HALCollection<TestResource> expected = createExpectedCollection(page, pageSize, 1, 10, 9, -1, 900, 1000, compact);
		assertEquals(expected, actual);
	}

	@Test
	public void test_createPagedCollectionFromFullList_WithLastPageNotEqualToMaxPageSize() {
		int page = 4;
		int pageSize = 300;
		boolean compact = true;
		HALCollection<TestResource> actual = collectionFactory.createPagedCollectionFromFullList(testResourcesList, page, pageSize, requestURI, compact);
		assertEquals(100, actual.getItem().size());
	}

	@Test
	public void test_createPagedCollectionFromFullList_UsesCorrectProfile() {
		int page = 1;
		int pageSize = 100;
		boolean compact = true;
		HALCollection<TestResource> actual = collectionFactory.createPagedCollectionFromFullList(testResourcesList, page, pageSize, requestURI, compact);
		assertEquals(HALCOLLECTION_PROFILE, actual.getProfile().toString());
	}

	@Test
	public void test_createPagedCollectionFromFullList_UsesCorrectMediaType() {
		int page = 1;
		int pageSize = 100;
		boolean compact = true;
		HALCollection<TestResource> actual = collectionFactory.createPagedCollectionFromFullList(testResourcesList, page, pageSize, requestURI, compact);
		assertEquals(MediaType.APPLICATION_HAL_JSON, actual.getSelf().getType());
	}

	private HALCollection<TestResource> createExpectedCollection(int page, int pageSize, int first, int last, int prev, int next, int sublistBegin, int sublistEnd, boolean compact) {
		HALCollection<TestResource> expected = new HALCollection<TestResource>();
		expected.setPage(page);
		expected.setPageSize(pageSize);
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
