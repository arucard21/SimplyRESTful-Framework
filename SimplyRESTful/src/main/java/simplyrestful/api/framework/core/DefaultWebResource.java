package simplyrestful.api.framework.core;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;

import io.openapitools.jackson.dataformat.hal.HALLink;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import simplyrestful.api.framework.core.hal.HALCollectionV1Builder;
import simplyrestful.api.framework.core.hal.HALCollectionV2Builder;
import simplyrestful.api.framework.resources.HALCollection;
import simplyrestful.api.framework.resources.HALCollectionV1;
import simplyrestful.api.framework.resources.HALCollectionV2;
import simplyrestful.api.framework.resources.HALResource;

public abstract class DefaultWebResource<T extends HALResource> {
    private static final String ERROR_SELF_LINK_ID_DOES_NOT_MATCH_PROVIDED_ID = "The provided resource contains an self-link that does not match the ID used in the request";
    private static final String ERROR_SELF_LINK_URI_DOES_NOT_MATCH_API_BASE_URI = "The identifier of the resource does not correspond to the base URI of this Web Resource";
    
    private static final String MEDIA_TYPE_QUALIFIED_COLLECTION_V2_JSON = HALCollectionV2.MEDIA_TYPE_JSON+";qs=1.0";
    private static final String MEDIA_TYPE_QUALIFIED_COLLECTION_V2_HAL_JSON = HALCollectionV2.MEDIA_TYPE_HAL_JSON+";qs=0.75";
    private static final String MEDIA_TYPE_QUALIFIED_COLLECTION_V1_HAL_JSON = HALCollectionV1.MEDIA_TYPE_HAL_JSON+";qs=0.25";

    public static final String V1_QUERY_PARAM_PAGE = "page";
    public static final String V1_QUERY_PARAM_COMPACT = "compact";
    public static final String QUERY_PARAM_PAGE_START = "pageStart";
    public static final String QUERY_PARAM_PAGE_SIZE = "pageSize";
    public static final String QUERY_PARAM_FIELDS = "fields";
    public static final String QUERY_PARAM_FIELDS_DELIMITER = ",";
    public static final String QUERY_PARAM_QUERY = "query";
    public static final String QUERY_PARAM_SORT = "sort";
    public static final String QUERY_PARAM_SORT_DELIMITER = ",";
    public static final String QUERY_PARAM_SORT_ORDER_DELIMITER = ":";
    
