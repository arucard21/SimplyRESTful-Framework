package simplyrestful.api.framework.webresource.api.implementation;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import simplyrestful.api.framework.api.crud.DefaultStream;
import simplyrestful.api.framework.resources.ApiResource;
import simplyrestful.api.framework.utils.QueryParamUtils;

public interface DefaultCollectionGetEventStream<T extends ApiResource> extends DefaultStream<T> {
	public static final String SSE_END_OF_COLLECTION_TOKEN = "end-of-collection";

	/**
     * Retrieve the collection of resources as a stream of events (as server-sent events).
     * <p>
     * This uses
     * <a href="https://html.spec.whatwg.org/multipage/server-sent-events.html#server-sent-events">server-sent events (SSE)</a>
     * to send each resource in the collection as an Event to the API consumer.
     * </p>
     * @param requestContext is a JAX-RS context object.
     * @param fields    is a list that defines which fields should be retrieved. This is only included for convenience
     * 			as it is already handled by the framework. It can be used to filter on these fields in the backend
     * 			as well, e.g. to improve performance.
     * @param query     is a FIQL query that defines how the resources should be filtered.
     * @param sort      is a list of field names on which the resources should be sorted. This is only included for
     * 			convenience as it is already handled by the framework.
	 * @throws IOException if an I/O error occurs while closing the outbound SSE stream (SseEventSink).
     */
    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS+";qs=0.1")
    @Operation(description = "Retrieve a filtered, sorted collection of resources as an event stream.")
    @ApiResponse(description = "An event stream containing your API resources.")
    default void streamAPIResources(
    		@QueryParam(DefaultCollectionGet.QUERY_PARAM_FIELDS)
		    @DefaultValue(QueryParamUtils.FIELDS_VALUE_ALL)
		    @Parameter(description = "The fields that should be retrieved", required = false)
		    List<String> fields,
		    @QueryParam(DefaultCollectionGet.QUERY_PARAM_QUERY)
		    @DefaultValue(DefaultCollectionGet.QUERY_PARAM_QUERY_DEFAULT)
		    @Parameter(description = "The FIQL query according to which the resources should be filtered", required = false)
		    String query,
		    @QueryParam(DefaultCollectionGet.QUERY_PARAM_SORT)
		    @DefaultValue(DefaultCollectionGet.QUERY_PARAM_SORT_DEFAULT)
		    @Parameter(description = "The fields on which the resources should be sorted", required = false)
		    List<String> sort,
		    @Context
		    SseEventSink eventSink,
		    @Context
		    Sse sse) throws IOException{
        try (SseEventSink sink = eventSink; Stream<T> stream = stream(fields,query,QueryParamUtils.parseSort(sort))) {
        	stream.forEach(resourceItem -> {
				sink.send(sse.newEventBuilder()
                        .data(resourceItem)
                        .mediaType(resourceItem.customJsonMediaType())
                        .build());
            });
        	sink.send(sse.newEventBuilder()
        			.comment(SSE_END_OF_COLLECTION_TOKEN)
        			.data(SSE_END_OF_COLLECTION_TOKEN)
        			.mediaType(MediaType.TEXT_PLAIN_TYPE)
        			.build());
        }
    }
}
