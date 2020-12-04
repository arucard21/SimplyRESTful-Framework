package simplyrestful.api.framework.core.filters;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import javax.inject.Named;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import simplyrestful.api.framework.core.DefaultWebResource;

@Named
@WebFilter("*")
public class JsonFieldsFilter extends HttpFilter {
    private static final long serialVersionUID = 6825636135376615562L;
    private static final String MEDIA_TYPE_STRUCTURE_SUFFIX_JSON = "+json";
    private static final String FIELDS_VALUE_ALL = "all";

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
	CharResponseWrapper wrappedResponse = new CharResponseWrapper(response);
	super.doFilter(request, wrappedResponse, chain);
	String originalJson = wrappedResponse.toString();
	response.setContentLength(originalJson.getBytes().length);
	response.getWriter().write(originalJson);
	if(!isJsonCompatibleMediaType(response.getContentType())) {
	    return;
	}
	String[] parameterValues = request.getParameterValues(DefaultWebResource.QUERY_PARAM_FIELDS);
	if(parameterValues == null) {
	    return;
	}
	List<String> fields = Arrays.asList(parameterValues);
	if (fields.contains(FIELDS_VALUE_ALL) || fields.isEmpty()) {
	    return;
	}
	String keyPath = "";
	String fieldFilteredJson;
	try(
		StringWriter jsonWriter = new StringWriter();
		JsonParser parser = Json.createParser(new StringReader(originalJson));
		JsonGenerator generator = Json.createGenerator(jsonWriter)){
	    while (parser.hasNext()) {
    	    	switch (parser.next()) {
    	    	case END_ARRAY:
    	    	case END_OBJECT:
    	    	    generator.writeEnd();
    	    	    break;
    	    	case KEY_NAME:
    	    	    generator.writeKey(parser.getString());
    	    	    break;
    	    	case START_ARRAY:
    	    	    generator.writeStartArray();
    	    	    break;
    	    	case START_OBJECT:
    	    	    generator.writeStartObject();
    	    	    break;
    	    	case VALUE_FALSE:
    	    	    generator.write(false);
    	    	    break;
    	    	case VALUE_NULL:
    	    	    generator.writeNull();
    	    	    break;
    	    	case VALUE_NUMBER:
    	    	    generator.write(parser.getBigDecimal());
    	    	    break;
    	    	case VALUE_STRING:
    	    	    generator.write(parser.getString());
    	    	    break;
    	    	case VALUE_TRUE:
    	    	    generator.write(true);
    	    	    break;
    	    	default:
    	    	    break;
    	    	}
    	    }
	    fieldFilteredJson = jsonWriter.toString();
	}
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
