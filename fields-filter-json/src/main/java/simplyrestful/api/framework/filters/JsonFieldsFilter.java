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
    /**
     * Tracks the current path in the original JSON structure, in dot-separated form (e.g. "top.inner.deepest")
     */
    private String currentPath = "";
    /**
     * Tracks whether to include the current part of the JSON structure.
     */
    private boolean include = false;
    /**
     * Tracks until which path to include or exclude the JSON structure.
     *
     * May be null, in which case there is no explicit inclusion or exclusion defined yet.
     * Usually occurs at the start or end of a JSON structure.
     */
    private String untilPath = null;
    /**
     * Tracks the path of any array currently being iterated through.
     *
     * Since arrays may be nested inside other arrays, this tracks the entire stack of
     * arrays. This variable will always reflect the path of the current array inside
     * which the parser is iterating. If not inside an array, the stack will be empty.
     */
    private Stack<String> arrayPath = new Stack<>();

    /**
     * Filter the fields in the provided JSON object according to the provided list
     * of field names.
     *
     * The field names can be nested, like "parent.child" or
     * "object.subobject.subsubobject".
     *
     * For any field that is included, all its children will also be included. If
     * the fields list is null or empty, the JSON is returned without modification.
     *
     * @param originalJson is the JSON object that should be filtered
     * @param fields       is the list of field names that should be kept in the
     *                     JSON object.
     * @return the JSON object that only includes the fields that are provided.
     */
    public String filterFieldsInJson(String originalJson, List<String> fields) {
        if (fields == null || fields.isEmpty()) {
            return originalJson;
        }
        List<String> pathToFields = generateAllPathsToProvidedFields(fields);
        StringWriter jsonWriter = new StringWriter();
        try (JsonParser parser = Json.createParser(new StringReader(originalJson));
                JsonGenerator generator = Json.createGenerator(jsonWriter)) {
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
                    if (pathToFields.contains(currentPath) || include) {
                        generator.writeKey(currentKey);
                        if (fields.contains(currentPath)) {
                            include = true;
                            untilPath = movePathUpOneLevel(currentPath);
                        }
                    }
                    else {
                        if (noExplicitInclusion()) {
                            include = false;
                            untilPath = movePathUpOneLevel(currentPath);
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
        for (String field : fields) {
            String[] fieldPathItems = field.split(Pattern.quote(FIELDS_NESTING_SEPARATOR));
            for (int i = 0; i < fieldPathItems.length; i++) {
                String[] pathToField = new String[i + 1];
                for (int j = 0; j <= i; j++) {
                    pathToField[j] = fieldPathItems[j];
                }
                pathToFields.add(String.join(FIELDS_NESTING_SEPARATOR, pathToField));
            }
        }
        return pathToFields;
    }

    private void writeStart(JsonGenerator generator, boolean isObject) {
        if (include || noExplicitInclusion()) {
            if (isObject) {
                generator.writeStartObject();
            } else {
                generator.writeStartArray();
            }
        }
        if (!isObject) {
            arrayPath.push(currentPath);
        }
    }

    private void writeEnd(JsonGenerator generator, boolean isObject, boolean hasNext) {
        if (include || noExplicitInclusion()) {
            generator.writeEnd();
        }
        if (!hasNext) {
            return;
        }
        if (isObject) {
            // check if still inside an array but not at the end of that array
            if (arrayPath.isEmpty() || !arrayPath.peek().equals(currentPath)) {
                moveCurrentPathUpOneLevel();
            }
        }
        else {
            arrayPath.pop();
            moveCurrentPathUpOneLevel();
        }
        if (currentPath.equals(untilPath)) {
            resetIncludeState();
        }
    }

    private <T> void writeValue(JsonGenerator generator, T value) {
        if (include || noExplicitInclusion()) {
            if (value == null) {
                generator.writeNull();
            }
            if (value instanceof String) {
                generator.write((String) value);
            } else if (Boolean.class.isInstance(value)) {
                generator.write((boolean) value);
            } else if (value instanceof BigDecimal) {
                generator.write((BigDecimal) value);
            }
        }
        moveCurrentPathUpOneLevel();
        if (currentPath.equals(untilPath)) {
            resetIncludeState();
        }
    }

    private void moveCurrentPathDownOneLevel(String nameOfNextLevel) {
        currentPath = currentPath.isBlank() ? nameOfNextLevel
                : currentPath + FIELDS_NESTING_SEPARATOR + nameOfNextLevel;
    }

    private void moveCurrentPathUpOneLevel() {
        currentPath = movePathUpOneLevel(currentPath);
    }

    private String movePathUpOneLevel(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalStateException("Cannot move the path up one level since it is already at the root.");
        }
        String[] newPath = path.split(Pattern.quote(FIELDS_NESTING_SEPARATOR));
        return newPath.length == 1 ? ""
                : String.join(FIELDS_NESTING_SEPARATOR, Arrays.copyOf(newPath, newPath.length - 1));
    }

    private void resetIncludeState() {
        untilPath = null;
        include = false;
    }

    private boolean noExplicitInclusion() {
        return untilPath == null;
    }
}