    public static final String QUERY_PARAM_PAGE_START_DEFAULT = "0";
    public static final String QUERY_PARAM_PAGE_SIZE_DEFAULT = "100";
    public static final String QUERY_PARAM_FIELDS_COLLECTION_DEFAULT = "first,last,prev,next,total,item.self";
    public static final String QUERY_PARAM_FIELDS_RESOURCE_DEFAULT = "all";
    public static final String QUERY_PARAM_QUERY_DEFAULT = "";
    public static final String QUERY_PARAM_SORT_DEFAULT = "";
    
    
    @Context
    protected UriInfo uriInfo;
    
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
     * @param pageSize  is the requested size of each page.
     * @param fields    is a list that defines which fields should be retrieved.
     * @param query     is a FIQL query that defines how the resources should be
     *                  filtered.
     * @param sort      is a list of field names on which the resources should be
     *                  sorted.
     * @return the paginated collection of resources.
     */
    @GET
    @Produces({
	MEDIA_TYPE_QUALIFIED_COLLECTION_V2_HAL_JSON,
	MEDIA_TYPE_QUALIFIED_COLLECTION_V1_HAL_JSON})
    @ApiOperation(value = "Get a list of resources", notes = "Get a list of resources")
    public HALCollection<T> getHALResources(
	    @Context
	    Request request,
	    @ApiParam(value = "The page to be shown", required = false)
            @QueryParam(V1_QUERY_PARAM_PAGE) 
            @DefaultValue(HALCollectionV1Builder.DEFAULT_PAGE_NUMBER_STRING) 
            int page,
            @ApiParam(value = "The amount of resources shown on each page", required = false)
            @QueryParam(QUERY_PARAM_PAGE_SIZE)
            @DefaultValue(HALCollectionV1Builder.DEFAULT_MAX_PAGESIZE_STRING)
            int pageSize,
            @ApiParam(value = "Provide minimal information for each resource", required = false)
            @QueryParam(V1_QUERY_PARAM_COMPACT)
            @DefaultValue(HALCollectionV1Builder.DEFAULT_COMPACT_VALUE_STRING)
            boolean compact,
            @ApiParam(value = "The page to be shown", required = false)
	    @QueryParam(QUERY_PARAM_PAGE_START)
	    @DefaultValue(QUERY_PARAM_PAGE_START_DEFAULT)
	    int pageStart,
	    @ApiParam(value = "The fields that should be included", required = false)
	    @QueryParam(QUERY_PARAM_FIELDS)
	    @DefaultValue(QUERY_PARAM_FIELDS_COLLECTION_DEFAULT)
	    List<String> fields,
	    @ApiParam(value = "The FIQL query according to which the resources should be filtered", required = false)
	    @QueryParam(QUERY_PARAM_QUERY)
	    @DefaultValue(QUERY_PARAM_QUERY_DEFAULT)
	    String query,
	    @ApiParam(value = "The fields on which the resources should be sorted", required = false)
	    @QueryParam(QUERY_PARAM_SORT)
	    @DefaultValue(QUERY_PARAM_SORT_DEFAULT)
	    List<String> sort) {
	MediaType v2HALJson = MediaType.valueOf(MEDIA_TYPE_QUALIFIED_COLLECTION_V2_HAL_JSON);
	MediaType v1HALJson = MediaType.valueOf(MEDIA_TYPE_QUALIFIED_COLLECTION_V1_HAL_JSON);
	MediaType selected = request.selectVariant(Variant.mediaTypes(v2HALJson, v1HALJson).build()).getMediaType();

	if(selected.equals(v1HALJson)) {
	    verifyNonV1ParametersNotUsedOnV1(pageStart, fields, query, sort);
	    return HALCollectionV1Builder.fromPartial(this.list(page, pageSize, Collections.emptyList(), "", Collections.emptyMap()), getRequestURI(), this.count("")).page(page)
	        	.maxPageSize(pageSize).compact(compact).build();
	}
	verifyNonV2ParametersNotUsedOnV2(page, compact);
	return getHALCollectionV2(pageStart, pageSize, fields, query, sort, MediaType.valueOf(HALCollectionV2.MEDIA_TYPE_HAL_JSON));
    }
    
    /**
     * Retrieve the paginated collection of resources.
     * 
     * Unless stated otherwise, these parameters can only be used with {@link HALCollectionV2}.
     * 
     * This is a separate method because otherwise Jackson will use the same ObjectMapper for both the HAL+JSON and JSON collection. 
     *
     * @param page      is the page number of the paginated collection of resources (for {@link HALCollectionV1} only)
     * @param pageStart is the offset at which the requested page starts.
     * @param pageSize  is the size of a single page in this paginated collection of
     *                  resources (for both {@link HALCollectionV1} and {@link HALCollectionV2})
     * @param compact   determines whether the resource in the collection only shows
     *                  its self-link (if true), or the entire resource (if false) (for {@link HALCollectionV1} only)
     * @param pageSize  is the requested size of each page.
     * @param fields    is a list that defines which fields should be retrieved.
     * @param query     is a FIQL query that defines how the resources should be
     *                  filtered.
     * @param sort      is a list of field names on which the resources should be
     *                  sorted.
     * @return the paginated collection of resources.
     */
    @GET
    @Produces(MEDIA_TYPE_QUALIFIED_COLLECTION_V2_JSON)
    @ApiOperation(value = "Get a list of resources", notes = "Get a list of resources")
    public HALCollection<T> getHALResourcesJson(
	    @Context
	    Request request,
	    @ApiParam(value = "The page to be shown", required = false)
	    @QueryParam(QUERY_PARAM_PAGE_START)
	    @DefaultValue(QUERY_PARAM_PAGE_START_DEFAULT)
	    int pageStart,
	    @ApiParam(value = "The amount of resources shown on each page", required = false)
            @QueryParam(QUERY_PARAM_PAGE_SIZE)
            @DefaultValue(HALCollectionV1Builder.DEFAULT_MAX_PAGESIZE_STRING)
            int pageSize,
	    @ApiParam(value = "The fields that should be included", required = false)
	    @QueryParam(QUERY_PARAM_FIELDS)
	    @DefaultValue(QUERY_PARAM_FIELDS_COLLECTION_DEFAULT)
	    List<String> fields,
	    @ApiParam(value = "The FIQL query according to which the resources should be filtered", required = false)
	    @QueryParam(QUERY_PARAM_QUERY)
	    @DefaultValue(QUERY_PARAM_QUERY_DEFAULT)
	    String query,
	    @ApiParam(value = "The fields on which the resources should be sorted", required = false)
	    @QueryParam(QUERY_PARAM_SORT)
	    @DefaultValue(QUERY_PARAM_SORT_DEFAULT)
	    List<String> sort) {
	return getHALCollectionV2(pageStart, pageSize, fields, query, sort, MediaType.valueOf(HALCollectionV2.MEDIA_TYPE_JSON));
    }

