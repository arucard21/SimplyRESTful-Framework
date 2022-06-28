package simplyrestful.api.framework.hal;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import io.openapitools.jackson.dataformat.hal.HALLink;
import simplyrestful.api.framework.MediaTypeUtils;
import simplyrestful.api.framework.resources.HALCollectionV2;
import simplyrestful.api.framework.resources.HALResource;
import simplyrestful.api.framework.webresource.api.implementation.DefaultCollectionGet;

public class HALCollectionV2Builder<T extends HALResource> {
	public static final int START_OF_FIRST_PAGE = 0;
    private final List<T> resources;
    private final URI requestURI;
    private Integer collectionSize;
    private Integer pageStart;
    private Integer pageSize;
    /**
     * Create a builder for the HALCollectionV2 object.
     *
     * @param <T> is the type of resource that this collection will contain.
     * @param resources is the list of resources for the page of the collection that the HALCollectionV2 object should contain.
     * @param requestURI is the request URI used to request this collection from the API.
     * @return the builder object.
     */
	public static <T extends HALResource> HALCollectionV2Builder<T> from(List<T> resources, URI requestURI) {
		return new HALCollectionV2Builder<T>(resources, requestURI);
	}

	private HALCollectionV2Builder(List<T> resources, URI requestURI) {
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
	public HALCollectionV2Builder<T> withNavigation(int pageStart, int pageSize) {
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
	public HALCollectionV2Builder<T> collectionSize(int collectionSize) {
		this.collectionSize = collectionSize;
		return this;
	}

    public HALCollectionV2<T> build(MediaType type) {
    	HALCollectionV2<T> collection = new HALCollectionV2<T>();
    	collection.setSelf(createLink(requestURI, type, collection.getProfile()));

    	setResourceSelfMediaTypeSimilarToCollectionMediaType(type);
    	collection.setItem(this.resources);

    	if(this.collectionSize != null) {
    		collection.setTotal(this.collectionSize);
    	}
    	if (shouldIncludeNavigation()) {
    	    includeNavigation(collection);
    	}
    	return collection;
    }

    /**
     * Since the resources will be serialized together with the collection, the self link
     * for each resource should reflect this. So if the collection is serialized as HAL+JSON,
     * the resources will also be serialized as HAL+JSON and the self-link should reflect that.
     * Similarly, if serialized to plain JSON (using the custom JSON media type), then the
     * resource self links should use their own custom JSON media type as well.
     *
     * @param type is the media type of the collection
     */
    private void setResourceSelfMediaTypeSimilarToCollectionMediaType(MediaType type) {
        if(this.resources.isEmpty()) {
            return;
        }
    	MediaType resourceType;
    	URI resourceProfile;
    	T resourceFromList = this.resources.stream().findAny().orElseThrow();
    	if(MediaTypeUtils.APPLICATION_HAL_JSON_TYPE.isCompatible(type)) {
    	    resourceType = MediaTypeUtils.APPLICATION_HAL_JSON_TYPE;
    	    resourceProfile = resourceFromList.getProfile();
    	}
    	else {
    	    resourceType = resourceFromList.getCustomJsonMediaType();
    	    resourceProfile = null;
    	}
		this.resources.stream().forEach(resource -> {
			URI selfLink = UriBuilder.fromUri(resource.getSelf().getHref())
					.replaceQuery(null)
					.replaceMatrix(null)
					.fragment(null)
					.build();
			resource.setSelf(createLink(selfLink, resourceType, resourceProfile));
		});
    }

    private void includeNavigation(HALCollectionV2<T> collection) {
		collection.setFirst(createHALLinkFromURIWithModifiedPageOffset(requestURI, START_OF_FIRST_PAGE));
		if (this.pageStart > 0) {
		    int startofPrevPage = this.pageStart - this.pageSize;
		    if (startofPrevPage >= 0) {
			collection.setPrev(createHALLinkFromURIWithModifiedPageOffset(requestURI, startofPrevPage));
		    }
		}
		if (this.collectionSize != null) {
		    int startofLastPage = calculateStartOfLastPage();
		    collection.setLast(createHALLinkFromURIWithModifiedPageOffset(requestURI, startofLastPage));
		    if (this.pageStart < startofLastPage) {
			int startOfNextPage = this.pageStart + this.pageSize;
			if (startOfNextPage <= startofLastPage) {
			    collection.setNext(createHALLinkFromURIWithModifiedPageOffset(requestURI, startOfNextPage));
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

    protected HALLink createHALLinkFromURIWithModifiedPageOffset(URI requestURI, int pageStart) {
		URI modifiedUri = UriBuilder.fromUri(requestURI)
			.replaceQueryParam(DefaultCollectionGet.QUERY_PARAM_PAGE_START, pageStart)
			.build();
		return new HALLink.Builder(modifiedUri).build();
    }

    protected HALLink createLink(URI collectionUri, MediaType collectionType, URI collectionProfileUri) {
		HALLink.Builder builder = new HALLink.Builder(collectionUri);
		if(collectionType.isCompatible(MediaTypeUtils.APPLICATION_HAL_JSON_TYPE)) {
		    builder
		    .type(MediaTypeUtils.APPLICATION_HAL_JSON)
		    .profile(collectionProfileUri);
		}
		else {
		    builder.type(collectionType.toString());
		}
		return builder.build();
    }
}
