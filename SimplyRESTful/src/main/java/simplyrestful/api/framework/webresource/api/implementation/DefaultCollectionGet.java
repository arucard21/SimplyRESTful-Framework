package simplyrestful.api.framework.webresource.api.implementation;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import simplyrestful.api.framework.MediaTypeUtils;
import simplyrestful.api.framework.QueryParamUtils;
import simplyrestful.api.framework.api.crud.DefaultCount;
import simplyrestful.api.framework.api.crud.DefaultList;
import simplyrestful.api.framework.hal.HALCollectionV1Builder;
import simplyrestful.api.framework.hal.HALCollectionV2Builder;
import simplyrestful.api.framework.resources.HALCollection;
import simplyrestful.api.framework.resources.HALCollectionV1;
import simplyrestful.api.framework.resources.HALCollectionV2;
import simplyrestful.api.framework.resources.HALResource;
import simplyrestful.api.framework.webresource.api.CollectionGet;

/**
 * Provide a default implementation for the collection resource.
 */
@SuppressWarnings("deprecation")
public interface DefaultCollectionGet<T extends HALResource> extends CollectionGet<T>, DefaultList<T>, DefaultCount {
    public static final String QUERY_PARAM_FIELDS_ALL= "all";
    public static final String QUERY_PARAM_FIELDS_COMPACT = "_links.self,_links.first,_links.last,_links.prev,_links.next,total,_links.item._links.self";

    @Operation(description = "Get a list of resources")
    @ApiResponse(content = {
	    @Content(
		    mediaType = HALCollectionV2.MEDIA_TYPE_HAL_JSON,
		    schema = @Schema(
			    implementation = HALCollectionV2.class)),
	    @Content(
		    mediaType = HALCollectionV2.MEDIA_TYPE_JSON,
		    schema = @Schema(
			    implementation = HALCollectionV2.class)),
	    @Content(
		    mediaType = HALCollectionV1.MEDIA_TYPE_HAL_JSON,
		    schema = @Schema(
			    implementation = HALCollectionV1.class))
    })
    default HALCollection<T> listHALResources(
    		ContainerRequestContext requestContext,
		    ResourceInfo resourceInfo,
		    UriInfo uriInfo,
		    HttpHeaders httpHeaders,
		    @Parameter(description  = "The page to be shown", required = false)
	        int page,
	        @Parameter(description = "The page to be shown", required = false)
		    int pageStart,
		    @Parameter(description = "The amount of resources shown on each page", required = false)
		    int pageSize,
		    @Parameter(description = "Provide minimal information for each resource", required = false)
	        boolean compact,
	        @Parameter(description = "The fields that should be retrieved", required = false)
		    List<String> fields,
		    @Parameter(description = "The FIQL query according to which the resources should be filtered", required = false)
		    String query,
		    @Parameter(description = "The fields on which the resources should be sorted", required = false)
		    List<String> sort) {
    	MediaType selected = MediaTypeUtils.selectMediaType(resourceInfo, httpHeaders);
    	QueryParamUtils.configureFieldsDefault(requestContext, QueryParamUtils.stripHALStructure(fields));

		if(selected.equals(MediaType.valueOf(HALCollectionV1.MEDIA_TYPE_HAL_JSON))) {
		    int calculatedPageStart = (page -1) * pageSize;
		    if(compact) {
			fields = Collections.singletonList(QUERY_PARAM_FIELDS_DEFAULT);
		    }
		    return HALCollectionV1Builder.fromPartial(
			    this.list(
				    calculatedPageStart,
				    pageSize,
				    QueryParamUtils.stripHALStructure(fields),
				    QueryParamUtils.stripHALStructure(query),
				    QueryParamUtils.parseSort(sort)),
			    uriInfo.getRequestUri(),
			    this.count(QueryParamUtils.stripHALStructure(query)))
			    .page(page)
			    .maxPageSize(pageSize)
			    .compact(compact)
			    .build();
		}
		return HALCollectionV2Builder.from(
			this.list(
				pageStart,
				pageSize,
				QueryParamUtils.stripHALStructure(fields),
				QueryParamUtils.stripHALStructure(query),
				QueryParamUtils.parseSort(sort)),
			uriInfo.getRequestUri())
			.withNavigation(pageStart, pageSize)
			.collectionSize(this.count(QueryParamUtils.stripHALStructure(query)))
			.build(selected);
	    }
}
