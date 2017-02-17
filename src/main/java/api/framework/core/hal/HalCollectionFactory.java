package api.framework.core.hal;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import api.framework.core.ApiEndpointBase;
import dk.nykredit.jackson.dataformat.hal.HALLink;

public class HalCollectionFactory<T extends HalResource> {

	public HalCollection<T> createPagedCollection(List<T> allResources, int page, int pageSize, URI requestURI, boolean compact){
		int collectionSize = allResources.size();
		HalCollection<T> collection = new HalCollection<T>();
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
				createHalLinkFromURIWithModifiedPageNumber(requestURI, firstPage));

		int lastPage = (int) Math.ceil((double) collectionSize / (double) pageSize);
		collection.setLast(
				createHalLinkFromURIWithModifiedPageNumber(requestURI, lastPage));

		if (page > 1){
			int prevPage = page - 1;
			collection.setPrev(
					createHalLinkFromURIWithModifiedPageNumber(requestURI, prevPage));
		}

		if (page < lastPage){
			int nextPage = page + 1;
			collection.setNext(
					createHalLinkFromURIWithModifiedPageNumber(requestURI, nextPage));
		}
		return collection;
	}

	private HALLink createHalLinkFromURIWithModifiedPageNumber(URI requestURI, int pageNumber){
		UriBuilder hrefBuilder = UriBuilder.fromUri(requestURI);
		hrefBuilder.replaceQueryParam(ApiEndpointBase.QUERY_PARAM_PAGE, pageNumber);
		HALLink link = new HALLink.Builder(hrefBuilder.build()).build();
		return link;
	}

	private void addResourcesToCollection(HalCollection<T> collection, List<T> pageResources, boolean compact) {
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
}
