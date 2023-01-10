package simplyrestful.api.framework.webresource.api.implementation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import simplyrestful.api.framework.QueryParamUtils;
import simplyrestful.api.framework.api.crud.DefaultCount;
import simplyrestful.api.framework.api.crud.DefaultList;
import simplyrestful.api.framework.collection.APICollectionBuilder;
import simplyrestful.api.framework.resources.APICollection;
import simplyrestful.api.framework.resources.APIResource;

/**
 * Provide a default implementation for the collection resource.
 *
 * If no preference is given for the type of collection resource, this will return the most recent version of the
 * collection resource in plain JSON format, using a custom JSON media type.
 */
public interface DefaultCollectionGet<T extends APIResource> extends DefaultList<T>, DefaultCount {
	public static final String QUERY_PARAM_PAGE_START = "pageStart";
    public static final String QUERY_PARAM_PAGE_SIZE = "pageSize";
    public static final String QUERY_PARAM_FIELDS = "fields";
    public static final String QUERY_PARAM_QUERY = "query";
    public static final String QUERY_PARAM_SORT = "sort";

    public static final String QUERY_PARAM_PAGE_START_DEFAULT = "0";
    public static final String QUERY_PARAM_PAGE_SIZE_DEFAULT = "100";
    public static final String QUERY_PARAM_FIELDS_DEFAULT = "self,first,last,prev,next,total,item.self";
    public static final String QUERY_PARAM_FIELDS_ALL= "all";
    public static final String QUERY_PARAM_QUERY_DEFAULT = "";
    public static final String QUERY_PARAM_SORT_DEFAULT = "";

    /**
     * Retrieve the paginated collection of resources.
     *
     * @param requestContext is a JAX-RS context object.
     * @param resourceInfo is a JAX-RS context object.
     * @param uriInfo is a JAX-RS context object.
     * @param httpHeaders is a JAX-RS context object.
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
    @Produces(APICollection.MEDIA_TYPE_JSON)
    @Operation(description = "Retrieve a filtered, sorted collection of API resources.")
    @ApiResponse(responseCode = "200", description = "A pageable collection containing your API resources.", content = {
	     @Content(
		    mediaType = APICollection.MEDIA_TYPE_JSON,
		    schema = @Schema(
			    implementation = APICollection.class))
    })
    default APICollection<T> listAPIResources(
    		@Context
    		ContainerRequestContext requestContext,
    		@Context
		    ResourceInfo resourceInfo,
		    @Context
		    UriInfo uriInfo,
		    @Context
		    HttpHeaders httpHeaders,
		    @QueryParam(QUERY_PARAM_PAGE_START)
		    @DefaultValue(QUERY_PARAM_PAGE_START_DEFAULT)
	        @Parameter(description = "The page to be shown", required = false)
		    int pageStart,
		    @QueryParam(QUERY_PARAM_PAGE_SIZE)
		    @DefaultValue(QUERY_PARAM_PAGE_SIZE_DEFAULT)
		    @Parameter(description = "The amount of resources shown on each page", required = false)
		    int pageSize,
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
    	QueryParamUtils.configureFieldsDefault(requestContext, fields);
    	MediaType collectionType = MediaType.valueOf(APICollection.MEDIA_TYPE_JSON);
		List<T> resources = this.list(pageStart, pageSize, fields, query, QueryParamUtils.parseSort(sort));
		if(!resources.isEmpty()) {
			MediaType resourceMediaType = resources.get(0).customJsonMediaType();
			if(!collectionType.getParameters().containsKey(APICollection.MEDIA_TYPE_PARAMETER_ITEM_TYPE)) {
				Map<String, String> mediaTypeParameters = new HashMap<>(collectionType.getParameters());
				mediaTypeParameters.put(APICollection.MEDIA_TYPE_PARAMETER_ITEM_TYPE, resourceMediaType.toString());
				collectionType = new MediaType(
						collectionType.getType(),
						collectionType.getSubtype(),
						mediaTypeParameters);
			}
		}
		return APICollectionBuilder.from(resources, uriInfo.getRequestUri())
				.withNavigation(pageStart, pageSize)
				.collectionSize(this.count(query))
				.build(collectionType);
	    }


}
