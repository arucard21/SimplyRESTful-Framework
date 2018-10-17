package simplyrestful.api.framework.core.hal;

import java.net.URI;
import java.util.List;

import simplyrestful.api.framework.resources.HALCollection;
import simplyrestful.api.framework.resources.HALResource;

/**
 * Create a HALCollection object containing a single page of resources from a list contain only the resources that should 
 * be shown as that page.
 * 
 * You can provide the resources that you want to be shown as a page. This method will create a paged collection that 
 * contains all those resources as the page that will be shown. You can also specify whether you want the collection to 
 * show the full resource or just the resource ID of each resource.
 * 
 * @param <T> is the API resource that the HALCollection will contain.
 */
public class HALCollectionBuilderFromPartialList<T extends HALResource> extends HALCollectionBuilder<T> {
	private static final String ERROR_MORE_RESOURCES_PROVIDED_THAN_ALLOWED = "More resources were provided than allowed by the max page size. You may want to try using HALCollectionBuilderFromFullList.";

	private final long collectionSize;

	public HALCollectionBuilderFromPartialList(List<T> resources, URI requestURI, long collectionSize) {
		super(resources, requestURI);
		this.collectionSize = collectionSize;
	}

	/**
	 * Build the HALCollection based on the values provided
	 * 
	 * @return a HALCollection that contains the provided resources as the shown page.
	 */
	public HALCollection<T> build(){
		if (resources.size() > maxPageSize) {
			throw new IllegalStateException(ERROR_MORE_RESOURCES_PROVIDED_THAN_ALLOWED);
		}
		HALCollection<T> collection = new HALCollection<T>();
		collection.setPage(page);
		collection.setMaxPageSize(maxPageSize);
		collection.setTotal(collectionSize);
		addResourcesToCollection(collection, resources, compact);

		int firstPage = 1;
		collection.setFirst(
				createHALLinkFromURIWithModifiedPageNumber(requestURI, firstPage));

		int lastPage = collectionSize == 0 ? 1 : (int) Math.ceil((double) collectionSize / (double) maxPageSize);
		collection.setLast(
				createHALLinkFromURIWithModifiedPageNumber(requestURI, lastPage));

		if (page > 1){
			int prevPage = page - 1;
			collection.setPrev(
					createHALLinkFromURIWithModifiedPageNumber(requestURI, prevPage));
		}

		if (page < lastPage){
			int nextPage = page + 1;
			collection.setNext(
					createHALLinkFromURIWithModifiedPageNumber(requestURI, nextPage));
		}
		addSelfLink(collection, requestURI);
		return collection;
	}
}
