package simplyrestful.api.framework.webresource.api.implementation;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
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

/**
 * Provide a default implementation for the collection resource.
 *
 * If no preference is given for the type of collection resource, this will return the most recent version of the
 * collection resource in plain JSON format, using a custom JSON media type.
 */
public interface DefaultCollectionGet<T extends HALResource> extends DefaultList<T>, DefaultCount {
	public static final String V1_QUERY_PARAM_PAGE = "page";
    public static final String V1_QUERY_PARAM_COMPACT = "compact";
    public static final String QUERY_PARAM_PAGE_START = "pageStart";
    public static final String QUERY_PARAM_PAGE_SIZE = "pageSize";
    public static final String QUERY_PARAM_FIELDS = "fields";
    public static final String QUERY_PARAM_QUERY = "query";
    public static final String QUERY_PARAM_SORT = "sort";

    public static final String QUERY_PARAM_PAGE_START_DEFAULT = "0";
    public static final String V1_QUERY_PARAM_PAGE_DEFAULT = "1";
    public static final String QUERY_PARAM_PAGE_SIZE_DEFAULT = "100";
    public static final String V1_QUERY_PARAM_COMPACT_DEFAULT = "true";
    public static final String QUERY_PARAM_FIELDS_DEFAULT = "_links.self,_links.first,_links.last,_links.prev,_links.next,total,_embedded.item._links.self";
    public static final String QUERY_PARAM_FIELDS_ALL= "all";
    public static final String QUERY_PARAM_FIELDS_COMPACT = "_links.self,_links.first,_links.last,_links.prev,_links.next,total,_links.item._links.self";
    public static final String QUERY_PARAM_QUERY_DEFAULT = "";
    public static final String QUERY_PARAM_SORT_DEFAULT = "";

    /**
     * Retrieve the paginated collection of resources.
     * <p>
     * Unless stated otherwise, these parameters can only be used with {@link HALCollectionV2}.
     * </p>
     * @param requestContext is a JAX-RS context object.
     * @param resourceInfo is a JAX-RS context object.
     * @param uriInfo is a JAX-RS context object.
     * @param httpHeaders is a JAX-RS context object.
     * @param page is the page number of the paginated collection of resources (for {@link HALCollectionV1} only)
     * @param pageStart is the offset at which the requested page starts.
     * @param pageSize is the size of a single page in this paginated collection of
     *                  resources (for both {@link HALCollectionV1} and {@link HALCollectionV2})
     * @param compact determines whether the resource in the collection only shows
     *                  its self-link (if true), or the entire resource (if false) (for {@link HALCollectionV1} only)
     * @param fields is a list that defines which fields should be retrieved. This is only included for convenience as
     * it is already handled by the framework. It can be used to filter on these fields in the backend as well, e.g. to
     * improve performance.
     * @param query is a FIQL query that defines how the resources should be filtered.
     * @param sort is a list of field names on which the resources should be sorted. This is only included for convenience
     * as it is already handled by the framework.
     * @return the paginated collection of resources.
     */
    @GET
    @Produces({
	HALCollectionV2.MEDIA_TYPE_HAL_JSON+";qs=0.7",
	HALCollectionV2.MEDIA_TYPE_JSON+";qs=0.9",
	HALCollectionV1.MEDIA_TYPE_HAL_JSON+";qs=0.2"
	})
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
    		@Context
    		ContainerRequestContext requestContext,
    		@Context
		    ResourceInfo resourceInfo,
		    @Context
		    UriInfo uriInfo,
		    @Context
		    HttpHeaders httpHeaders,
		    @QueryParam(V1_QUERY_PARAM_PAGE)
	        @DefaultValue(V1_QUERY_PARAM_PAGE_DEFAULT)
		    @Parameter(description  = "The page to be shown", required = false)
	        int page,
	        @QueryParam(QUERY_PARAM_PAGE_START)
		    @DefaultValue(QUERY_PARAM_PAGE_START_DEFAULT)
	        @Parameter(description = "The page to be shown", required = false)
		    int pageStart,
		    @QueryParam(QUERY_PARAM_PAGE_SIZE)
		    @DefaultValue(QUERY_PARAM_PAGE_SIZE_DEFAULT)
		    @Parameter(description = "The amount of resources shown on each page", required = false)
		    int pageSize,
		    @QueryParam(V1_QUERY_PARAM_COMPACT)
	        @DefaultValue(V1_QUERY_PARAM_COMPACT_DEFAULT)
		    @Parameter(description = "Provide minimal information for each resource", required = false)
	        boolean compact,
	        @QueryParam(QUERY_PARAM_FIELDS)
		    @DefaultValue(QUERY_PARAM_FIELDS_DEFAULT)
		    @Parameter(description = "The fields that should be retrieved", required = false)
		    List<String> fields,
		    @QueryParam(QUERY_PARAM_QUERY)
		    @DefaultValue(QUERY_PARAM_QUERY_DEFAULT)
		    @Parameter(description = "The FIQL query according to which the resources should be filtered", required = false)
		    String query,
		    @QueryParam(QUERY_PARAM_SORT)
		    @DefaultValue(QUERY_PARAM_SORT_DEFAULT)
		    @Parameter(description = "The fields on which the resources should be sorted", required = false)
		    List<String> sort) {
    	MediaType selected = MediaTypeUtils.selectMediaType(resourceInfo, httpHeaders);
    	if(selected.equals(MediaType.valueOf(HALCollectionV2.MEDIA_TYPE_JSON))) {
    		QueryParamUtils.configureFieldsDefault(requestContext, QueryParamUtils.stripHALStructure(fields));
    	}
    	else {
    		QueryParamUtils.configureFieldsDefault(requestContext, fields);
    	}

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
