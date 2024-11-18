package simplyrestful.api.framework.filters;

import java.io.IOException;
import java.util.List;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import simplyrestful.api.framework.outputstream.json.JsonFieldsFilterOutputStream;
import simplyrestful.api.framework.resources.ApiCollection;
import simplyrestful.api.framework.utils.MediaTypeUtils;
import simplyrestful.api.framework.utils.QueryParamUtils;

/**
 * Filters the keys of any JSON-based response body based on the fields provided in a "fields" query parameter.
 *
 * Multiple fields can be provided, separated by comma's. The "fields" query parameter can also be provided multiple
 * times with different values.
 *
 * Nested fields can be specified using a dot as separated (e.g. "topField.nestedField").
 *
 * If the API returns a body containing a "application/x.simplyrestful-collection-v1+json" media type, the filter will
 * use a default "fields" value of "self,first,last,prev,next,total,item.self", only showing the self link of each
 * resource in the collection.
 */
@Provider
public class JsonFieldsFilterInterceptor implements WriterInterceptor {
    @Context
    UriInfo uriInfo;

	@Override
	public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
		boolean isEventStream = context.getMediaType().isCompatible(MediaType.SERVER_SENT_EVENTS_TYPE);
		if (!isJson(context.getMediaType()) && !isEventStream) {
			context.proceed();
			return;
		}
		List<String> fieldsQueryParameters = uriInfo.getQueryParameters().get(QueryParamUtils.QUERY_PARAM_FIELDS);
		if(fieldsQueryParameters == null) {
			if(isApiCollection(context.getMediaType())) {
				fieldsQueryParameters = List.of(ApiCollection.FIELDS_VALUE_DEFAULT);
			}
			else {
				context.proceed();
				return;
			}
		}
		List<String> fields = QueryParamUtils.flattenQueryParameters(fieldsQueryParameters);
		if (!fields.isEmpty() && !fields.contains(QueryParamUtils.FIELDS_VALUE_ALL)) {
			context.setOutputStream(new JsonFieldsFilterOutputStream(context.getOutputStream(), fields));
		}
		context.proceed();
	}

	private boolean isApiCollection(MediaType mediaType) {
		return MediaType.valueOf(ApiCollection.MEDIA_TYPE_JSON).equals(mediaType);
	}

	/**
	 * Verify that the provided media type is compatible with "application/json" as well as the "+json" type structure suffix.
	 * @param contentType
	 * @return
	 */
    private boolean isJson(MediaType contentType) {
    	if (contentType != null && (
    			contentType.isCompatible(MediaType.APPLICATION_JSON_TYPE) ||
    			contentType.getSubtype().endsWith(MediaTypeUtils.MEDIA_TYPE_STRUCTURED_SYNTAX_SUFFIX_JSON))) {
            return true;
        }
        return false;
    }
}
