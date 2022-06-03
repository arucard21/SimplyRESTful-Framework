package simplyrestful.api.framework.test.legacy;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.junit.jupiter.api.BeforeEach;

import io.openapitools.jackson.dataformat.hal.HALLink;
import simplyrestful.api.framework.MediaTypeUtils;
import simplyrestful.api.framework.legacy.HALCollectionV1;
import simplyrestful.api.framework.legacy.HALCollectionV1Builder;
import simplyrestful.api.framework.resources.HALResource;

@Deprecated(since = "0.12.0")
public abstract class AbstractHALCollectionV1BuilderTest {
    protected static final String HALCOLLECTION_PROFILE = "https://arucard21.github.io/SimplyRESTful-Framework/HALCollection/v1";
    protected static final int TEST_RESOURCES_SIZE = 1000;
    protected static final URI requestURI = URI.create("local://resources/testresources/");
    protected List<TestResource> testResourcesList;
    protected HALCollectionV1Builder<TestResource> builder;

    @BeforeEach
    public void createSourceData() {
	testResourcesList = new ArrayList<TestResource>();
	for (int i = 0; i < TEST_RESOURCES_SIZE; i++) {
	    TestResource testResource = new TestResource();
	    testResource.setNumber(i);
	    URI selfLink = UriBuilder.fromUri(requestURI).path(String.valueOf(i)).build();
	    testResource.setSelf(new HALLink.Builder(selfLink).build());
	    testResourcesList.add(testResource);
	}
    }

    protected HALCollectionV1<TestResource> createExpectedCollection(int page, int pageSize, int first, int last,
	    int prev, int next, int sublistBegin, int sublistEnd, boolean compact) {
	HALCollectionV1<TestResource> expected = new HALCollectionV1<TestResource>();
	expected.setSelf(new HALLink.Builder(requestURI).type(MediaTypeUtils.APPLICATION_HAL_JSON)
		.profile(expected.getProfile()).build());
	expected.setPage(page);
	expected.setPageSize(pageSize);
	expected.setTotal(TEST_RESOURCES_SIZE);
	HALLink firstPage = first == -1 ? null
		: new HALLink.Builder(UriBuilder.fromUri(requestURI).replaceQueryParam("page", first).build()).build();
	HALLink lastPage = last == -1 ? null
		: new HALLink.Builder(UriBuilder.fromUri(requestURI).replaceQueryParam("page", last).build()).build();
	HALLink prevPage = prev == -1 ? null
		: new HALLink.Builder(UriBuilder.fromUri(requestURI).replaceQueryParam("page", prev).build()).build();
	HALLink nextPage = next == -1 ? null
		: new HALLink.Builder(UriBuilder.fromUri(requestURI).replaceQueryParam("page", next).build()).build();
	expected.setFirst(firstPage);
	expected.setLast(lastPage);
	expected.setPrev(prevPage);
	expected.setNext(nextPage);
	List<TestResource> resourcesForPage = testResourcesList.subList(sublistBegin, sublistEnd);
	if (!compact) {
	    expected.setItemEmbedded(resourcesForPage);
	    return expected;
	}
	ArrayList<HALLink> resourceLinksForPage = new ArrayList<HALLink>(pageSize);
	for (TestResource resourceForPage : resourcesForPage) {
	    resourceLinksForPage.add(resourceForPage.getSelf());
	}
	expected.setItem(resourceLinksForPage);
	return expected;
    }

    protected class TestResource extends HALResource {
	private int number;

	@Override
	public URI getProfile() {
	    return URI.create("local://docs/resources/testresource");
	}

	@Override
	public MediaType getCustomJsonMediaType() {
	    return new MediaType("application", "x.testresource-v1+json");
	}

	public int getNumber() {
	    return number;
	}

	public void setNumber(int number) {
	    this.number = number;
	}
    }
}
