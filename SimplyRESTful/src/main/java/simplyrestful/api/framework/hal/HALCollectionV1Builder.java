package simplyrestful.api.framework.hal;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import io.openapitools.jackson.dataformat.hal.HALLink;
import simplyrestful.api.framework.MediaTypeUtils;
import simplyrestful.api.framework.resources.HALCollectionV1;
import simplyrestful.api.framework.resources.HALResource;
import simplyrestful.api.framework.webresource.api.CollectionGet;

@Deprecated(since = "0.12.0")
public class HALCollectionV1Builder<T extends HALResource> {
	private static final String ERROR_MORE_RESOURCES_PROVIDED_THAN_ALLOWED = "More resources were provided than the max page size. You may want to try using HALCollectionBuilder.fromFullList.";

	protected final List<T> resources;
	protected final URI requestURI;
	protected final boolean fromFull;
	protected final long collectionSize;
	protected long page = Long.valueOf(CollectionGet.V1_QUERY_PARAM_PAGE_DEFAULT);
	protected long maxPageSize = Long.valueOf(CollectionGet.QUERY_PARAM_PAGE_SIZE_DEFAULT);
	protected boolean compact = Boolean.valueOf(CollectionGet.V1_QUERY_PARAM_COMPACT_DEFAULT);

	public static <T extends HALResource> HALCollectionV1Builder<T> fromFull(List<T> resources, URI requestURI) {
		return new HALCollectionV1Builder<T>(resources, requestURI, resources.size(), true);
	}

	public static <T extends HALResource> HALCollectionV1Builder<T> fromPartial(List<T> resources, URI requestURI, long collectionSize) {
		return new HALCollectionV1Builder<T>(resources, requestURI, collectionSize, false).maxPageSize(resources.size());
	}

	private HALCollectionV1Builder(List<T> resources, URI requestURI, long collectionSize, boolean fromFull){
		this.resources = resources;
		this.requestURI = requestURI;
		this.fromFull = fromFull;
		this.collectionSize = collectionSize;
	}

	public HALCollectionV1Builder<T> page(long page) {
		this.page = page;
		return this;
	}

	public HALCollectionV1Builder<T> maxPageSize(long maxPageSize) {
		this.maxPageSize = maxPageSize;
		return this;
	}

	public HALCollectionV1Builder<T> compact(boolean compact) {
		this.compact = compact;
		return this;
	}

	public HALCollectionV1<T> build(){
		HALCollectionV1<T> collection = new HALCollectionV1<T>();
		collection.setPage(page);
		collection.setPageSize(maxPageSize);
		collection.setTotal(collectionSize);
		if(fromFull) {
			int pageBegin = Math.toIntExact((page - 1) * maxPageSize);
			int pageEnd = Math.toIntExact(pageBegin + maxPageSize);

			if (pageEnd >= collectionSize){
				pageEnd = Math.toIntExact(collectionSize);
			}
			addResourcesToCollection(collection, resources.subList(pageBegin, pageEnd), compact);
		}
		else {
			if (resources.size() > maxPageSize) {
				throw new IllegalStateException(ERROR_MORE_RESOURCES_PROVIDED_THAN_ALLOWED);
			}
			addResourcesToCollection(collection, resources, compact);
		}

		int firstPage = 1;
		collection.setFirst(
				createHALLinkFromURIWithModifiedPageNumber(requestURI, firstPage));

		int lastPage = collectionSize == 0 ? 1 : Double.valueOf(Math.ceil(Long.valueOf(collectionSize).doubleValue() / Long.valueOf(maxPageSize).doubleValue())).intValue();
		collection.setLast(
				createHALLinkFromURIWithModifiedPageNumber(requestURI, lastPage));

		int currentPage = Math.toIntExact(page);
		if (currentPage > 1){
			int prevPage = currentPage - 1;
			collection.setPrev(
					createHALLinkFromURIWithModifiedPageNumber(requestURI, prevPage));
		}

		if (currentPage < lastPage){
			int nextPage = currentPage + 1;
			collection.setNext(
					createHALLinkFromURIWithModifiedPageNumber(requestURI, nextPage));
		}
		addSelfLink(collection, requestURI);
		return collection;
	}

	protected HALLink createHALLinkFromURIWithModifiedPageNumber(URI requestURI, int pageNumber){
		UriBuilder hrefBuilder = UriBuilder.fromUri(requestURI);
		hrefBuilder.replaceQueryParam(CollectionGet.V1_QUERY_PARAM_PAGE, pageNumber);
		HALLink link = new HALLink.Builder(hrefBuilder.build()).build();
		return link;
	}

	protected void addResourcesToCollection(HALCollectionV1<T> collection, List<T> pageResources, boolean compact) {
		if(compact){
			List<HALLink> resourcesOnPage = retrieveSelfLinks(pageResources);
			collection.setItem(resourcesOnPage);
		}
		else{
			collection.setItemEmbedded(pageResources);
		}
	}

	protected List<HALLink> retrieveSelfLinks(List<T> resources) {
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

	protected void addSelfLink(HALCollectionV1<T> collection, URI collectionURI) {
		collection.setSelf(new HALLink.Builder(collectionURI)
										.type(MediaTypeUtils.APPLICATION_HAL_JSON)
										.profile(collection.getProfile())
										.build());
	}
}
