package simplyrestful.api.framework.core.filters;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

@Named
@WebFilter("*")
public class JsonFieldsFilter extends HttpFilter {
    private static final String QUERY_PARAM_FIELDS = "fields";
    private static final String FIELDS_PARAMS_SEPARATOR = ",";
    private static final String FIELDS_NESTING_SEPARATOR = ".";
    private static final long serialVersionUID = 6825636135376615562L;
    private static final String MEDIA_TYPE_STRUCTURE_SUFFIX_JSON = "+json";
    private static final String FIELDS_VALUE_ALL = "all";
    private String skipUntilPath;
    private String keepUntilPath;
    private Stack<String> arrayPath;
    private String currentPath;

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
	List<String> pathToFields = new ArrayList<>();
	for(String field : fields) {
	    String[] fieldPathItems = field.split(Pattern.quote(FIELDS_NESTING_SEPARATOR));
	    for(int i = 0 ; i < fieldPathItems.length ; i++) {
		String[] pathToField = new String[i+1];
		for (int j = 0 ; j <= i ; j++) {
		    pathToField[j] = fieldPathItems[j];
		}
		pathToFields.add(String.join(FIELDS_NESTING_SEPARATOR, pathToField));
	    }
	}
	String fieldFilteredJson = filterFieldsInJson(originalJson, fields, pathToFields);
	response.setContentLength(fieldFilteredJson.getBytes().length);
	response.getWriter().write(fieldFilteredJson);
    }

    private String filterFieldsInJson(String originalJson, List<String> fields, List<String> pathToFields) throws IOException {
	skipUntilPath = null;
	keepUntilPath = null;
	arrayPath = new Stack<>();
	currentPath = "";
	try(
		StringWriter jsonWriter = new StringWriter();
		JsonParser parser = Json.createParser(new StringReader(originalJson));
		JsonGenerator generator = Json.createGenerator(jsonWriter)){
	    while (parser.hasNext()) {
    	    	switch (parser.next()) {
    	    	case END_ARRAY:
    	    	    writeEnd(generator, false, parser.hasNext());
    	    	    break;
    	    	case END_OBJECT:
    	    	    writeEnd(generator, true, parser.hasNext());
    	    	    break;
    	    	case KEY_NAME:
    	    	    String currentKey = parser.getString();
    	    	    moveCurrentPathDownOneLevel(currentKey);
    	    	    if(pathToFields.contains(currentPath) || keepUntilPath != null){
    	    		generator.writeKey(currentKey);
    	    		if(fields.contains(currentPath)){
    	    		    if(skipUntilPath != null) {
    	    			throw new IllegalStateException("The filter cannot be in skip state when moving into keep state");
    	    		    }
    	    		    keepUntilPath = movePathUpOneLevel(currentPath);
    	    		}
    	    	    }
    	    	    else{
    	    		if(keepUntilPath != null) {
    	    		    throw new IllegalStateException("The filter cannot be in keep state when moving into skip state");
    	    		}
    	    		if (skipUntilPath == null) {
    	    		    skipUntilPath = movePathUpOneLevel(currentPath);
    	    		}
    	    	    }
    	    	    break;
    	    	case START_ARRAY:
    	    	    writeStart(generator, false);
    	    	    break;
    	    	case START_OBJECT:
    	    	    writeStart(generator, true);
    	    	    break;
    	    	case VALUE_FALSE:
    	    	    writeValue(generator, false);
    	    	    break;
    	    	case VALUE_NULL:
    	    	    writeValue(generator, null);
    	    	    break;
    	    	case VALUE_NUMBER:
    	    	    writeValue(generator, parser.getBigDecimal());
    	    	    break;
    	    	case VALUE_STRING:
    	    	    writeValue(generator, parser.getString());
    	    	    break;
    	    	case VALUE_TRUE:
    	    	    writeValue(generator, true);
    	    	    break;
    	    	default:
    	    	    break;
    	    	}
    	    }
	    generator.flush();
	    return jsonWriter.toString();
	}
    }

    private void writeStart(JsonGenerator generator, boolean isObject) {
    	if (skipUntilPath == null || keepUntilPath != null) {
    	    if(isObject) {
    		generator.writeStartObject();
    	    }
    	    else {
    		generator.writeStartArray();
    	    }
    	}
    	if (!isObject) {
    	    arrayPath.push(currentPath);
    	}
    }

    private void writeEnd(JsonGenerator generator, boolean isObject, boolean hasNext) {
	if(skipUntilPath == null || keepUntilPath != null) {
	    generator.writeEnd();
	}
	if(isObject && hasNext) {
	    if(arrayPath.isEmpty() || !arrayPath.peek().equals(currentPath)) {
		moveCurrentPathUpOneLevel();
	    }
	    if(currentPath.equals(skipUntilPath)) {
		skipUntilPath = null;
	    }
	    if(currentPath.equals(keepUntilPath)) {
		keepUntilPath = null;
	    }
	}
	if(!isObject) {
	    arrayPath.pop();
	    moveCurrentPathUpOneLevel();
	}
    }

    private <T> void writeValue(JsonGenerator generator, T value) {
	if (skipUntilPath == null || keepUntilPath != null) {
	    if (value == null) {
		generator.writeNull();
	    }
	    if (value instanceof String) {
		generator.write((String) value);
	    }
	    else if (Boolean.class.isInstance(value)) {
		generator.write((boolean) value);
	    }
	    else if (value instanceof BigDecimal) {
		generator.write((BigDecimal) value);
	    }
	}
	moveCurrentPathUpOneLevel();
	if(currentPath.equals(skipUntilPath)) {
	    skipUntilPath = null;
	}
	if(currentPath.equals(keepUntilPath)) {
	    keepUntilPath = null;
	}
    }

    private void moveCurrentPathDownOneLevel(String nameOfNextLevel) {
	currentPath = currentPath.isBlank() ?
		nameOfNextLevel : currentPath + FIELDS_NESTING_SEPARATOR + nameOfNextLevel;
    }

    private void moveCurrentPathUpOneLevel() {
	currentPath = movePathUpOneLevel(currentPath);
    }

    private String movePathUpOneLevel(String path) {
	if(path == null || path.isBlank()) {
	    throw new IllegalStateException("Cannot move the path up one level since it is already at the root.");
	}
	String[] newPath = path.split(Pattern.quote(FIELDS_NESTING_SEPARATOR));

	return newPath.length == 1 ?
		"" : String.join(FIELDS_NESTING_SEPARATOR, Arrays.copyOf(newPath, newPath.length - 1));
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
