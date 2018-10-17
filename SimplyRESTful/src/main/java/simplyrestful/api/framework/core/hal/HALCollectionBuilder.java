package simplyrestful.api.framework.core.hal;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import io.openapitools.jackson.dataformat.hal.HALLink;
import simplyrestful.api.framework.core.AbstractWebResource;
import simplyrestful.api.framework.core.MediaType;
import simplyrestful.api.framework.resources.HALCollection;
import simplyrestful.api.framework.resources.HALResource;

public abstract class HALCollectionBuilder<T extends HALResource> {
	public static final String DEFAULT_PAGE_NUMBER_STRING = "1";
	public static final int DEFAULT_PAGE_NUMBER = Integer.parseInt(DEFAULT_PAGE_NUMBER_STRING);
	public static final String DEFAULT_MAX_PAGESIZE_STRING = "100";
	public static final int DEFAULT_MAX_PAGESIZE = Integer.parseInt(DEFAULT_MAX_PAGESIZE_STRING);
	public static final String DEFAULT_COMPACT_VALUE_STRING = "true";
	public static final boolean DEFAULT_COMPACT_VALUE = Boolean.parseBoolean(DEFAULT_COMPACT_VALUE_STRING);
	
	protected final List<T> resources;
	protected final URI requestURI;
	protected int page = DEFAULT_PAGE_NUMBER;
	protected int maxPageSize = DEFAULT_MAX_PAGESIZE; 
	protected boolean compact = DEFAULT_COMPACT_VALUE;
	
	public HALCollectionBuilder(List<T> resources, URI requestURI){
		this.resources = resources;
		this.requestURI = requestURI;
	}
	
	public HALCollectionBuilder<T> page(int page) {
		this.page = page;
		return this;
	}
	
	public HALCollectionBuilder<T> maxPageSize(int maxPageSize) {
		this.maxPageSize = maxPageSize;
		return this;
	}
	
	public HALCollectionBuilder<T> compact(boolean compact) {
		this.compact = compact;
		return this;
	}
	
	public abstract HALCollection<T> build();

	protected HALLink createHALLinkFromURIWithModifiedPageNumber(URI requestURI, int pageNumber){
		UriBuilder hrefBuilder = UriBuilder.fromUri(requestURI);
		hrefBuilder.replaceQueryParam(AbstractWebResource.QUERY_PARAM_PAGE, pageNumber);
		HALLink link = new HALLink.Builder(hrefBuilder.build()).build();
		return link;
	}

	protected void addResourcesToCollection(HALCollection<T> collection, List<T> pageResources, boolean compact) {
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

	protected void addSelfLink(HALCollection<T> collection, URI collectionURI) {
		collection.setSelf(new HALLink.Builder(collectionURI)
										.type(MediaType.APPLICATION_HAL_JSON)
										.profile(collection.getProfile())
										.build());
	}
}
