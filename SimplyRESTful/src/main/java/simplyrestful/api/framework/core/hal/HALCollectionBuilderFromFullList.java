package simplyrestful.api.framework.core.hal;

import java.net.URI;
import java.util.List;

import simplyrestful.api.framework.resources.HALCollection;
import simplyrestful.api.framework.resources.HALResource;

/**
 * Build a HALCollection object containing a single page of resources from the full list of those resources.
 * You can provide the full list of resources that are available and this method will retrieve the correct resources
 * that should be shown on the page that is requested, with the given page size. You can also specify whether you
 * want the collection to show the full resource or just the resource ID of each resource.
 * 
 * @param <T> is the API resource that the HALCollection will contain.
 */
public class HALCollectionBuilderFromFullList<T extends HALResource> extends HALCollectionBuilder<T> {
	public HALCollectionBuilderFromFullList(List<T> resources, URI requestURI) {
		super(resources, requestURI);
	}

	/**
	 * Build the HALCollection based on the values provided
	 *
	 * @return a HALCollection that contains the resources for the page that has been requested.
	 */
	public HALCollection<T> build(){
		int collectionSize = resources.size();
		HALCollection<T> collection = new HALCollection<T>();
		collection.setPage(page);
		collection.setMaxPageSize(maxPageSize);
		collection.setTotal(collectionSize);

		int pageBegin = ((page - 1) * maxPageSize);
		int pageEnd = pageBegin + maxPageSize;
		if (pageEnd >= collectionSize){
			pageEnd = collectionSize;
		}
		addResourcesToCollection(collection, resources.subList(pageBegin, pageEnd), compact);

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
