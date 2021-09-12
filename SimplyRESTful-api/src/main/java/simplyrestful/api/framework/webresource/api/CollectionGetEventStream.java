package simplyrestful.api.framework.webresource.api;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;

import simplyrestful.api.framework.resources.HALResource;

public interface CollectionGetEventStream<T extends HALResource> {
    /**
     * Retrieve the collection of resources as a stream of events (as server-sent events).
     * <p>
     * This uses
     * <a href="https://html.spec.whatwg.org/multipage/server-sent-events.html#server-sent-events}">server-sent events (SSE)</a>
     * to send each resource in the collection as an Event to the API consumer.
     * </p>
     * @param requestContext is a JAX-RS context object.
     * @param fields    is a list that defines which fields should be retrieved. This is only included for convenience
     * 			as it is already handled by the framework. It can be used to filter on these fields in the backend
     * 			as well, e.g. to improve performance.
     * @param query     is a FIQL query that defines how the resources should be filtered.
     * @param sort      is a list of field names on which the resources should be sorted. This is only included for
     * 			convenience as it is already handled by the framework.
     */
    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS+";qs=0.1")
    void streamHALResources(
    		@Context
    		ContainerRequestContext requestContext,
		    @QueryParam(CollectionGet.QUERY_PARAM_FIELDS)
		    @DefaultValue(CollectionGet.QUERY_PARAM_FIELDS_DEFAULT)
		    List<String> fields,
		    @QueryParam(CollectionGet.QUERY_PARAM_QUERY)
		    @DefaultValue(CollectionGet.QUERY_PARAM_QUERY_DEFAULT)
		    String query,
		    @QueryParam(CollectionGet.QUERY_PARAM_SORT)
		    @DefaultValue(CollectionGet.QUERY_PARAM_SORT_DEFAULT)
		    List<String> sort,
		    @Context
		    SseEventSink eventSink,
		    @Context
		    Sse sse);
}
