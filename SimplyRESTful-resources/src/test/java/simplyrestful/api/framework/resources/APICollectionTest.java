package simplyrestful.api.framework.resources;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.core.UriBuilder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import simplyrestful.api.framework.resources.test.TestResource;

public class APICollectionTest {
	public static final String TEST_COLLECTION_HREF_1 = "local://docs/resources/testcollection1/";
	public static final String TEST_COLLECTION_HREF_2 = "local://docs/resources/testcollection2/";
	public static final String TEST_COLLECTION_PAGE_BASE1 = "local://docs/resources/testcollection1/?page=";
	public static final String TEST_COLLECTION_PAGE_BASE2 = "local://docs/resources/testcollection2/?page=";
	public static final URI requestURI = URI.create("local://resources/testresources/");

	private APICollection<TestResource> testCollection;
	private APICollection<TestResource> testCollectionSame;
	private APICollection<TestResource> testCollectionDifferent;
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

		testCollection = new APICollection<>();
		testCollection.setSelf(new Link(TEST_COLLECTION_HREF_1, null));
		testCollection.setTotal(100);
		testCollection.setFirst(new Link(TEST_COLLECTION_PAGE_BASE1+"1", null));
		testCollection.setLast(new Link(TEST_COLLECTION_PAGE_BASE1+"10", null));
		testCollection.setPrev(new Link(TEST_COLLECTION_PAGE_BASE1+"1", null));
		testCollection.setNext(new Link(TEST_COLLECTION_PAGE_BASE1+"10", null));

		testCollectionSame = new APICollection<>();
		testCollectionSame.setSelf(new Link(TEST_COLLECTION_HREF_1, null));
		testCollectionSame.setTotal(100);
		testCollectionSame.setFirst(new Link(TEST_COLLECTION_PAGE_BASE1+"1", null));
		testCollectionSame.setLast(new Link(TEST_COLLECTION_PAGE_BASE1+"10", null));
		testCollectionSame.setPrev(new Link(TEST_COLLECTION_PAGE_BASE1+"1", null));
		testCollectionSame.setNext(new Link(TEST_COLLECTION_PAGE_BASE1+"10", null));

		testCollectionDifferent = new APICollection<>();
		testCollectionDifferent.setSelf(new Link(TEST_COLLECTION_HREF_2, null));
		testCollectionDifferent.setTotal(50);
		testCollectionDifferent.setFirst(new Link(TEST_COLLECTION_PAGE_BASE2+"1", null));
		testCollectionDifferent.setLast(new Link(TEST_COLLECTION_PAGE_BASE2+"10", null));
		testCollectionDifferent.setPrev(new Link(TEST_COLLECTION_PAGE_BASE2+"2", null));
		testCollectionDifferent.setNext(new Link(TEST_COLLECTION_PAGE_BASE2+"4", null));
	}

	@Test
	public void equalsContract() {
	    EqualsVerifier.forClass(APICollection.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

	@Test
	public void apiCollection_shouldHaveSameHashcode_whenObjectsAreEqual() throws Exception {
		Assertions.assertEquals(testCollection.hashCode(), testCollectionSame.hashCode());
	}

	@Test
	public void apiCollection_shouldBeEqual_whenContainingSameValues() throws Exception {
		Assertions.assertEquals(testCollection, testCollectionSame);
	}

	@Test
	public void apiCollection_shouldBeEqual_whenSameInstance() throws Exception {
		Assertions.assertEquals(testCollection, testCollection);
	}

	@Test
	public void apiCollection_shouldNotBeEqual_whenContainingDifferentValues() throws Exception {
		Assertions.assertNotEquals(testCollection, testCollectionDifferent);
	}

	@Test
	public void apiCollection_shouldNotBeEqual_whenOnlySelfLinkDiffers() throws Exception {
		APICollection<TestResource> onlySelfLink1 = new APICollection<>();
		onlySelfLink1.setSelf(new Link(TEST_COLLECTION_HREF_1, null));

		APICollection<TestResource> onlySelfLink2 = new APICollection<>();
		onlySelfLink2.setSelf(new Link(TEST_COLLECTION_HREF_2, null));

		Assertions.assertNotEquals(onlySelfLink1, onlySelfLink2);
	}
}