    private void verifyNonV2ParametersNotUsedOnV2(int page, boolean compact) {
	if(page != HALCollectionV1Builder.DEFAULT_PAGE_NUMBER) {
	throw new BadRequestException("The page parameter can not be used on version 2 of this collection resource");
	}
	if(compact != HALCollectionV1Builder.DEFAULT_COMPACT_VALUE) {
	throw new BadRequestException("The compact parameter can not be used on version 2 of this collection resource");
	}
    }

    private void verifyNonV1ParametersNotUsedOnV1(int pageStart, List<String> fields, String query, List<String> sort) {
	if(pageStart != Integer.valueOf(QUERY_PARAM_PAGE_START_DEFAULT)) {
	throw new BadRequestException("The pageStart parameter can not be used on version 1 of this collection resource");
	}
	if(!fields.equals(Collections.singletonList(QUERY_PARAM_FIELDS_COLLECTION_DEFAULT))) {
	throw new BadRequestException("The pageStart parameter can not be used on version 1 of this collection resource");
	}
	if(!query.equals(QUERY_PARAM_QUERY_DEFAULT)) {
	throw new BadRequestException("The pageStart parameter can not be used on version 1 of this collection resource");
	}
	if(!sort.equals(Collections.singletonList(QUERY_PARAM_SORT_DEFAULT))) {
	throw new BadRequestException("The pageStart parameter can not be used on version 1 of this collection resource");
	}
    }

    private HALCollectionV2<T> getHALCollectionV2(int pageStart, int pageSize, List<String> fields, String query, List<String> sort, MediaType mediaType) {
	List<T> filteredAndSortedResources = this.list(
        	pageStart,
        	pageSize,
        	separateDelimitedFields(fields),
        	query,
        	separateDelimitedSort(sort));
        return HALCollectionV2Builder.from(filteredAndSortedResources, getRequestURI())
        	.withNavigation(pageStart, pageSize)
        	.collectionSize(this.count(query))
        	.build(mediaType);
    }

    /**
     * Create a resource.
     *
     * @param resource is a resource that should be created.
     * @return a "201 Created" response for the resource that was created,
     *         containing its URI identifier in the Location header, if the resource
     *         was correctly created.
     */
    @POST
    @ApiOperation(value = "Create a new resource", notes = "Create a new resource which can already have a self-link containing a URI as identifier or one will be generated")
    @Produces
    public Response postHALResource(@ApiParam(value = "resource", required = true) @NotNull @Valid T resource) {
	UUID resourceId = ensureSelfLinkValid(resource, null);
	if (this.exists(resourceId)) {
	    throw new ClientErrorException(
		    "A resource with the same ID already exists. Try to update the resource with a PUT request.",
		    Response.Status.CONFLICT);
	}
	T updatedResource = this.create(resource, resourceId);
	return Response.created(URI.create(updatedResource.getSelf().getHref())).build();
    }

    /**
     * Retrieve a resource.
     *
     * @param id is the UUID part from the entire URI identifier of the resource.
     * @return the requested resource.
     */
    @Path("/{id}")
    @GET
    @ApiOperation(value = "Retrieve a single resource", notes = "Retrieve a single resource")
    @Consumes
    public T getHALResource(
	    @ApiParam(value = "The identifier for the resource", required = true) @PathParam("id") @NotNull UUID id) {
	return Optional.ofNullable(this.read(id)).orElseThrow(NotFoundException::new);
    }

    /**
     * Update a resource (or create it with the given identifier)
     *
     * The resource should contain a self-link. This self-link must match the
     * provided id. If a resource with that id does not exist yet, it will be
     * created with that id.
     *
     * @param id       is the UUID part from the entire URI identifier of the
     *                 resource.
     * @param resource is the updated resource.
     * @return a "200 OK" if the resource was updated, or "201 Created" if the
     *         resource was created.
     */
    @Path("/{id}")
    @PUT
    @ApiOperation(value = "Create or update a resource", notes = "Create a resource with a specified ID or update that resource. Returns a 201 HTTP status with the UUID of the resource in the Location header, if a new one was created. Otherwise it just returns 200 OK.")
    @Produces
    public Response putHALResource(
	    @ApiParam(value = "The UUID part of the identifier for the resource", required = true) @PathParam("id") @NotNull UUID id,
	    @ApiParam(value = "The resource to be updated", required = true) @NotNull @Valid T resource) {
	ensureSelfLinkValid(resource, id);
	if (this.exists(id)) {
	    this.update(resource, id);
	    return Response.ok().build();
	}
	this.create(resource, id);
	return Response.created(getAbsoluteWebResourceURI(id)).build();
    }

