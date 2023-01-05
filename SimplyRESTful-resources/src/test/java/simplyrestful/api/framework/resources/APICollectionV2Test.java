package simplyrestful.api.framework.resources;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import simplyrestful.api.framework.resources.test.TestResource;

public class APICollectionV2Test {
	public static final String TEST_COLLECTION_HREF_1 = "local://docs/resources/testcollection1/";
	public static final String TEST_COLLECTION_HREF_2 = "local://docs/resources/testcollection2/";
	public static final String TEST_COLLECTION_PAGE_BASE1 = "local://docs/resources/testcollection1/?page=";
	public static final String TEST_COLLECTION_PAGE_BASE2 = "local://docs/resources/testcollection2/?page=";
	public static final URI requestURI = URI.create("local://resources/testresources/");

	private APICollectionV2<TestResource> testCollection;
	private APICollectionV2<TestResource> testCollectionSame;
	private APICollectionV2<TestResource> testCollectionDifferent;
	private List<TestResource> testResourcesList;

	@BeforeEach
	public void createTestResources(){
		testResourcesList = new ArrayList<TestResource>();
		for (int i = 0; i < 100; i++){
			TestResource testResource = new TestResource();
			URI selfLink = UriBuilder.fromUri(requestURI).path(String.valueOf(i)).build();
			testResource.setSelf(new Link(selfLink.toString(), null));
			testResourcesList.add(testResource);
		}

		testCollection = new APICollectionV2<>();
		testCollection.setSelf(new Link(TEST_COLLECTION_HREF_1, null));
		testCollection.setTotal(100);
		testCollection.setFirst(new Link(TEST_COLLECTION_PAGE_BASE1+"1", null));
		testCollection.setLast(new Link(TEST_COLLECTION_PAGE_BASE1+"10", null));
		testCollection.setPrev(new Link(TEST_COLLECTION_PAGE_BASE1+"1", null));
		testCollection.setNext(new Link(TEST_COLLECTION_PAGE_BASE1+"10", null));

		testCollectionSame = new APICollectionV2<>();
		testCollectionSame.setSelf(new Link(TEST_COLLECTION_HREF_1, null));
		testCollectionSame.setTotal(100);
		testCollectionSame.setFirst(new Link(TEST_COLLECTION_PAGE_BASE1+"1", null));
		testCollectionSame.setLast(new Link(TEST_COLLECTION_PAGE_BASE1+"10", null));
		testCollectionSame.setPrev(new Link(TEST_COLLECTION_PAGE_BASE1+"1", null));
		testCollectionSame.setNext(new Link(TEST_COLLECTION_PAGE_BASE1+"10", null));

		testCollectionDifferent = new APICollectionV2<>();
		testCollectionDifferent.setSelf(new Link(TEST_COLLECTION_HREF_2, null));
		testCollectionDifferent.setTotal(50);
		testCollectionDifferent.setFirst(new Link(TEST_COLLECTION_PAGE_BASE2+"1", null));
		testCollectionDifferent.setLast(new Link(TEST_COLLECTION_PAGE_BASE2+"10", null));
		testCollectionDifferent.setPrev(new Link(TEST_COLLECTION_PAGE_BASE2+"2", null));
		testCollectionDifferent.setNext(new Link(TEST_COLLECTION_PAGE_BASE2+"4", null));
	}

	@Test
	public void equalsContract() {
	    EqualsVerifier.forClass(APICollectionV2.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
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
		APICollectionV2<TestResource> onlySelfLink1 = new APICollectionV2<>();
		onlySelfLink1.setSelf(new Link(TEST_COLLECTION_HREF_1, null));

		APICollectionV2<TestResource> onlySelfLink2 = new APICollectionV2<>();
		onlySelfLink2.setSelf(new Link(TEST_COLLECTION_HREF_2, null));

		Assertions.assertNotEquals(onlySelfLink1, onlySelfLink2);
	}
}
