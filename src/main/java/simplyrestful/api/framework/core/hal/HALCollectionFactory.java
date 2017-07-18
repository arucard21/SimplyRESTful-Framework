package simplyrestful.api.framework.core.hal;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import dk.nykredit.jackson.dataformat.hal.HALLink;
import simplyrestful.api.framework.core.ApiEndpointBase;

public class HALCollectionFactory<T extends HALResource> {

	/**
	 * Create a HALCollection object containing a single page of resources from the full list of those resources.
	 *
	 * You can provide the full list of resources that are available and this method will retrieve the correct resources
	 * that should be shown on the page that is requested, with the given page size. You can also specify whether you
	 * want the collection to show the full resource or just the resource ID of each resource.
	 *
	 * @param allResources is a List containing all resources for which a paged collection is requested.
	 * @param page is the number of the page for which we want the resources to be shown.
	 * @param pageSize is the size of each page.
	 * @param requestURI is the URI on which the request is made, which is used to provide proper navigation links (like prev and next).
	 * @param compact determines whether the collection shows a list containing the entire resource or just the resource ID.
	 * @return a HALCollection that contains the resources for the page that has been requested.
	 */
	public HALCollection<T> createPagedCollectionFromFullList(List<T> allResources, int page, int pageSize, URI requestURI, boolean compact){
		int collectionSize = allResources.size();
		HALCollection<T> collection = new HALCollection<T>();
		collection.setPage(page);
		collection.setPageSize(pageSize);
		collection.setTotal(collectionSize);

		int pageBegin = ((page - 1) * pageSize);
		int pageEnd = pageBegin + pageSize;
		if (pageEnd >= collectionSize){
			pageEnd = collectionSize;
		}
		addResourcesToCollection(collection, allResources.subList(pageBegin, pageEnd), compact);

		int firstPage = 1;
		collection.setFirst(
				createHALLinkFromURIWithModifiedPageNumber(requestURI, firstPage));

		int lastPage = collectionSize == 0 ? 1 : (int) Math.ceil((double) collectionSize / (double) pageSize);
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

	/**
	 * Create a HALCollection object containing a single page of resources from a list contain only the resources that
	 * should be shown as that page.
	 *
	 * You can provide the resources that you want to be shown as a page. This method will create a paged collection
	 * that contains all those resources as the page that will be shown. You can also specify whether you
	 * want the collection to show the full resource or just the resource ID of each resource.
	 *
	 * @param resourcesForPage is a List containing only the resource that you wish to display on the page of your paged collection.
	 * @param page is the number of the page that is represented by the provided list of resources.
	 * @param collectionSize is the size of the full collection of resources.
	 * @param requestURI is the URI on which the request is made, which is used to provide proper absolute links (like self, prev and next).
	 * @param compact determines whether the collection shows a list containing the entire resource or just the resource ID.
	 * @return a HALCollection that contains the provided resources as the shown page.
	 */
	public HALCollection<T> createPagedCollectionFromPartialList(List<T> resourcesForPage, int page, int collectionSize, URI requestURI, boolean compact){
		int pageSize = resourcesForPage.size();
		HALCollection<T> collection = new HALCollection<T>();
		collection.setPage(page);
		collection.setPageSize(pageSize);
		collection.setTotal(collectionSize);
		addResourcesToCollection(collection, resourcesForPage, compact);

		int firstPage = 1;
		collection.setFirst(
				createHALLinkFromURIWithModifiedPageNumber(requestURI, firstPage));

		int lastPage = collectionSize == 0 ? 1 : (int) Math.ceil((double) collectionSize / (double) pageSize);
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

	private HALLink createHALLinkFromURIWithModifiedPageNumber(URI requestURI, int pageNumber){
		UriBuilder hrefBuilder = UriBuilder.fromUri(requestURI);
		hrefBuilder.replaceQueryParam(ApiEndpointBase.QUERY_PARAM_PAGE, pageNumber);
		HALLink link = new HALLink.Builder(hrefBuilder.build()).build();
		return link;
	}

	private void addResourcesToCollection(HALCollection<T> collection, List<T> pageResources, boolean compact) {
		if(compact){
			List<HALLink> resourcesOnPage = retrieveSelfLinks(pageResources);
			collection.setItem(resourcesOnPage);
		}
		else{
			collection.setItemEmbedded(pageResources);
		}
	}

	private List<HALLink> retrieveSelfLinks(List<T> resources) {
		List<HALLink> resourcesOnPage = new ArrayList<HALLink>();
		for(T resource: resources){
			HALLink selfLink = resource.getSelf();
			if (selfLink == null){
				throw new IllegalStateException("Encountered HAL resource without an ID");
			}
			resourcesOnPage.add(selfLink);

		}
		return resourcesOnPage;
	}

	private void addSelfLink(HALCollection<T> collection, URI collectionURI) {
		collection.setSelf(new HALLink.Builder(collectionURI)
										.type(MediaType.APPLICATION_JSON)
										.profile(collection.getProfile())
										.build());
	}
}
