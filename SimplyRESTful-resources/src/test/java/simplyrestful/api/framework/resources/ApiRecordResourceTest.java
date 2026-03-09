package simplyrestful.api.framework.resources;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import simplyrestful.api.framework.resources.test.TestRecordResource;

public class ApiRecordResourceTest {
	public static final String TEST_RESOURCE_HREF_1 = "local://docs/resources/testresource/1";
	public static final String TEST_RESOURCE_HREF_2 = "local://docs/resources/testresource/2";

	private TestRecordResource testResource;
	private TestRecordResource testResourceSame;
	private TestRecordResource testResourceDifferent;

	@BeforeEach
	public void createTestResources() {
		testResource = new TestRecordResource(new Link(TEST_RESOURCE_HREF_1, null));
		testResourceSame = new TestRecordResource(new Link(TEST_RESOURCE_HREF_1, null));
		testResourceDifferent = new TestRecordResource(new Link(TEST_RESOURCE_HREF_2, null));
	}

	@Test
	public void equalsContract() {
		EqualsVerifier.forClass(TestRecordResource.class).verify();
	}

	@Test
	public void apiResource_shouldHaveSameHashcode_whenObjectsAreEqual() {
		Assertions.assertEquals(testResource.hashCode(), testResourceSame.hashCode());
	}

	@Test
	public void apiResource_shouldBeEqual_whenContainingSameValues() {
		Assertions.assertEquals(testResource, testResourceSame);
	}

	@Test
	public void apiResource_shouldBeEqual_whenSameInstance() {
		Assertions.assertEquals(testResource, testResource);
	}

	@Test
	public void apiResource_shouldNotBeEqual_whenContainingDifferentValues() {
		Assertions.assertNotEquals(testResource, testResourceDifferent);
	}
}
