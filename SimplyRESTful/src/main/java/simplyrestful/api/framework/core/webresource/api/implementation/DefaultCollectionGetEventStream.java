package simplyrestful.api.framework.core.webresource.api.implementation;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import simplyrestful.api.framework.core.MediaTypeUtils;
import simplyrestful.api.framework.core.QueryParamUtils;
import simplyrestful.api.framework.core.api.crud.DefaultStream;
import simplyrestful.api.framework.resources.HALResource;
import simplyrestful.api.framework.webresource.api.CollectionGetEventStream;

public interface DefaultCollectionGetEventStream<T extends HALResource> extends CollectionGetEventStream<T>, DefaultStream<T> {
    @Operation(description = "Get a stream of resources")
    default void streamHALResources(
	    @Parameter(description = "The fields that should be retrieved", required = false)
	    List<String> fields,
	    @Parameter(description = "The FIQL query according to which the resources should be filtered", required = false)
	    String query,
	    @Parameter(description = "The fields on which the resources should be sorted", required = false)
	    List<String> sort,
	    SseEventSink eventSink,
	    Sse sse){
        try (SseEventSink sink = eventSink) {
            try (Stream<T> stream = stream(
        	    QueryParamUtils.stripHALStructure(fields),
                    QueryParamUtils.stripHALStructure(query),
                    QueryParamUtils.parseSort(sort))) {
                stream.forEach(resourceItem -> {
                    sink.send(sse.newEventBuilder()
                            .data(resourceItem)
                            .mediaType(new MediaType(
                                    MediaTypeUtils.APPLICATION_HAL_JSON_TYPE.getType(),
                                    MediaTypeUtils.APPLICATION_HAL_JSON_TYPE.getSubtype(),
                                    Collections.singletonMap(
                                            MediaTypeUtils.APPLICATION_HAL_JSON_PARAMETER_PROFILE,
                                            resourceItem.getProfile().toString())))
                            .build());
                });
            }
        }
    }
}