    /**
     * Delete a resource.
     *
     * @param id is the UUID part from the entire URI identifier of the resource.
     * @return a "204 No Content" if the resource was correctly deleted.
     */
    @Path("/{id}")
    @DELETE
    @ApiOperation(value = "Delete a single resource", notes = "Delete a single resource")
    @Consumes
    @Produces
    public Response deleteHALResource(
	    @ApiParam(value = "The UUID part of the identifier for the resource", required = true) @PathParam("id") @NotNull UUID id) {
	return Optional.ofNullable(this.delete(id)).map(resource -> Response.noContent().build())
		.orElseThrow(NotFoundException::new);
    }

    /**
     * Create the resource in the data store where it is stored.
     *
     * The resource should contain a self-link that contains the unique ID for this
     * resource.
     *
     * @param resource     is the resource that should be created, containing a
     *                     self-link with its unique ID
     * @param resourceUUID is the unique ID of the resource which should match the
     *                     UUID used in the self-link
     * @return the created resource as persisted
     */
    public abstract T create(T resource, UUID resourceUUID);

    /**
     * Retrieve the resource from the data store where it is stored.
     *
     * The identifier provided by the API is the URI of the resource. This does not
     * have to be the identifier used in the data store (UUID is more commonly used)
     * but each entity in the data store must be uniquely identifiable by the
     * information provided in the URI.
     *
     * @param resourceUUID is the identifier (from API perspective) for the resource
     * @return the resource that was requested or null if it doesn't exist
     */
    public abstract T read(UUID resourceUUID);

    /**
     * Update the resource in the data store where it is stored.
     *
     * The resource should contain a self-link in order to identify which resource
     * needs to be updated.
     *
     * @param resource     is the updated resource (which contains a self-link with
     *                     which to identify the resource)
     * @param resourceUUID is the identifier of the resource that should be updated
     * @return the updated resource as persisted
     */
    public abstract T update(T resource, UUID resourceUUID);

    /**
     * Remove a resource from the data store.
     *
     * @param resourceUUID is the identifier of the resource that should be removed
     * @return the removed resource, or null if it did not exist
     */
    public abstract T delete(UUID resourceUUID);

    /**
     * Retrieve the paged collection of resources that have been requested.
     *
     * For proper discoverability of the API, all links (href values in each HALLink
     * object) should contain absolute URI's and a self-link must be available in
     * each resource.
     *
     * @param pageStart is the offset at which the requested page starts.
     * @param pageSize is the requested size of each page.
     * @param fields is a list that defines which fields should be retrieved. 
     * @param query is a FIQL query that defines how the resources should be filtered.
     * @param sort is a list of field names on which the resources should be sorted.
     * @return the requested list of resources for the requested page.
     */
    public abstract List<T> list(int pageStart, int pageSize, List<String> fields, String query, Map<String, String> sort);

    /**
     * Retrieve how many resources are available.
     *
     * This provides a simple implementation for convenience but should be
     * overridden with an optimized implementation, if possible.
     *
     * @param query is a FIQL query that defines how the resources should be filtered.
     * @return the total amount of resources that are available
     */
    public abstract int count(String query);

    /**
     * Check if a resource, identified by its resourceURI, exists.
     *
     * This provides a simple implementation for convenience but should be
     * overridden with an optimized implementation, if possible.
     *
     * @param resourceUUID is the identifier of a resource.
     * @return true if the resource identified by resourceURI exists, false
     *         otherwise.
     */
    public boolean exists(UUID resourceUUID) {
	return Objects.nonNull(this.read(resourceUUID));
    }

    protected List<String> separateDelimitedFields(List<String> delimitedFields) {
        return delimitedFields.stream()
        	.filter(Objects::nonNull)
        	.flatMap(delimitedString -> Arrays.asList(delimitedString.split(QUERY_PARAM_FIELDS_DELIMITER)).stream())
        	.map(String::trim)
        	.collect(Collectors.toList());
    }
    
