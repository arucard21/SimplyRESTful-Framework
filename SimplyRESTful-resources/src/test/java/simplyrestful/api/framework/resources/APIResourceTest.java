package simplyrestful.api.framework.resources;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import simplyrestful.api.framework.resources.test.TestResource;

public class APIResourceTest{
	public static final String TEST_RESOURCE_HREF_1 = "local://docs/resources/testresource/1";
	public static final String TEST_RESOURCE_HREF_2 = "local://docs/resources/testresource/2";

	private TestResource testResource;
	private TestResource testResourceSame;
	private TestResource testResourceDifferent;

	@BeforeEach
	public void createTestResources(){
		testResource = new TestResource();
		testResource.setSelf(new Link(TEST_RESOURCE_HREF_1, null));

		testResourceSame = new TestResource();
		testResourceSame.setSelf(new Link(TEST_RESOURCE_HREF_1, null));

		testResourceDifferent = new TestResource();
		testResourceDifferent.setSelf(new Link(TEST_RESOURCE_HREF_2, null));
	}

	@Test
	public void equalsContract() {
	    EqualsVerifier.forClass(TestResource.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

	@Test
	public void apiResource_shouldHaveSameHashcode_whenObjectsAreEqual() throws Exception {
		Assertions.assertEquals(testResource.hashCode(), testResourceSame.hashCode());
	}

	@Test
	public void apiResource_shouldBeEqual_whenContainingSameValues() throws Exception {
		Assertions.assertEquals(testResource, testResourceSame);
	}

	@Test
	public void apiResource_shouldBeEqual_whenSameInstance() throws Exception {
		Assertions.assertEquals(testResource, testResource);
	}

	@Test
	public void apiResource_shouldNotBeEqual_whenContainingDifferentValues() throws Exception {
		Assertions.assertNotEquals(testResource, testResourceDifferent);
	}
}
