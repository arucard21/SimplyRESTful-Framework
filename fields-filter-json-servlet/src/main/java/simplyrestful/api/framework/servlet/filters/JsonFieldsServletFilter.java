package simplyrestful.api.framework.servlet.filters;

import java.io.IOException;
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
    private static final String MEDIA_TYPE_JSON = "application/json";
    private static final String MEDIA_TYPE_STRUCTURE_SUFFIX_JSON = "+json";
    public static final String FIELDS_PROVIDED_REQUEST_CONTEXT_PROPERTY = "simplyrestful.fields.json.provided";
    public static final String FIELDS_OVERRIDE_REQUEST_CONTEXT_PROPERTY = "simplyrestful.fields.json.override";
    public static final String QUERY_PARAM_FIELDS = "fields";
    public static final String FIELDS_PARAMS_SEPARATOR = ",";
    public static final String FIELDS_VALUE_ALL = "all";

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        boolean filter = false;
        if (request.getParameter(QUERY_PARAM_FIELDS) != null) {
        	request.setAttribute(FIELDS_PROVIDED_REQUEST_CONTEXT_PROPERTY, true);
        }
        CharResponseWrapper wrappedResponse = new CharResponseWrapper(response);
        super.doFilter(request, wrappedResponse, chain);
        if (isJson(wrappedResponse.getContentType())) {
            filter = true;
        }

        List<String> fields = parseFieldsParameter(request);

        if (fields.isEmpty() || fields.contains(FIELDS_VALUE_ALL)) {
            filter = false;
        }

        String jsonResponse = wrappedResponse.toString();
        if (filter) {
            jsonResponse = new JsonFieldsFilter().filterFieldsInJson(jsonResponse, fields);
        }

        response.setContentLength(jsonResponse.getBytes().length);
        response.getWriter().write(jsonResponse);
    }

	private List<String> parseFieldsParameter(HttpServletRequest request) {
		String[] parameterValuesArray = Optional.ofNullable(request.getParameterValues(QUERY_PARAM_FIELDS)).orElse(new String[] {});
		String fieldsParameter = String.join(FIELDS_PARAMS_SEPARATOR, parameterValuesArray);

		Object fieldsOverride = request.getAttribute(FIELDS_OVERRIDE_REQUEST_CONTEXT_PROPERTY);
        if(fieldsOverride != null) {
            fieldsParameter = String.valueOf(fieldsOverride);
        }

        return Stream.of(fieldsParameter.split(FIELDS_PARAMS_SEPARATOR))
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
}
