package simplyrestful.api.framework.core;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;

import io.openapitools.jackson.dataformat.hal.HALLink;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import simplyrestful.api.framework.core.api.DefaultCount;
import simplyrestful.api.framework.core.api.DefaultCreate;
import simplyrestful.api.framework.core.api.DefaultDelete;
import simplyrestful.api.framework.core.api.DefaultExists;
import simplyrestful.api.framework.core.api.DefaultList;
import simplyrestful.api.framework.core.api.DefaultRead;
import simplyrestful.api.framework.core.api.DefaultStream;
import simplyrestful.api.framework.core.api.DefaultUpdate;
import simplyrestful.api.framework.core.hal.HALCollectionV1Builder;
import simplyrestful.api.framework.core.hal.HALCollectionV2Builder;
import simplyrestful.api.framework.resources.HALCollection;
import simplyrestful.api.framework.resources.HALCollectionV1;
import simplyrestful.api.framework.resources.HALCollectionV2;
import simplyrestful.api.framework.resources.HALResource;

@SuppressWarnings("deprecation")
public abstract class DefaultWebResource<T extends HALResource> implements
	DefaultCreate<T>,
	DefaultRead<T>,
	DefaultUpdate<T>,
	DefaultDelete<T>,
	DefaultList<T>,
	DefaultStream<T>,
	DefaultCount,
	DefaultExists {
    private static final int MEDIA_TYPE_SPECIFICITY_WILDCARD_TYPE = 0; // Example: */*
    private static final int MEDIA_TYPE_SPECIFICITY_WILDCARD_SUBTYPE = 1; // Example: application/*
    private static final int MEDIA_TYPE_SPECIFICITY_CONCRETE_TYPE_WITHOUT_PARAMETERS = 2; // Example: application/json
    private static final int MEDIA_TYPE_SPECIFICITY_CONCRETE_TYPE_WITH_PARAMETERS = 3; // Example: application/hal+json;profile="https://arucard21.github.io/SimplyRESTful-Framework/HALCollection/v2"
    private static final String MEDIA_TYPE_PARAMETER_QUALITY_SERVER = "qs";
    private static final String MEDIA_TYPE_PARAMETER_QUALITY_CLIENT = "q";
    private static final String QUERY_PARAM_SORT_ORDER_ASCENDING = "asc";
    private static final String HAL_EMBEDDED_OBJECT_NAME = "_embedded";
    private static final String HAL_LINKS_OBJECT_NAME = "_links";
    private static final String ERROR_SELF_LINK_ID_DOES_NOT_MATCH_PROVIDED_ID = "The provided resource contains an self-link that does not match the ID used in the request";
    private static final String ERROR_SELF_LINK_URI_DOES_NOT_MATCH_API_BASE_URI = "The identifier of the resource does not correspond to the base URI of this Web Resource";

    public static final String MEDIA_TYPE_HAL_PARAMETER_PROFILE_NAME = "profile";
    public static final String MEDIA_TYPE_COLLECTION_V1_HAL_JSON_QUALIFIED = HALCollectionV1.MEDIA_TYPE_HAL_JSON+";qs=0.9";
    public static final String MEDIA_TYPE_COLLECTION_V2_HAL_JSON_QUALIFIED = HALCollectionV2.MEDIA_TYPE_HAL_JSON+";qs=0.7";
    public static final String MEDIA_TYPE_COLLECTION_V2_JSON_QUALIFIED = HALCollectionV2.MEDIA_TYPE_JSON+";qs=0.8";

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
    public static final String QUERY_PARAM_FIELDS_MINIMUM = "_links.self,_links.first,_links.last,_links.prev,_links.next,total,_embedded.item._links.self";
    public static final String QUERY_PARAM_FIELDS_ALL = "all";
    public static final String QUERY_PARAM_QUERY_DEFAULT = "";
    public static final String QUERY_PARAM_SORT_DEFAULT = "";

    @Context
    protected UriInfo uriInfo;
    @Context
    protected Request request;
    @Context
    protected HttpHeaders httpHeaders;

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
    public HALCollection<T> getHALResources(
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
	    @DefaultValue(QUERY_PARAM_FIELDS_MINIMUM)
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
	MediaType selected = this.selectMediaType(mediaTypes);
	if(selected.equals(MediaType.valueOf(HALCollectionV1.MEDIA_TYPE_HAL_JSON))) {
	    int calculatedPageStart = (page -1) * pageSize;
	    if(compact) {
		fields = Collections.singletonList(QUERY_PARAM_FIELDS_MINIMUM);
	    }
	    return HALCollectionV1Builder.fromPartial(
		    this.list(
			    calculatedPageStart,
			    pageSize,
			    getFieldsQueryParameter(fields),
			    removeHALStructure(query),
			    getSortQueryParameter(sort)),
		    getRequestURI(),
		    this.count(removeHALStructure(query)))
		    .page(page)
		    .maxPageSize(pageSize)
		    .compact(compact)
		    .build();
	}
	return getHALCollectionV2(
		pageStart,
		pageSize,
		getFieldsQueryParameter(fields),
		removeHALStructure(query),
		getSortQueryParameter(sort),
		selected);
    }

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS+";qs=0.1")
    @Operation(description = "Get a stream of resources")
    public void streamHALResources(
	    @Parameter(description = "The fields that should be retrieved", required = false)
	    @QueryParam(QUERY_PARAM_FIELDS)
	    @DefaultValue(QUERY_PARAM_FIELDS_MINIMUM)
	    List<String> fields,
	    @Parameter(description = "The FIQL query according to which the resources should be filtered", required = false)
	    @QueryParam(QUERY_PARAM_QUERY)
	    @DefaultValue(QUERY_PARAM_QUERY_DEFAULT)
	    String query,
	    @Parameter(description = "The fields on which the resources should be sorted", required = false)
	    @QueryParam(QUERY_PARAM_SORT)
	    @DefaultValue(QUERY_PARAM_SORT_DEFAULT)
	    List<String> sort,
	    @Context
	    SseEventSink eventSink,
	    @Context
	    Sse sse) throws InterruptedException{
	try (SseEventSink sink = eventSink) {
	    try (Stream<T> stream = stream(getFieldsQueryParameter(fields), removeHALStructure(query),
		    getSortQueryParameter(sort))) {
		stream.forEach(resourceItem -> {
		    sink.send(sse.newEventBuilder().data(resourceItem).mediaType(new MediaType(
			    AdditionalMediaTypes.APPLICATION_HAL_JSON_TYPE.getType(),
			    AdditionalMediaTypes.APPLICATION_HAL_JSON_TYPE.getSubtype(), Collections.singletonMap(
				    MEDIA_TYPE_HAL_PARAMETER_PROFILE_NAME, resourceItem.getProfile().toString())))
			    .build());
		});
	    }
	}
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
    @Operation(description = "Create a new resource which can already have a self-link containing a URI as identifier or one will be generated")
    @Produces
    public Response postHALResource(@Parameter(description = "resource", required = true) @NotNull @Valid T resource) {
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
    @Operation(description = "Retrieve a single resource")
    @Consumes
    public T getHALResource(
	    @Parameter(description = "The identifier for the resource", required = true) @PathParam("id") @NotNull UUID id) {
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
    @Operation(description = "Create a resource with a specified ID or update that resource. Returns a 201 HTTP status with the UUID of the resource in the Location header, if a new one was created. Otherwise it just returns 200 OK.")
    @Produces
    public Response putHALResource(
	    @Parameter(description = "The UUID part of the identifier for the resource", required = true) @PathParam("id") @NotNull UUID id,
	    @Parameter(description = "The resource to be updated", required = true) @NotNull @Valid T resource) {
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
    @Operation(description = "Delete a single resource")
    @Consumes
    @Produces
    public Response deleteHALResource(
	    @Parameter(description = "The UUID part of the identifier for the resource", required = true) @PathParam("id") @NotNull UUID id) {
	return Optional.ofNullable(this.delete(id)).map(resource -> Response.noContent().build())
		.orElseThrow(NotFoundException::new);
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

    private String removeHALStructure(String query) {
        return query.replaceAll(HAL_LINKS_OBJECT_NAME+".", "").replaceAll(HAL_EMBEDDED_OBJECT_NAME+".", "");
    }

    private List<String> getFieldsQueryParameter(List<String> fields) {
        return fields.stream()
        	.map(String::trim)
        	.map(this::removeHALStructure)
        	.collect(Collectors.toList());
    }

    private Map<String, Boolean> getSortQueryParameter(List<String> sort) {
        return sort.stream()
        	.map(String::trim)
        	.filter(sortSingleEntry -> sortSingleEntry.contains(DefaultWebResource.QUERY_PARAM_SORT_ORDER_DELIMITER))
        	.map(this::removeHALStructure)
        	.collect(Collectors.toMap(
        		sortWithOrderDelimeter -> sortWithOrderDelimeter.split(
        			DefaultWebResource.QUERY_PARAM_SORT_ORDER_DELIMITER)[0],
        		sortWithOrderDelimeter -> Boolean.valueOf(
        			sortWithOrderDelimeter.split(
        				DefaultWebResource.QUERY_PARAM_SORT_ORDER_DELIMITER)[1].equalsIgnoreCase(QUERY_PARAM_SORT_ORDER_ASCENDING))));
    }

    private HALCollectionV2<T> getHALCollectionV2(int pageStart, int pageSize, List<String> fields, String query, Map<String, Boolean> sort, MediaType mediaType) {
        List<T> filteredResources = this.list(
        	pageStart,
        	pageSize,
        	fields,
        	query,
        	sort);
        return HALCollectionV2Builder.from(filteredResources, getRequestURI())
        	.withNavigation(pageStart, pageSize)
        	.collectionSize(this.count(query))
        	.build(mediaType);
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

    /**
     *
     * Select the most suitable media type according to JAX-RS specification.
     *
     * This custom method is provided since some JAX-RS frameworks do not consider
     * media type parameters as a more specific media type (at least, when using
     * the {@link Request}.selectVariant() method). This method provides a correct
     * implementation for this, limited to only the media type.
     *
     * This framework requires the media type parameters to be considered since that
     * is where the profile is found. This profile is what uniquely typifies each
     * API resource (representing a single version of a specific API resource).
     *
     * This method does not change the Content-Type HTTP header that the JAX-RS
     * framework returns. The JAX-RS framework must already set this correctly,
     * based on the @{@link Produces} or @(@link Consumes} annotations. Or it must be
     * adjusted in some other way, e.g. using a {@link ContainerResponseFilter}.
     *
     * @param mediaTypes is the list of media types that the server can produce.
     * @return the most suitable media type, considering the client's preferences and
     * the server's capabilities.
     */
    protected MediaType selectMediaType(List<MediaType> producibleMediaTypes) {
        List<MediaType> acceptableMediaTypes = httpHeaders.getAcceptableMediaTypes();
        List<MediaType> selectedMediaTypes = new ArrayList<>();
        for(MediaType acceptableMediaType : acceptableMediaTypes) {
            for(MediaType producibleMediaType : producibleMediaTypes) {
        	if(acceptableMediaType.isCompatible(producibleMediaType)) {
        	    selectedMediaTypes.add(getMostSpecificMediaType(acceptableMediaType, producibleMediaType));
        	}
            }
        }
        if(selectedMediaTypes.isEmpty()) {
            throw new NotAcceptableException();
        }
        selectedMediaTypes.sort(
        	Comparator.comparingInt(this::determineMediaTypeSpecificity)
        	.thenComparingDouble(this::getQParameter).reversed()
        	.thenComparingDouble(this::getQSParameter).reversed());
        for(MediaType selectedMediaType : selectedMediaTypes) {
            if(isConcreteMediaType(selectedMediaType)) {
        	return withoutQualityParameters(selectedMediaType);
            }
            if((selectedMediaType.getType().equals(MediaType.MEDIA_TYPE_WILDCARD) ||
        	    selectedMediaType.getType().equals(MediaType.APPLICATION_OCTET_STREAM_TYPE.getType())) &&
        	    selectedMediaType.getSubtype().equals(MediaType.MEDIA_TYPE_WILDCARD)) {
        	return MediaType.APPLICATION_OCTET_STREAM_TYPE;
            }
        }
        throw new NotAcceptableException();
    }

    private MediaType addQualityParameters(MediaType mediaType, double q, double qs) {
	return addQSParameter(addQParameter(mediaType, q), qs);
    }

    private MediaType addQSParameter(MediaType mediaType, double qs) {
        Map<String, String> newParameters = new HashMap<>(mediaType.getParameters());
        newParameters.put(MEDIA_TYPE_PARAMETER_QUALITY_SERVER, Double.toString(qs));
        return new MediaType(mediaType.getType(), mediaType.getSubtype(), newParameters);
    }

    private MediaType addQParameter(MediaType mediaType, double q) {
	Map<String, String> newParameters = new HashMap<>(mediaType.getParameters());
        newParameters.put(MEDIA_TYPE_PARAMETER_QUALITY_CLIENT, Double.toString(q));
        return new MediaType(mediaType.getType(), mediaType.getSubtype(), newParameters);
    }

    private Double getQSParameter(MediaType mediaType) {
        String qsParameter = mediaType.getParameters().get(MEDIA_TYPE_PARAMETER_QUALITY_SERVER);
        return qsParameter == null ? 1.0 : Double.valueOf(qsParameter);
    }

    private Double getQParameter(MediaType mediaType) {
        String qParameter = mediaType.getParameters().get(MEDIA_TYPE_PARAMETER_QUALITY_CLIENT);
        return qParameter == null ? 1.0 : Double.valueOf(qParameter);
    }

    private MediaType withoutQualityParameters(MediaType selectedMediaType) {
        Map<String, String> parametersWithoutQAndQS = selectedMediaType.getParameters().entrySet().stream()
        	.filter(entry -> !entry.getKey().equals(MEDIA_TYPE_PARAMETER_QUALITY_CLIENT))
        	.filter(entry -> !entry.getKey().equals(MEDIA_TYPE_PARAMETER_QUALITY_SERVER))
        	.collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        return new MediaType(selectedMediaType.getType(), selectedMediaType.getSubtype(), parametersWithoutQAndQS);
    }

    private boolean isConcreteMediaType(MediaType selectedMediaType) {
        if(!selectedMediaType.getType().equals(MediaType.MEDIA_TYPE_WILDCARD) &&
        	!selectedMediaType.getSubtype().equals(MediaType.MEDIA_TYPE_WILDCARD)) {
            return true;
        }
        return false;
    }

    private MediaType getMostSpecificMediaType(MediaType acceptableMediaType, MediaType producibleMediaType) {
        int clientSpecificity = determineMediaTypeSpecificity(acceptableMediaType);
        int serverSpecificity = determineMediaTypeSpecificity(producibleMediaType);
        if(clientSpecificity == MEDIA_TYPE_SPECIFICITY_CONCRETE_TYPE_WITH_PARAMETERS && clientSpecificity == serverSpecificity) {
            Map<String, String> serverParameters = producibleMediaType.getParameters();
            for(Entry<String, String> clientParameter : acceptableMediaType.getParameters().entrySet()) {
        	String serverParameter = serverParameters.get(clientParameter.getKey());
        	if(serverParameter == null || !clientParameter.getValue().equals(serverParameter)) {
        	    /*
        	     * The client has this parameter in the media type while the server does not or
        	     * the client has a different value for this parameter than the server.
        	     *
        	     * This makes the client media type more specific than the server media type.
        	     */
        	    clientSpecificity++;
        	}
            }
        }
        return clientSpecificity > serverSpecificity ?
        	addQualityParameters(
        		acceptableMediaType,
        		getQParameter(acceptableMediaType),
        		getQSParameter(producibleMediaType)):
		addQualityParameters(
			producibleMediaType,
			getQParameter(acceptableMediaType),
			getQSParameter(producibleMediaType));
    }

    /**
     * Determines how specific the media type is.
     * A higher number denotes a more specific media type.
     *
     * @param mediaType is the media type for which to determine specificity
     * @return the specificity of the media type (higher is more specific)
     */
    private int determineMediaTypeSpecificity(MediaType mediaType) {
        String type = mediaType.getType();
        String subType = mediaType.getSubtype();
        Map<String, String> parameters = mediaType.getParameters();
        if(type.equals(MediaType.MEDIA_TYPE_WILDCARD)) {
            return MEDIA_TYPE_SPECIFICITY_WILDCARD_TYPE;
        }
        if(subType.equals(MediaType.MEDIA_TYPE_WILDCARD)) {
            return MEDIA_TYPE_SPECIFICITY_WILDCARD_SUBTYPE;
        }
        if(parameters.isEmpty()) {
            return MEDIA_TYPE_SPECIFICITY_CONCRETE_TYPE_WITHOUT_PARAMETERS;
        }
        return MEDIA_TYPE_SPECIFICITY_CONCRETE_TYPE_WITH_PARAMETERS;
    }
}
