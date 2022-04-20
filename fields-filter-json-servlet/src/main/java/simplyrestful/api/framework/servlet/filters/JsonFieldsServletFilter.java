package simplyrestful.api.framework.servlet.filters;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Named;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import simplyrestful.api.framework.filters.JsonFieldsFilter;

@Named
@WebFilter("*")
public class JsonFieldsServletFilter extends HttpFilter {
	private static final long serialVersionUID = 6825636135376615562L;
	public static final String MEDIA_TYPE_JSON = "application/json";
	public static final String MEDIA_TYPE_STRUCTURE_SUFFIX_JSON = "+json";
    public static final String FIELDS_OVERRIDE_REQUEST_CONTEXT_PROPERTY = "simplyrestful.fields.json.override";
    public static final String QUERY_PARAM_FIELDS = "fields";
    public static final String FIELDS_PARAMS_SEPARATOR = ",";
    public static final String FIELDS_VALUE_ALL = "all";

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        boolean filter = false;
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        /**
         * FIXME This allows an EventStream to stream its events by not applying the fields filter.
         * However, this is done whenever the client includes "text/event-stream" in its Accept header.
         * If this is included with low priority along with other JSON-based media types, this may cause
         * the servlet to return a JSON-based media type instead of an EventStream. In that case, that
         * JSON-based media type will not have this field filter applied either.
         *
         * Until this can be implemented properly, clients should only include "text/event-stream" in
         * their Accept header if that's actually what they want in their response.
         */
        if(clientRequestsEventStream(request)) {
        	super.doFilter(request, response, chain);
        	return;
        }
        CharResponseWrapper wrappedResponse = new CharResponseWrapper(response);
        super.doFilter(request, wrappedResponse, chain);
        if (isJson(wrappedResponse.getContentType())) {
            filter = true;
        }

        List<String> fields = parseFieldsParameter(request);
        String jsonResponse = wrappedResponse.toString();

        if (fields.isEmpty() || fields.contains(FIELDS_VALUE_ALL) || wrappedResponse.getStatus() != 200 || jsonResponse.isBlank()) {
            filter = false;
        }

        if (filter) {
            jsonResponse = new JsonFieldsFilter().filterFieldsInJson(jsonResponse, fields);
        }

        response.setContentLength(jsonResponse.getBytes().length);
        response.getWriter().write(jsonResponse);
    }

	private List<String> parseFieldsParameter(HttpServletRequest request) {
		String[] clientProvidedFields = Optional.ofNullable(request.getParameterValues(QUERY_PARAM_FIELDS)).orElse(new String[] {});
		Object fieldsOverride = request.getAttribute(FIELDS_OVERRIDE_REQUEST_CONTEXT_PROPERTY);
		if (fieldsOverride != null) {
			clientProvidedFields = new String[] {String.valueOf(fieldsOverride)};
		}
		return Stream.of(clientProvidedFields)
				.flatMap(field -> Stream.of(field.split(FIELDS_PARAMS_SEPARATOR)))
				.map(param -> param.trim())
				.collect(Collectors.toList());
	}

    private boolean isJson(String contentType) {
        if (contentType != null && (contentType.startsWith(MEDIA_TYPE_JSON)
                || contentType.contains(MEDIA_TYPE_STRUCTURE_SUFFIX_JSON))) {
            return true;
        }
        return false;
    }

	private boolean clientRequestsEventStream(HttpServletRequest request) {
		String acceptValue = String.join(",", Collections.list(request.getHeaders("Accept")));
		if (acceptValue.contains("text/event-stream")) {
			return true;
		}
		return false;
	}
}
