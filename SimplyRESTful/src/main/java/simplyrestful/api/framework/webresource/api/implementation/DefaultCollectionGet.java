package simplyrestful.api.framework.webresource.api.implementation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import simplyrestful.api.framework.api.crud.ResourceCount;
import simplyrestful.api.framework.api.crud.ResourceList;
import simplyrestful.api.framework.collection.ApiCollectionBuilder;
import simplyrestful.api.framework.resources.ApiCollection;
import simplyrestful.api.framework.resources.ApiResource;
import simplyrestful.api.framework.utils.QueryParamUtils;

/**
 * Provide a default implementation for retrieving the collection resource.
 *
 * If no preference is given for the type of collection resource, this will return the most recent version of the
 * collection resource in plain JSON format, using a custom JSON media type.
 *
 * @param <T> is the API resource class used in the JAX-RS WebResource, which will be contained in the collection resource.
 */
public interface DefaultCollectionGet<T extends ApiResource> extends ResourceList<T>, ResourceCount {
	/**
	 * The name of the query parameter for the starting offset for the page that is contained in the collection.
	 */
	public static final String QUERY_PARAM_PAGE_START = "pageStart";
	/**
	 * The name of the query parameter for the size of the page that is contained in the collection.
	 */
    public static final String QUERY_PARAM_PAGE_SIZE = "pageSize";
    /**
     * The name of the query parameter for the list of fields from the collection resource that should be shown.
     */
    public static final String QUERY_PARAM_FIELDS = "fields";
    /**
     * The name of the query parameter for the FIQL query that filters the items included in the collection.
     */
    public static final String QUERY_PARAM_QUERY = "query";
    /**
     * The name of the query for the field on which the collection should be sorted, including a sort direction.
     */
    public static final String QUERY_PARAM_SORT = "sort";
    /**
     * The default value for the "pageStart" query parameter
     */
    public static final String QUERY_PARAM_PAGE_START_DEFAULT = "0";
    /**
     * The default value for the "pageSize" query parameter
     */
    public static final String QUERY_PARAM_PAGE_SIZE_DEFAULT = "100";
    /**
     * The default value for the "query" query parameter
     */
    public static final String QUERY_PARAM_QUERY_DEFAULT = "";
    /**
     * The default value for the "sort" query parameter
     */
    public static final String QUERY_PARAM_SORT_DEFAULT = "";

    /**
     * Retrieve the paginated collection of resources.
     *
     * @param uriInfo is a JAX-RS context object.
     * @param pageStart is the offset at which the requested page starts.
     * @param pageSize is the size of a single page in this paginated collection of resources
     * @param fields is a list that defines which fields should be retrieved. This is only included for convenience as
     * it is already handled by the framework. It can be used to filter on these fields in the backend as well, e.g. to
     * improve performance.
     * @param query is a FIQL query that defines how the resources should be filtered.
     * @param sort is a list of field names on which the resources should be sorted. This is only included for convenience
     * as it is already handled by the framework.
     * @return the paginated collection of resources.
     */
    @GET
    @Produces(ApiCollection.MEDIA_TYPE_JSON)
    @Operation(description = "Retrieve a filtered, sorted collection of API resources.")
    default ApiCollection<T> listAPIResources(
    		@Context
		    UriInfo uriInfo,
		    @QueryParam(QUERY_PARAM_PAGE_START)
		    @DefaultValue(QUERY_PARAM_PAGE_START_DEFAULT)
	        @Parameter(description = "The page to be shown", required = false)
		    int pageStart,
		    @QueryParam(QUERY_PARAM_PAGE_SIZE)
		    @DefaultValue(QUERY_PARAM_PAGE_SIZE_DEFAULT)
		    @Parameter(description = "The amount of resources shown on each page", required = false)
		    int pageSize,
		    @QueryParam(QUERY_PARAM_FIELDS)
		    @DefaultValue(ApiCollection.FIELDS_VALUE_DEFAULT)
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
    	MediaType collectionType = MediaType.valueOf(ApiCollection.MEDIA_TYPE_JSON);
		List<T> resources = this.list(pageStart, pageSize, fields, query, QueryParamUtils.parseSort(sort));
		if(!resources.isEmpty()) {
			MediaType resourceMediaType = resources.get(0).customJsonMediaType();
			if(!collectionType.getParameters().containsKey(ApiCollection.MEDIA_TYPE_PARAMETER_ITEM_TYPE)) {
				Map<String, String> mediaTypeParameters = new HashMap<>(collectionType.getParameters());
				mediaTypeParameters.put(ApiCollection.MEDIA_TYPE_PARAMETER_ITEM_TYPE, resourceMediaType.toString());
				collectionType = new MediaType(
						collectionType.getType(),
						collectionType.getSubtype(),
						mediaTypeParameters);
			}
		}
		return ApiCollectionBuilder.from(resources, uriInfo.getRequestUri())
				.withNavigation(pageStart, pageSize)
				.collectionSize(this.count(query))
				.build(collectionType);
	    }


}
