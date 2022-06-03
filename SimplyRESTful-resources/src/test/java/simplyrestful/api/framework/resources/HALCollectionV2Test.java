package simplyrestful.api.framework.resources;

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
import simplyrestful.api.framework.resources.test.TestResourceWithEquals;

public class HALCollectionV2Test {
	public static final String TEST_COLLECTION_HREF_1 = "local://docs/resources/testcollection/1";
	public static final String TEST_COLLECTION_HREF_2 = "local://docs/resources/testcollection/2";
	public static final String TEST_COLLECTION_PAGE_BASE = "local://docs/resources/testcollection/2?page=";
	public static final URI requestURI = URI.create("local://resources/testresources/");

	private HALCollectionV2<TestResourceWithEquals> testCollection;
	private HALCollectionV2<TestResourceWithEquals> testCollectionSame;
	private HALCollectionV2<TestResourceWithEquals> testCollectionDifferent;
	private List<TestResourceWithEquals> testResourcesList;

	@BeforeEach
	public void createTestResources(){
		testResourcesList = new ArrayList<TestResourceWithEquals>();
		for (int i = 0; i < 100; i++){
			TestResourceWithEquals testResource = new TestResourceWithEquals();
			URI selfLink = UriBuilder.fromUri(requestURI).path(String.valueOf(i)).build();
			testResource.setSelf(new HALLink.Builder(selfLink).build());
			testResourcesList.add(testResource);
		}

		testCollection = new HALCollectionV2<>();
		testCollection.setSelf(new HALLink.Builder(TEST_COLLECTION_HREF_1).profile(testCollection.getProfile()).build());
		testCollection.setTotal(100);
		testCollection.setFirst(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"1").profile(testCollection.getProfile()).build());
		testCollection.setLast(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"10").profile(testCollection.getProfile()).build());
		testCollection.setPrev(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"1").profile(testCollection.getProfile()).build());
		testCollection.setNext(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"10").profile(testCollection.getProfile()).build());

		testCollectionSame = new HALCollectionV2<>();
		testCollectionSame.setSelf(new HALLink.Builder(TEST_COLLECTION_HREF_1).profile(testCollectionSame.getProfile()).build());
		testCollectionSame.setTotal(100);
		testCollectionSame.setFirst(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"1").profile(testCollectionSame.getProfile()).build());
		testCollectionSame.setLast(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"10").profile(testCollectionSame.getProfile()).build());
		testCollectionSame.setPrev(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"1").profile(testCollectionSame.getProfile()).build());
		testCollectionSame.setNext(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"10").profile(testCollectionSame.getProfile()).build());

		testCollectionDifferent = new HALCollectionV2<>();
		testCollectionDifferent.setSelf(new HALLink.Builder(TEST_COLLECTION_HREF_2).profile(testCollectionDifferent.getProfile()).build());
		testCollectionDifferent.setTotal(50);
		testCollectionDifferent.setFirst(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"1").profile(testCollectionDifferent.getProfile()).build());
		testCollectionDifferent.setLast(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"10").profile(testCollectionDifferent.getProfile()).build());
		testCollectionDifferent.setPrev(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"2").profile(testCollectionDifferent.getProfile()).build());
		testCollectionDifferent.setNext(new HALLink.Builder(TEST_COLLECTION_PAGE_BASE+"4").profile(testCollectionDifferent.getProfile()).build());
	}

	@Test
	public void equalsContract() {
	    EqualsVerifier.forClass(HALCollectionV2.class)
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
		HALCollectionV2<TestResourceWithEquals> onlySelfLink1 = new HALCollectionV2<>();
		onlySelfLink1.setSelf(new HALLink.Builder(TEST_COLLECTION_HREF_1).profile(onlySelfLink1.getProfile()).build());

		HALCollectionV2<TestResourceWithEquals> onlySelfLink2 = new HALCollectionV2<>();
		onlySelfLink2.setSelf(new HALLink.Builder(TEST_COLLECTION_HREF_2).profile(onlySelfLink2.getProfile()).build());

		Assertions.assertNotEquals(onlySelfLink1, onlySelfLink2);
	}
}
