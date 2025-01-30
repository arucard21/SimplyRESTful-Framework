package simplyrestful.api.framework.collection;

import java.net.URI;
import java.util.List;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import simplyrestful.api.framework.resources.ApiCollection;
import simplyrestful.api.framework.resources.ApiResource;
import simplyrestful.api.framework.resources.Link;
import simplyrestful.api.framework.webresource.api.implementation.DefaultCollectionGet;

/**
 * A builder for the APICollectionV2 object.
 *
 * @param <T> is the type of resource that this collection will contain.
 */
public class ApiCollectionBuilder<T extends ApiResource> {
	/**
	 * The value indicating the offset for the first page of the collection.
	 */
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
	public static <T extends ApiResource> ApiCollectionBuilder<T> from(List<T> resources, URI requestURI) {
		return new ApiCollectionBuilder<T>(resources, requestURI);
	}

	private ApiCollectionBuilder(List<T> resources, URI requestURI) {
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
	public ApiCollectionBuilder<T> withNavigation(int pageStart, int pageSize) {
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
	public ApiCollectionBuilder<T> collectionSize(int collectionSize) {
		this.collectionSize = collectionSize;
		return this;
	}

	/**
	 * Create the ApiCollection according to the builder.
	 *
	 * @param type is the media type for this collection, which can include the "item-type" media type parameter
	 * indicating the media type of the API resource contained in the collection.
	 * @return the ApiCollection created according to the builder.
	 */
    public ApiCollection<T> build(MediaType type) {
    	ApiCollection<T> collection = new ApiCollection<T>();
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

    private void includeNavigation(ApiCollection<T> collection) {
    	MediaType collectionType = collection.getSelf().getType();

		collection.setFirst(createLinkFromURIWithModifiedPageOffset(requestURI, START_OF_FIRST_PAGE, collectionType));
		if (this.pageStart > 0) {
		    int startofPrevPage = this.pageStart - this.pageSize;
		    if (startofPrevPage >= 0) {
			collection.setPrev(createLinkFromURIWithModifiedPageOffset(requestURI, startofPrevPage, collectionType));
		    }
		}
		if (this.collectionSize != null) {
		    int startofLastPage = calculateStartOfLastPage();
		    collection.setLast(createLinkFromURIWithModifiedPageOffset(requestURI, startofLastPage, collectionType));
		    if (this.pageStart < startofLastPage) {
			int startOfNextPage = this.pageStart + this.pageSize;
			if (startOfNextPage <= startofLastPage) {
			    collection.setNext(createLinkFromURIWithModifiedPageOffset(requestURI, startOfNextPage, collectionType));
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

    /**
     * Create a Link object for the provided URI and media type, updating the "pageStart" parameter in the URI with the provided value.
     *
     * @param requestURI is a URI containing the "pageStart" parameter.
     * @param pageStart is the value to which the "pageStart" parameter in the URI should be updated.
     * @param type is the media type of the resource at the provided URI, which should typically be an API collection.
     * @return the created Link object.
     */
    protected Link createLinkFromURIWithModifiedPageOffset(URI requestURI, int pageStart, MediaType type) {
		URI modifiedUri = UriBuilder.fromUri(requestURI)
			.replaceQueryParam(DefaultCollectionGet.QUERY_PARAM_PAGE_START, pageStart)
			.build();
		return new Link(modifiedUri, type);
    }
}
