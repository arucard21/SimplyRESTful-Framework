package simplyrestful.api.framework.core.hal;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.openapitools.jackson.dataformat.hal.HALLink;
import simplyrestful.api.framework.resources.HALCollection;
import simplyrestful.api.framework.resources.HALResource;

public class HALCollectionTest {
	private static final String TEST_COLLECTION_PROFILE_URI = "local://docs/resources/testcollection";
	private static final String TEST_COLLECTION_HREF_1 = "local://docs/resources/testcollection/1";
	private static final String TEST_COLLECTION_HREF_2 = "local://docs/resources/testcollection/2";
	private static final String TEST_COLLECTION_PAGE_BASE = "local://docs/resources/testcollection/2?page=";
	private static final URI requestURI = URI.create("local://resources/testresources/");

	private HALCollection<TestResource> testCollection;
	private HALCollection<TestResource> testCollectionSame;
	private HALCollection<TestResource> testCollectionDifferent;
	private List<TestResource> testResourcesList;

	@BeforeEach
	public void createTestResources(){
		testResourcesList = new ArrayList<TestResource>();
		for (int i = 0; i < 100; i++){
			TestResource testResource = new TestResource();
			URI selfLink = UriBuilder.fromUri(requestURI).path(String.valueOf(i)).build();
			testResource.setSelf(new HALLink.Builder(selfLink).build());
			testResourcesList.add(testResource);
		}

		testCollection = new HALCollection<>();
		testCollection.setSelf(new HALLink.Builder(TEST_COLLECTION_HREF_1).profile(testCollection.getProfile()).build());
		testCollection.setPage(1);
		testCollection.setMaxPageSize(10);
		testCollection.setTotal(100);
		testCollection.setFirst(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"1").profile(testCollection.getProfile()).build());
		testCollection.setLast(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"10").profile(testCollection.getProfile()).build());
		testCollection.setPrev(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"1").profile(testCollection.getProfile()).build());
		testCollection.setNext(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"10").profile(testCollection.getProfile()).build());

		testCollectionSame = new HALCollection<>();
		testCollectionSame.setSelf(new HALLink.Builder(TEST_COLLECTION_HREF_1).profile(testCollectionSame.getProfile()).build());
		testCollectionSame.setPage(1);
		testCollectionSame.setMaxPageSize(10);
		testCollectionSame.setTotal(100);
		testCollectionSame.setFirst(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"1").profile(testCollectionSame.getProfile()).build());
		testCollectionSame.setLast(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"10").profile(testCollectionSame.getProfile()).build());
		testCollectionSame.setPrev(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"1").profile(testCollectionSame.getProfile()).build());
		testCollectionSame.setNext(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"10").profile(testCollectionSame.getProfile()).build());

		testCollectionDifferent = new HALCollection<>();
		testCollectionDifferent.setSelf(new HALLink.Builder(TEST_COLLECTION_HREF_2).profile(testCollectionDifferent.getProfile()).build());
		testCollectionDifferent.setPage(3);
		testCollectionDifferent.setMaxPageSize(5);
		testCollectionDifferent.setTotal(50);
		testCollectionDifferent.setFirst(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"1").profile(testCollectionDifferent.getProfile()).build());
		testCollectionDifferent.setLast(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"10").profile(testCollectionDifferent.getProfile()).build());
		testCollectionDifferent.setPrev(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"2").profile(testCollectionDifferent.getProfile()).build());
		testCollectionDifferent.setNext(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"4").profile(testCollectionDifferent.getProfile()).build());
	}

	@Test
	public void halCollection_shouldHaveSameHashcode_whenObjectsAreEqual() throws Exception {
		Assertions.assertEquals(testCollection.hashCode(), testCollectionSame.hashCode());
	}

	@Test
	public void halCollection_shouldBeEqual_whenContainingSameValues() throws Exception {
		Assertions.assertEquals(testCollection, testCollectionSame);
	}

	@Test
	public void halCollection_shouldBeEqual_whenSameInstance() throws Exception {
		Assertions.assertEquals(testCollection, testCollection);
	}

	@Test
	public void halCollection_shouldNotBeEqual_whenContainingDifferentValues() throws Exception {
		Assertions.assertNotEquals(testCollection, testCollectionDifferent);
	}

	@Test
	public void halCollection_shouldNotBeEqual_whenOnlySelfLinkDiffers() throws Exception {
		HALCollection<TestResource> onlySelfLink1 = new HALCollection<>();
		onlySelfLink1.setSelf(new HALLink.Builder(TEST_COLLECTION_HREF_1).profile(onlySelfLink1.getProfile()).build());

		HALCollection<TestResource> onlySelfLink2 = new HALCollection<>();
		onlySelfLink2.setSelf(new HALLink.Builder(TEST_COLLECTION_HREF_2).profile(onlySelfLink2.getProfile()).build());

		Assertions.assertNotEquals(onlySelfLink1, onlySelfLink2);
	}

	private class TestResource extends HALResource {
		@Override
		public URI getProfile() {
			return URI.create(TEST_COLLECTION_PROFILE_URI);
		}
	}
}
