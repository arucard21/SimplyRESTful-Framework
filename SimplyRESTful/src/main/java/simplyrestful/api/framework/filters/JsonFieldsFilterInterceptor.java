package simplyrestful.api.framework.filters;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import simplyrestful.api.framework.outputstream.json.JsonFieldsFilterOutputStream;

@Provider
public class JsonFieldsFilterInterceptor implements WriterInterceptor {
	public static final String MEDIA_TYPE_STRUCTURED_SYNTAX_SUFFIX_JSON = "+json";
    public static final String FIELDS_OVERRIDE_REQUEST_CONTEXT_PROPERTY = "simplyrestful.fields.json.override";
    public static final String QUERY_PARAM_FIELDS = "fields";
    public static final String FIELDS_PARAMS_SEPARATOR = ",";
    public static final String FIELDS_VALUE_ALL = "all";

    @Context
    UriInfo uriInfo;

	@Override
	public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
		boolean isEventStream = context.getMediaType().isCompatible(MediaType.SERVER_SENT_EVENTS_TYPE);
		if (isJson(context.getMediaType()) || isEventStream) {
			String fieldsOverride = getFieldsOverride(context);
			List<String> fieldsQueryParameters = fieldsOverride == null ? getFieldsQueryParameters() : List.of(fieldsOverride);
			List<String> fields = fieldsQueryParameters.stream()
					.flatMap(field -> Stream.of(field.split(FIELDS_PARAMS_SEPARATOR)))
					.map(String::trim)
					.toList();
			if (!fields.isEmpty() && !fields.contains(FIELDS_VALUE_ALL)) {
				context.setOutputStream(new JsonFieldsFilterOutputStream(context.getOutputStream(), fields));
			}
        }
		context.proceed();
	}

	private List<String> getFieldsQueryParameters() {
		List<String> fieldsParameters = uriInfo.getQueryParameters().get(QUERY_PARAM_FIELDS);
		if(fieldsParameters != null) {
			return fieldsParameters;
		}
		return List.of();
	}

	private String getFieldsOverride(WriterInterceptorContext context) {
		Object fieldsOverride = context.getProperty(FIELDS_OVERRIDE_REQUEST_CONTEXT_PROPERTY);
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
    			contentType.getSubtype().endsWith(MEDIA_TYPE_STRUCTURED_SYNTAX_SUFFIX_JSON))) {
            return true;
        }
        return false;
    }
}
