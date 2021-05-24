package simplyrestful.api.framework.core.api.webresource;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import simplyrestful.api.framework.core.hal.HALCollectionV1Builder;
import simplyrestful.api.framework.resources.HALCollection;
import simplyrestful.api.framework.resources.HALCollectionV1;
import simplyrestful.api.framework.resources.HALCollectionV2;
import simplyrestful.api.framework.resources.HALResource;

/**
 * Provide a collection resource.
 *
 * If no preference is given for the type of collection resource, this will return the most recent version of the
 * collection resource in plain JSON format, using a custom JSON media type.
 */
@SuppressWarnings("deprecation")
public interface CollectionGet<T extends HALResource> {
    public static final String V1_QUERY_PARAM_PAGE = "page";
    public static final String V1_QUERY_PARAM_COMPACT = "compact";
    public static final String QUERY_PARAM_PAGE_START = "pageStart";
    public static final String QUERY_PARAM_PAGE_SIZE = "pageSize";
    public static final String QUERY_PARAM_FIELDS = "fields";
    public static final String QUERY_PARAM_QUERY = "query";
    public static final String QUERY_PARAM_SORT = "sort";

    public static final String QUERY_PARAM_PAGE_START_DEFAULT = "0";
    public static final String QUERY_PARAM_PAGE_SIZE_DEFAULT = "100";
    public static final String QUERY_PARAM_FIELDS_DEFAULT = "_links.self,_links.first,_links.last,_links.prev,_links.next,total,_embedded.item._links.self";
    public static final String QUERY_PARAM_QUERY_DEFAULT = "";
    public static final String QUERY_PARAM_SORT_DEFAULT = "";

    /**
     * Retrieve the paginated collection of resources.
     * <p>
     * Unless stated otherwise, these parameters can only be used with {@link HALCollectionV2}.
     * </p>
     * @param page      is the page number of the paginated collection of resources (for {@link HALCollectionV1} only)
     * @param pageStart is the offset at which the requested page starts.
     * @param pageSize  is the size of a single page in this paginated collection of
     *                  resources (for both {@link HALCollectionV1} and {@link HALCollectionV2})
     * @param compact   determines whether the resource in the collection only shows
     *                  its self-link (if true), or the entire resource (if false) (for {@link HALCollectionV1} only)
     * @param fields    is a list that defines which fields should be retrieved. This is only included for convenience
     * 			as it is already handled by the framework. It can be used to filter on these fields in the backend
     * 			as well, e.g. to improve performance.
     * @param query     is a FIQL query that defines how the resources should be
     *                  filtered.
     * @param sort      is a list of field names on which the resources should be
     *                  sorted. This is only included for convenience as it is already handled by the framework.
     * @return the paginated collection of resources.
     */
    @GET
    @Produces({
	HALCollectionV2.MEDIA_TYPE_HAL_JSON+";qs=0.7",
	HALCollectionV2.MEDIA_TYPE_JSON+";qs=0.9",
	HALCollectionV1.MEDIA_TYPE_HAL_JSON+";qs=0.2"
	})
    HALCollection<T> listHALResources(
	    @Context
	    ResourceInfo resourceInfo,
	    @Context
	    UriInfo uriInfo,
	    @Context
	    HttpHeaders httpHeaders,
	    @QueryParam(V1_QUERY_PARAM_PAGE)
        @DefaultValue(HALCollectionV1Builder.DEFAULT_PAGE_NUMBER_STRING)
        int page,
        @QueryParam(QUERY_PARAM_PAGE_START)
	    @DefaultValue(QUERY_PARAM_PAGE_START_DEFAULT)
	    int pageStart,
	    @QueryParam(QUERY_PARAM_PAGE_SIZE)
	    @DefaultValue(QUERY_PARAM_PAGE_SIZE_DEFAULT)
	    int pageSize,
	    @QueryParam(V1_QUERY_PARAM_COMPACT)
        @DefaultValue(HALCollectionV1Builder.DEFAULT_COMPACT_VALUE_STRING)
        boolean compact,
        @QueryParam(QUERY_PARAM_FIELDS)
	    @DefaultValue(QUERY_PARAM_FIELDS_DEFAULT)
	    List<String> fields,
	    @QueryParam(QUERY_PARAM_QUERY)
	    @DefaultValue(QUERY_PARAM_QUERY_DEFAULT)
	    String query,
	    @QueryParam(QUERY_PARAM_SORT)
	    @DefaultValue(QUERY_PARAM_SORT_DEFAULT)
	    List<String> sort);
}
