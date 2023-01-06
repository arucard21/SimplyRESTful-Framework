package simplyrestful.api.framework.collection;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import simplyrestful.api.framework.resources.APICollectionV2;
import simplyrestful.api.framework.resources.APIResource;
import simplyrestful.api.framework.resources.Link;
import simplyrestful.api.framework.webresource.api.implementation.DefaultCollectionGet;

public class APICollectionV2Builder<T extends APIResource> {
	public static final int START_OF_FIRST_PAGE = 0;
    private final List<T> resources;
    private final URI requestURI;
    private Integer collectionSize;
    private Integer pageStart;
    private Integer pageSize;
    /**
     * Create a builder for the APICollectionV2 object.
     *
     * @param <T> is the type of resource that this collection will contain.
     * @param resources is the list of resources for the page of the collection that the APICollectionV2 object should contain.
     * @param requestURI is the request URI used to request this collection from the API.
     * @return the builder object.
     */
	public static <T extends APIResource> APICollectionV2Builder<T> from(List<T> resources, URI requestURI) {
		return new APICollectionV2Builder<T>(resources, requestURI);
	}

	private APICollectionV2Builder(List<T> resources, URI requestURI) {
		this.resources = resources;
		this.requestURI = requestURI;
	}

    /**
     * Include navigation links (first, last, prev, next) in the collection.
     *
     * The links are calculated from the offset and maximum size of the current page.
     *
     * @param pageStart is the offset used for the current page
     * @param pageSize is the maximum size of each page.
     * @return this builder object.
     */
	public APICollectionV2Builder<T> withNavigation(int pageStart, int pageSize) {
		this.pageStart = pageStart;
		this.pageSize = pageSize;
		return this;
	}

    /**
     * Specify the size of the entire collection.
     *
     * This is set as the total amount in the collection. It is also used to calculate
     * the navigation links for the next and last page.
     *
     * @param collectionSize is the size of the entire collection
     * @return this build object.
     */
	public APICollectionV2Builder<T> collectionSize(int collectionSize) {
		this.collectionSize = collectionSize;
		return this;
	}

    public APICollectionV2<T> build(MediaType type) {
    	APICollectionV2<T> collection = new APICollectionV2<T>();
    	collection.setSelf(new Link(requestURI, type));
    	collection.setItem(this.resources);

    	if(this.collectionSize != null) {
    		collection.setTotal(this.collectionSize);
    	}
    	if (shouldIncludeNavigation()) {
    	    includeNavigation(collection);
    	}
    	return collection;
    }

    private void includeNavigation(APICollectionV2<T> collection) {
		collection.setFirst(createLinkFromURIWithModifiedPageOffset(requestURI, START_OF_FIRST_PAGE));
		if (this.pageStart > 0) {
		    int startofPrevPage = this.pageStart - this.pageSize;
		    if (startofPrevPage >= 0) {
			collection.setPrev(createLinkFromURIWithModifiedPageOffset(requestURI, startofPrevPage));
		    }
		}
		if (this.collectionSize != null) {
		    int startofLastPage = calculateStartOfLastPage();
		    collection.setLast(createLinkFromURIWithModifiedPageOffset(requestURI, startofLastPage));
		    if (this.pageStart < startofLastPage) {
			int startOfNextPage = this.pageStart + this.pageSize;
			if (startOfNextPage <= startofLastPage) {
			    collection.setNext(createLinkFromURIWithModifiedPageOffset(requestURI, startOfNextPage));
			}
		    }
		}
    }

    private boolean shouldIncludeNavigation() {
    	return this.pageStart != null && this.pageSize != null;
    }

    private int calculateStartOfLastPage() {
    	int numberOfPages = Math.floorDiv(collectionSize, this.pageSize);
		if (numberOfPages > 0 && (this.collectionSize % this.pageSize) == 0) {
		    numberOfPages--;
		}
		return numberOfPages * this.pageSize;
    }

    protected Link createLinkFromURIWithModifiedPageOffset(URI requestURI, int pageStart) {
		URI modifiedUri = UriBuilder.fromUri(requestURI)
			.replaceQueryParam(DefaultCollectionGet.QUERY_PARAM_PAGE_START, pageStart)
			.build();
		return new Link(modifiedUri, null);
    }
}
