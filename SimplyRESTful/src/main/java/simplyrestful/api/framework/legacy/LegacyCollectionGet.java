package simplyrestful.api.framework.legacy;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import simplyrestful.api.framework.QueryParamUtils;
import simplyrestful.api.framework.api.crud.DefaultCount;
import simplyrestful.api.framework.api.crud.DefaultList;
import simplyrestful.api.framework.resources.HALCollection;
import simplyrestful.api.framework.resources.HALCollectionV2;
import simplyrestful.api.framework.resources.HALResource;
import simplyrestful.api.framework.webresource.api.implementation.DefaultCollectionGet;

/**
 * Provide a legacy implementation for the deprecated HALCollectionV1 resource.
 *
 * @deprecated Use {@link DefaultCollectionGet} with the {@link HALCollectionV2} resource instead.
 */
@Deprecated(since = "0.12.0")
public interface LegacyCollectionGet<T extends HALResource> extends DefaultList<T>, DefaultCount {
	public static final String V1_QUERY_PARAM_PAGE = "page";
    public static final String V1_QUERY_PARAM_COMPACT = "compact";
    public static final String V1_QUERY_PARAM_PAGE_DEFAULT = "1";
    public static final String V1_QUERY_PARAM_COMPACT_DEFAULT = "true";
    public static final String QUERY_PARAM_FIELDS_COMPACT = "_links";

    /**
     * Retrieve the paginated collection of resources as the deprecated v1 collection.
     *
     * @param requestContext is a JAX-RS context object.
     * @param uriInfo is a JAX-RS context object.
     * @param page is the page number of the paginated collection of resources
     * @param pageSize is the size of a single page in this paginated collection of resources
     * @param compact determines whether the resource in the collection only shows its self-link (if true), or the entire resource (if false)
     * @param fields is a list that defines which fields should be retrieved. This is only included for convenience as
     * it is already handled by the framework. It can be used to filter on these fields in the backend as well, e.g. to
     * improve performance. If both compact and fields are provided, compact is ignored.
     * @param query is a FIQL query that defines how the resources should be filtered.
     * @param sort is a list of field names on which the resources should be sorted. This is only included for convenience
     * as it is already handled by the framework.
     * @return the paginated collection of resources.
     */
    @GET
    @Produces(HALCollectionV1.MEDIA_TYPE_HAL_JSON)
    @Operation(description = "Get a list of resources")
    @ApiResponse(content = {
	    @Content(
		    mediaType = HALCollectionV1.MEDIA_TYPE_HAL_JSON,
		    schema = @Schema(
			    implementation = HALCollectionV1.class))
    })
    default HALCollection<T> listHALResources(
    		@Context
    		ContainerRequestContext requestContext,
    		@Context
		    UriInfo uriInfo,
		    @QueryParam(V1_QUERY_PARAM_PAGE)
	        @DefaultValue(V1_QUERY_PARAM_PAGE_DEFAULT)
		    @Parameter(description  = "The page to be shown", required = false)
	        int page,
	        @QueryParam(DefaultCollectionGet.QUERY_PARAM_PAGE_SIZE)
		    @DefaultValue(DefaultCollectionGet.QUERY_PARAM_PAGE_SIZE_DEFAULT)
		    @Parameter(description = "The amount of resources shown on each page", required = false)
		    int pageSize,
		    @QueryParam(V1_QUERY_PARAM_COMPACT)
	        @DefaultValue(V1_QUERY_PARAM_COMPACT_DEFAULT)
		    @Parameter(description = "Provide minimal information for each resource", required = false)
	        boolean compact,
	        @QueryParam(DefaultCollectionGet.QUERY_PARAM_FIELDS)
		    @DefaultValue(DefaultCollectionGet.QUERY_PARAM_FIELDS_DEFAULT)
		    @Parameter(description = "The fields that should be retrieved", required = false)
		    List<String> fields,
		    @QueryParam(DefaultCollectionGet.QUERY_PARAM_QUERY)
		    @DefaultValue(DefaultCollectionGet.QUERY_PARAM_QUERY_DEFAULT)
		    @Parameter(description = "The FIQL query according to which the resources should be filtered", required = false)
		    String query,
		    @QueryParam(DefaultCollectionGet.QUERY_PARAM_SORT)
		    @DefaultValue(DefaultCollectionGet.QUERY_PARAM_SORT_DEFAULT)
		    @Parameter(description = "The fields on which the resources should be sorted", required = false)
		    List<String> sort) {
    	if(!QueryParamUtils.fieldsQueryParamProvided(requestContext)) {
    		fields = compact ? Collections.singletonList(QUERY_PARAM_FIELDS_COMPACT) : Collections.singletonList(DefaultCollectionGet.QUERY_PARAM_FIELDS_ALL);
    	}
    	QueryParamUtils.configureFieldsDefault(requestContext, fields);
	    int calculatedPageStart = (page -1) * pageSize;
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
}
