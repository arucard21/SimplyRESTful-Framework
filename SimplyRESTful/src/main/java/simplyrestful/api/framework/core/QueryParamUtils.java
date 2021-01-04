package simplyrestful.api.framework.core;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueryParamUtils {
    private static final String QUERY_PARAM_VALUE_DELIMITER = ",";
    private static final String HAL_EMBEDDED_OBJECT_NAME = "_embedded";
    private static final String HAL_LINKS_OBJECT_NAME = "_links";
    private static final String SORT_ORDER_DELIMITER = ";";
    private static final String SORT_ORDER_ASCENDING = "asc";

    private static List<String> flattenQueryParameters(List<String> parameters){
	return parameters.stream()
		.flatMap(param -> Stream.of(param.split(QUERY_PARAM_VALUE_DELIMITER)))
		.collect(Collectors.toList());
    }

    /**
     * Strips the HAL structure from the provided field names.
     * <p>
     * This will remove "_links." and "_embedded." from the provided fields
     * names. This ensures that these fields don't contain this HAL-specific
     * structure in their nesting format so their hierarchy should match that
     * of the POJOs.
     * </p>
     * @param values is a list of field names from which the HAL structure
     * should be removed.
     * @return the list of field names with their HAL structure removed.
     */
    public static List<String> stripHALStructure(List<String> fields) {
	return flattenQueryParameters(fields).stream()
		.map(QueryParamUtils::stripHALStructure)
		.map(String::trim)
		.filter(String::isBlank)
		.collect(Collectors.toList());
    }

    /**
     * Parse sort values as field name and sort order and remove the HAL structure.
     * <p>
     * This will parse the sort fields and sort order correctly, mapping them to a
     * String and Boolean value respectively. The HAL-structure will also be removed
     * from the sort fields so they match the hierarchy of the POJOs. If no sort order
     * is provided, the default sort order, asc, is used.
     * </p>
     * @param sortValues is the list of sort values that should be parsed.
     * @return a Map containing the field name and sort order as String and Boolean
     * respectively, for each field that should be sorted.
     */
    public static Map<String, Boolean> parseSort(List<String> sortValues) {
	return stripHALStructure(sortValues).stream()
		.collect(Collectors.toMap(QueryParamUtils::parseSortField, QueryParamUtils::parseSortOrder));
    }

    private static String parseSortField(String sortWithOrderDelimeter) {
	if (sortWithOrderDelimeter.contains(SORT_ORDER_DELIMITER)) {
	    return sortWithOrderDelimeter.split(SORT_ORDER_DELIMITER)[0];
	}
	else {
	    return sortWithOrderDelimeter;
	}
    }

    private static Boolean parseSortOrder(String sortWithOrderDelimeter) {
	if (sortWithOrderDelimeter.contains(SORT_ORDER_DELIMITER)) {
	    return Boolean.valueOf(sortWithOrderDelimeter.split(SORT_ORDER_DELIMITER)[1].equalsIgnoreCase(SORT_ORDER_ASCENDING));
	}
	else {
	    return false;
	}
    }

    /**
     * Strips the HAL structure from the provided String value.
     * <p>
     * This will remove "_links." and "_embedded." from the provided String value
     * This ensures that any fields in the String don't contain this HAL-specific
     * structure in their nesting format so their hierarchy should match that of
     * the POJOs.
     * </p>
     * @param value is the String containing field names with HAL-specific hierarchy.
     * @return the provided String without any HAL-specific hierarchy.
     */
    public static String stripHALStructure(String value) {
        return value.replaceAll(HAL_LINKS_OBJECT_NAME+".", "").replaceAll(HAL_EMBEDDED_OBJECT_NAME+".", "");
    }
}