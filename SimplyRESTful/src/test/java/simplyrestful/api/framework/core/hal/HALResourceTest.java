package simplyrestful.api.framework.core.hal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.openapitools.jackson.dataformat.hal.HALLink;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import simplyrestful.api.framework.resources.HALResource;
import simplyrestful.api.framework.test.implementation.TestResource;

public class HALResourceTest{
	private static final String TEST_RESOURCE_HREF_1 = "local://docs/resources/testresource/1";
	private static final String TEST_RESOURCE_HREF_2 = "local://docs/resources/testresource/2";

	private HALResource testResource;
	private HALResource testResourceSame;
	private HALResource testResourceDifferent;

	@BeforeEach
	public void createTestResources(){
		testResource = new TestResource();
		testResource.setSelf(new HALLink.Builder(TEST_RESOURCE_HREF_1).profile(testResource.getProfile()).build());

		testResourceSame = new TestResource();
		testResourceSame.setSelf(new HALLink.Builder(TEST_RESOURCE_HREF_1).profile(testResourceSame.getProfile()).build());

		testResourceDifferent = new TestResource();
		testResourceDifferent.setSelf(new HALLink.Builder(TEST_RESOURCE_HREF_2).profile(testResourceDifferent.getProfile()).build());
	}
	
	@Test
	public void equalsContract() {
	    EqualsVerifier.forClass(HALResource.class)
	    		.withRedefinedSubclass(TestResource.class)
	    		.suppress(Warning.NONFINAL_FIELDS)
	    		.verify();
	}

	@Test
	public void halResource_shouldHaveSameHashcode_whenObjectsAreEqual() throws Exception {
		Assertions.assertEquals(testResource.hashCode(), testResourceSame.hashCode());
	}

	@Test
	public void halResource_shouldBeEqual_whenContainingSameValues() throws Exception {
		Assertions.assertEquals(testResource, testResourceSame);
	}

	@Test
	public void halResource_shouldBeEqual_whenSameInstance() throws Exception {
		Assertions.assertEquals(testResource, testResource);
	}

	@Test
	public void halResource_shouldNotBeEqual_whenContainingDifferentValues() throws Exception {
		Assertions.assertNotEquals(testResource, testResourceDifferent);
	}
}
