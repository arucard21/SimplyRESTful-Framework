package simplyrestful.api.framework.core.api.webresource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import simplyrestful.api.framework.core.MediaTypeUtils;
import simplyrestful.api.framework.core.QueryParamUtils;
import simplyrestful.api.framework.core.api.crud.DefaultStream;
import simplyrestful.api.framework.resources.HALResource;

public interface DefaultCollectionGetEventStream<T extends HALResource> extends WebResourceBase<T>, DefaultStream<T> {
    /**
     * Retrieve the collection of resources as a stream of events (as server-sent events).
     * <p>
     * This uses
     * <a href="https://html.spec.whatwg.org/multipage/server-sent-events.html#server-sent-events}">server-sent events (SSE)</a>
     * to send each resource in the collection as an Event to the API consumer.
     * </p>
     * @param fields    is a list that defines which fields should be retrieved. This is only included for convenience
     * 			as it is already handled by the framework. It can be used to filter on these fields in the backend
     * 			as well, e.g. to improve performance.
     * @param query     is a FIQL query that defines how the resources should be filtered.
     * @param sort      is a list of field names on which the resources should be sorted. This is only included for
     * 			convenience as it is already handled by the framework.
     * @return the paginated collection of resources.
     */
    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS+";qs=0.1")
    @Operation(description = "Get a stream of resources")
    default void streamHALResources(
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
	    List<String> sort,
	    @Context
	    SseEventSink eventSink,
	    @Context
	    Sse sse) throws InterruptedException{
	try (SseEventSink sink = eventSink) {
	    try (Stream<T> stream = stream(
		    QueryParamUtils.stripHALStructure(fields),
		    QueryParamUtils.stripHALStructure(query),
		    QueryParamUtils.parseSort(sort))) {
		stream.forEach(resourceItem -> {
		    sink.send(sse.newEventBuilder().data(resourceItem).mediaType(new MediaType(
			    MediaTypeUtils.APPLICATION_HAL_JSON_TYPE.getType(),
			    MediaTypeUtils.APPLICATION_HAL_JSON_TYPE.getSubtype(), Collections.singletonMap(
				    MEDIA_TYPE_HAL_PARAMETER_PROFILE_NAME, resourceItem.getProfile().toString())))
			    .build());
		});
	    }
	}
    }
}
