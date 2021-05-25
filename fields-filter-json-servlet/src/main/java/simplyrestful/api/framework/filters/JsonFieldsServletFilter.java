package simplyrestful.api.framework.filters;

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
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
	String[] parameterValues = request.getParameterValues(QUERY_PARAM_FIELDS);
	if(parameterValues == null) {
	    super.doFilter(request, response, chain);
	    return;
	}
	List<String> fields = Stream.of(parameterValues)
		.flatMap(oneOrMoreParams -> Stream.of(oneOrMoreParams.split(FIELDS_PARAMS_SEPARATOR)))
		.map(param -> param.trim())
		.collect(Collectors.toList());
	if(fields.isEmpty() || fields.contains(FIELDS_VALUE_ALL)) {
	    super.doFilter(request, response, chain);
	    return;
	}
	CharResponseWrapper wrappedResponse = new CharResponseWrapper(response);
	super.doFilter(request, wrappedResponse, chain);
	if (!isJson(wrappedResponse.getContentType())) {
	    return;
	}
	String originalJson = wrappedResponse.toString();
	String fieldFilteredJson = new JsonFieldsFilter().filterFieldsInJson(originalJson, fields);
	response.setContentLength(fieldFilteredJson.getBytes().length);
	response.getWriter().write(fieldFilteredJson);
    }

    private boolean isJson(String contentType) {
	if(contentType != null && (
		contentType.startsWith(MEDIA_TYPE_JSON) ||
		contentType.contains(MEDIA_TYPE_STRUCTURE_SUFFIX_JSON))) {
	    return true;
	}
	return false;
    }
}