    protected Map<String, String> separateDelimitedSort(List<String> delimitedSort) {
        return delimitedSort.stream()
        	.filter(Objects::nonNull)
        	.filter(sortEntry -> !sortEntry.isBlank())
        	.flatMap(delimitedString -> Arrays.asList(delimitedString.split(QUERY_PARAM_SORT_DELIMITER)).stream())
        	.map(String::trim)
        	.collect(Collectors.toMap(
        		entry -> entry.split(QUERY_PARAM_SORT_ORDER_DELIMITER)[0].trim(),
        		entry -> entry.split(QUERY_PARAM_SORT_ORDER_DELIMITER)[1].trim()));
    }

    /**
     * Get the absolute URI for a different web resource with the given ID.
     *
     * The different web resource has to be hosted on the same application root as
     * this web resource. This will likely already be the case when you have access
     * to the Class object of that web resource
     *
     * @param webResource is the class of the @Path-annotated endpoint class for the
     *                    resource
     * @param id          is the UUID of the resource, which can be null if the base
     *                    URI is requested.
     * @return the absolute URI for the resource on the requested endpoint.
     */
    protected URI getAbsoluteWebResourceURI(Class<?> webResource, UUID id) {
	if (id == null) {
	    return uriInfo.getBaseUriBuilder().path(webResource).build();
	}
	return uriInfo.getBaseUriBuilder().path(webResource).path(id.toString()).build();
    }

    /**
     * Get the absolute URI for the web resource with the given ID.
     *
     * Example:
     * https://example.com/api/resource/00000000-0000-0000-0000-000000000000
     *
     * @param id is the ID of the resource provided on the endpoint.
     * @return the absolute URI for the resource on the endpoint.
     */
    protected URI getAbsoluteWebResourceURI(UUID id) {
	return getAbsoluteWebResourceURI(this.getClass(), id);
    }

    /**
     * Get the absolute base URI for this web resource.
     *
     * Example: https://example.com/api/resource/
     *
     * @return the absolute base URI for this resource
     */
    protected URI getAbsoluteWebResourceURI() {
	return getAbsoluteWebResourceURI(null);
    }

    protected URI getRequestURI() {
	return uriInfo.getRequestUri();
    }

    /**
     * Create a {@link HALLink} that refers to the provided resource URI with the
     * given profile.
     *
     * Note that the media type is always set to HAL+JSON.
     *
     * @param resourceURI     is the URI of the resource to which this
     *                        {@link HALLink} refers
     * @param resourceProfile is the URI of the profile describing the resource to
     *                        which this {@link HALLink} refers
     * @return a {@link HALLink} that refers to the provided URI with the given
     *         profile
     */
    protected HALLink createLink(URI resourceURI, URI resourceProfile) {
	return new HALLink.Builder(resourceURI).type(AdditionalMediaTypes.APPLICATION_HAL_JSON).profile(resourceProfile)
		.build();
    }

    /**
     * Checks if the self-link is present and valid.
     *
     * A new self-link can be generated with a random or provided UUID, if one does
     * not yet exist. If a UUID is provided and a self-link exists, the ID in the
     * self-link must match the provided UUID.
     *
     * @param resource   is the resource to check.
     * @param providedID is a resource ID that should be used in the self-link, if
     *                   one does not yet exist.
     * @return the resource UUID that matches the ID used in the self-link.
     */
    private UUID ensureSelfLinkValid(T resource, UUID providedID) {
	if (resource.getSelf() == null) {
	    if (providedID == null) {
		providedID = UUID.randomUUID();
	    }
	    resource.setSelf(createLink(getAbsoluteWebResourceURI(providedID), resource.getProfile()));
	    return providedID;
	} else {
	    URI selfUri = URI.create(resource.getSelf().getHref());
	    URI relativizedResourceUri = getAbsoluteWebResourceURI().relativize(selfUri);
	    if (relativizedResourceUri.equals(selfUri)) {
		throw new BadRequestException(ERROR_SELF_LINK_URI_DOES_NOT_MATCH_API_BASE_URI);
	    }
	    UUID resourceIdFromSelf = UUID.fromString(relativizedResourceUri.getPath());
	    if (!Objects.equals(providedID, resourceIdFromSelf)) {
		throw new BadRequestException(ERROR_SELF_LINK_ID_DOES_NOT_MATCH_PROVIDED_ID);
	    }
	    return resourceIdFromSelf;
	}
    }
}
