package simplyrestful.api.framework.test.legacy;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.openapitools.jackson.dataformat.hal.HALLink;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import simplyrestful.api.framework.legacy.HALCollectionV1;
import simplyrestful.api.framework.test.implementation.TestResource;

@SuppressWarnings("deprecation")
public class HALCollectionV1Test {
	public static final String TEST_COLLECTION_HREF_1 = "local://docs/resources/testcollection/1";
	public static final String TEST_COLLECTION_HREF_2 = "local://docs/resources/testcollection/2";
	public static final String TEST_COLLECTION_PAGE_BASE = "local://docs/resources/testcollection/2?page=";
	public static final URI requestURI = URI.create("local://resources/testresources/");

	private HALCollectionV1<TestResource> testCollection;
	private HALCollectionV1<TestResource> testCollectionSame;
	private HALCollectionV1<TestResource> testCollectionDifferent;
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

		testCollection = new HALCollectionV1<>();
		testCollection.setSelf(new HALLink.Builder(TEST_COLLECTION_HREF_1).profile(testCollection.getProfile()).build());
		testCollection.setPage(1);
		testCollection.setPageSize(10);
		testCollection.setTotal(100);
		testCollection.setFirst(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"1").profile(testCollection.getProfile()).build());
		testCollection.setLast(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"10").profile(testCollection.getProfile()).build());
		testCollection.setPrev(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"1").profile(testCollection.getProfile()).build());
		testCollection.setNext(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"10").profile(testCollection.getProfile()).build());

		testCollectionSame = new HALCollectionV1<>();
		testCollectionSame.setSelf(new HALLink.Builder(TEST_COLLECTION_HREF_1).profile(testCollectionSame.getProfile()).build());
		testCollectionSame.setPage(1);
		testCollectionSame.setPageSize(10);
		testCollectionSame.setTotal(100);
		testCollectionSame.setFirst(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"1").profile(testCollectionSame.getProfile()).build());
		testCollectionSame.setLast(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"10").profile(testCollectionSame.getProfile()).build());
		testCollectionSame.setPrev(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"1").profile(testCollectionSame.getProfile()).build());
		testCollectionSame.setNext(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"10").profile(testCollectionSame.getProfile()).build());

		testCollectionDifferent = new HALCollectionV1<>();
		testCollectionDifferent.setSelf(new HALLink.Builder(TEST_COLLECTION_HREF_2).profile(testCollectionDifferent.getProfile()).build());
		testCollectionDifferent.setPage(3);
		testCollectionDifferent.setPageSize(5);
		testCollectionDifferent.setTotal(50);
		testCollectionDifferent.setFirst(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"1").profile(testCollectionDifferent.getProfile()).build());
		testCollectionDifferent.setLast(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"10").profile(testCollectionDifferent.getProfile()).build());
		testCollectionDifferent.setPrev(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"2").profile(testCollectionDifferent.getProfile()).build());
		testCollectionDifferent.setNext(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"4").profile(testCollectionDifferent.getProfile()).build());
	}

	@Test
	public void equalsContract() {
	    EqualsVerifier.forClass(HALCollectionV1.class)
	    		.withRedefinedSuperclass()
	    		.suppress(Warning.NONFINAL_FIELDS)
	    		.verify();
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
		HALCollectionV1<TestResource> onlySelfLink1 = new HALCollectionV1<>();
		onlySelfLink1.setSelf(new HALLink.Builder(TEST_COLLECTION_HREF_1).profile(onlySelfLink1.getProfile()).build());

		HALCollectionV1<TestResource> onlySelfLink2 = new HALCollectionV1<>();
		onlySelfLink2.setSelf(new HALLink.Builder(TEST_COLLECTION_HREF_2).profile(onlySelfLink2.getProfile()).build());

		Assertions.assertNotEquals(onlySelfLink1, onlySelfLink2);
	}
}
