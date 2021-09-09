package simplyrestful.api.framework.servlet.filters;

import java.io.IOException;
import java.util.List;
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
    private static final String QUERY_PARAM_FIELDS = "fields";
    private static final String FIELDS_PARAMS_SEPARATOR = ",";
    private static final String MEDIA_TYPE_JSON = "application/json";
    private static final String MEDIA_TYPE_STRUCTURE_SUFFIX_JSON = "+json";
    private static final String FIELDS_VALUE_ALL = "all";

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        boolean filter = false;
        CharResponseWrapper wrappedResponse = new CharResponseWrapper(response);
        super.doFilter(request, wrappedResponse, chain);
        if (isJson(wrappedResponse.getContentType())) {
            filter = true;
        }
        String[] parameterValues = request.getParameterValues(QUERY_PARAM_FIELDS);
        List<String> fields = parameterValues == null ? List.of()
                : Stream.of(parameterValues)
                        .flatMap(oneOrMoreParams -> Stream.of(oneOrMoreParams.split(FIELDS_PARAMS_SEPARATOR)))
                        .map(param -> param.trim()).collect(Collectors.toList());
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

    private boolean isJson(String contentType) {
        if (contentType != null && (contentType.startsWith(MEDIA_TYPE_JSON)
                || contentType.contains(MEDIA_TYPE_STRUCTURE_SUFFIX_JSON))) {
            return true;
        }
        return false;
    }
}
