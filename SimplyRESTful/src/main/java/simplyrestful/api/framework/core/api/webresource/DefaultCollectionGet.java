package simplyrestful.api.framework.core.api.webresource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import simplyrestful.api.framework.core.MediaTypeUtils;
import simplyrestful.api.framework.core.QueryParamUtils;
import simplyrestful.api.framework.core.api.crud.DefaultCount;
import simplyrestful.api.framework.core.api.crud.DefaultList;
import simplyrestful.api.framework.core.hal.HALCollectionV1Builder;
import simplyrestful.api.framework.core.hal.HALCollectionV2Builder;
import simplyrestful.api.framework.resources.HALCollection;
import simplyrestful.api.framework.resources.HALCollectionV1;
import simplyrestful.api.framework.resources.HALCollectionV2;
import simplyrestful.api.framework.resources.HALResource;

@SuppressWarnings("deprecation")
public interface DefaultCollectionGet<T extends HALResource> extends WebResourceBase<T>, DefaultList<T>, DefaultCount {
    /**
     * Retrieve the paginated collection of resources.
     *
     * Unless stated otherwise, these parameters can only be used with {@link HALCollectionV2}.
     *
     * @param page      is the page number of the paginated collection of resources (for {@link HALCollectionV1} only)
     * @param pageStart is the offset at which the requested page starts.
     * @param pageSize  is the size of a single page in this paginated collection of
     *                  resources (for both {@link HALCollectionV1} and {@link HALCollectionV2})
     * @param compact   determines whether the resource in the collection only shows
     *                  its self-link (if true), or the entire resource (if false) (for {@link HALCollectionV1} only)
     * @param fields    is a list that defines which fields should be retrieved. This is only included for convenience
     * 			as it is already handled by the framework.
     * @param query     is a FIQL query that defines how the resources should be
     *                  filtered.
     * @param sort      is a list of field names on which the resources should be
     *                  sorted. This is only included for convenience as it is already handled by the framework.
     * @return the paginated collection of resources.
     */
    @GET
    @Produces({
	MEDIA_TYPE_COLLECTION_V2_JSON_QUALIFIED,
	MEDIA_TYPE_COLLECTION_V1_HAL_JSON_QUALIFIED,
	MEDIA_TYPE_COLLECTION_V2_HAL_JSON_QUALIFIED})
    @Operation(description = "Get a list of resources")
    @ApiResponse(content = {
	    @Content(
		    mediaType = MEDIA_TYPE_COLLECTION_V2_JSON_QUALIFIED,
		    schema = @Schema(
			    implementation = HALCollectionV2.class)),
	    @Content(
		    mediaType = MEDIA_TYPE_COLLECTION_V2_HAL_JSON_QUALIFIED,
		    schema = @Schema(
			    implementation = HALCollectionV2.class)),
	    @Content(
		    mediaType = MEDIA_TYPE_COLLECTION_V1_HAL_JSON_QUALIFIED,
		    schema = @Schema(
			    implementation = HALCollectionV1.class))
    })
    default HALCollection<T> listHALResources(
	    @Context
	    UriInfo uriInfo,
	    @Context
	    HttpHeaders httpHeaders,
	    @Parameter(description  = "The page to be shown", required = false)
            @QueryParam(V1_QUERY_PARAM_PAGE)
            @DefaultValue(HALCollectionV1Builder.DEFAULT_PAGE_NUMBER_STRING)
            int page,
            @Parameter(description = "The page to be shown", required = false)
	    @QueryParam(QUERY_PARAM_PAGE_START)
	    @DefaultValue(QUERY_PARAM_PAGE_START_DEFAULT)
	    int pageStart,
	    @Parameter(description = "The amount of resources shown on each page", required = false)
	    @QueryParam(QUERY_PARAM_PAGE_SIZE)
	    @DefaultValue(QUERY_PARAM_PAGE_SIZE_DEFAULT)
	    int pageSize,
	    @Parameter(description = "Provide minimal information for each resource", required = false)
            @QueryParam(V1_QUERY_PARAM_COMPACT)
            @DefaultValue(HALCollectionV1Builder.DEFAULT_COMPACT_VALUE_STRING)
            boolean compact,
            @Parameter(description = "The fields that should be retrieved", required = false)
	    @QueryParam(QUERY_PARAM_FIELDS)
	    @DefaultValue(QUERY_PARAM_FIELDS_DEFAULT)
	    List<String> fields,
	    @Parameter(description = "The FIQL query according to which the resources should be filtered", required = false)
	    @QueryParam(QUERY_PARAM_QUERY)
	    @DefaultValue(QUERY_PARAM_QUERY_DEFAULT)
	    String query,
	    @Parameter(description = "The fields on which the resources should be sorted", required = false)
	    @QueryParam(QUERY_PARAM_SORT)
	    @DefaultValue(QUERY_PARAM_SORT_DEFAULT)
	    List<String> sort) {
	String[] mediaTypesFromAnnotation = new Object(){}.getClass().getEnclosingMethod().getAnnotation(Produces.class).value();
	List<MediaType> mediaTypes = Stream.of(mediaTypesFromAnnotation)
		.map(MediaType::valueOf)
		.collect(Collectors.toList());
	MediaType selected = MediaTypeUtils.selectMediaType(mediaTypes, httpHeaders.getAcceptableMediaTypes());
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
		    getRequestURI(uriInfo),
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
		getRequestURI(uriInfo))
		.withNavigation(pageStart, pageSize)
		.collectionSize(this.count(QueryParamUtils.stripHALStructure(query)))
		.build(selected);
    }
}
