package simplyrestful.api.framework.core.filters;

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
import javax.ws.rs.core.MediaType;

@Named
@WebFilter("*")
public class JsonFieldsServletFilter extends HttpFilter {
    private static final long serialVersionUID = 6825636135376615562L;
    private static final String QUERY_PARAM_FIELDS = "fields";
    private static final String FIELDS_PARAMS_SEPARATOR = ",";
    private static final String MEDIA_TYPE_STRUCTURE_SUFFIX_JSON = "+json";

    private static final String FIELDS_VALUE_ALL = "all";


    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
	CharResponseWrapper wrappedResponse = new CharResponseWrapper(response);
	super.doFilter(request, wrappedResponse, chain);
	String originalJson = wrappedResponse.toString();
	if(!isJsonCompatibleMediaType(response.getContentType())) {
	    response.setContentLength(originalJson.getBytes().length);
	    response.getWriter().write(originalJson);
	    return;
	}
	String[] parameterValues = request.getParameterValues(QUERY_PARAM_FIELDS);
	if(parameterValues == null) {
	    response.setContentLength(originalJson.getBytes().length);
	    response.getWriter().write(originalJson);
	    return;
	}
	List<String> fields = Stream.of(parameterValues)
		.flatMap(oneOrMoreParams -> Stream.of(oneOrMoreParams.split(FIELDS_PARAMS_SEPARATOR)))
		.map(param -> param.trim())
		.collect(Collectors.toList());
	if (fields.contains(FIELDS_VALUE_ALL) || fields.isEmpty()) {
	    response.setContentLength(originalJson.getBytes().length);
	    response.getWriter().write(originalJson);
	    return;
	}

	String fieldFilteredJson = new JsonFieldsFilter().filterFieldsInJson(originalJson, fields);
	response.setContentLength(fieldFilteredJson.getBytes().length);
	response.getWriter().write(fieldFilteredJson);
    }

    private boolean isJsonCompatibleMediaType(String contentType) {
	if(contentType != null &&
		(
			MediaType.valueOf(contentType).isCompatible(MediaType.APPLICATION_JSON_TYPE) ||
			MediaType.valueOf(contentType).getSubtype().endsWith(MEDIA_TYPE_STRUCTURE_SUFFIX_JSON))) {
	    return true;
	}
	return false;
    }
}
