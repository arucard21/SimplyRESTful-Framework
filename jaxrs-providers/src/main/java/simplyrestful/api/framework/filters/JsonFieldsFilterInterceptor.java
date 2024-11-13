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
import simplyrestful.api.framework.utils.MediaTypeUtils;
import simplyrestful.api.framework.utils.QueryParamUtils;

/**
 * Filters the keys of any JSON-based response body based on the fields provided in a "fields" query parameter.
 *
 * Multiple fields can be provided, separated by comma's. The "fields" query parameter can also be provided multiple
 * times with different values.
 *
 * Nested fields can be specified using a dot as separated (e.g. "topField.nestedField").
 */
@Provider
public class JsonFieldsFilterInterceptor implements WriterInterceptor {
    @Context
    UriInfo uriInfo;

	@Override
	public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
		boolean isEventStream = context.getMediaType().isCompatible(MediaType.SERVER_SENT_EVENTS_TYPE);
		if (isJson(context.getMediaType()) || isEventStream) {
			String fieldsOverride = getFieldsOverride(context);
			List<String> fieldsQueryParameters = fieldsOverride == null ? getFieldsQueryParameters() : List.of(fieldsOverride);
			List<String> fields = QueryParamUtils.flattenQueryParameters(fieldsQueryParameters);
			if (!fields.isEmpty() && !fields.contains(QueryParamUtils.FIELDS_VALUE_ALL)) {
				context.setOutputStream(new JsonFieldsFilterOutputStream(context.getOutputStream(), fields));
			}
        }
		context.proceed();
	}

	private List<String> getFieldsQueryParameters() {
		List<String> fieldsParameters = uriInfo.getQueryParameters().get(QueryParamUtils.QUERY_PARAM_FIELDS);
		if(fieldsParameters != null) {
			return fieldsParameters;
		}
		return List.of();
	}

	private String getFieldsOverride(WriterInterceptorContext context) {
		Object fieldsOverride = context.getProperty(QueryParamUtils.FIELDS_OVERRIDE_REQUEST_CONTEXT_PROPERTY);
		if (fieldsOverride == null) {
			return null;
		}
		return String.valueOf(fieldsOverride);
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
