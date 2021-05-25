package simplyrestful.api.framework.filters;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;

public class JsonFieldsFilter {
    public static final String FIELDS_NESTING_SEPARATOR = ".";

    private String skipUntilPath = null;
    private String keepUntilPath = null;
    private Stack<String> arrayPath = new Stack<>();
    private String currentPath = "";

    /**
     * Filter the fields in the provided JSON object according to the provided list of field names.
     *
     * The field names can be nested, like "parent.child" or "object.subobject.subsubobject".
     *
     * For any field that is included, all its children will also be included.
     *
     * @param originalJson is the JSON object that should be filtered
     * @param fields is the list of field names that should be kept in the JSON object.
     * @return the JSON object that only includes the fields that are provided.
     */
    public String filterFieldsInJson(String originalJson, List<String> fields) {
	List<String> pathToFields = generateAllPathsToProvidedFields(fields);
	StringWriter jsonWriter = new StringWriter();
	try(
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

    private List<String> generateAllPathsToProvidedFields(List<String> fields) {
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
	return pathToFields;
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


}
