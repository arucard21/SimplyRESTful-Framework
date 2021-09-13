package simplyrestful.api.framework;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.container.ContainerRequestContext;

import simplyrestful.api.framework.queryparams.SortOrder;

public class QueryParamUtils {
    private static final String QUERY_PARAM_VALUE_DELIMITER = ",";
    private static final String HAL_EMBEDDED_OBJECT_NAME = "_embedded";
    private static final String HAL_LINKS_OBJECT_NAME = "_links";
    private static final String FIELDS_OVERRIDE_REQUEST_CONTEXT_PROPERTY = "simplyrestful.fields.json.override";
	private static final String FIELDS_PARAM_NAME = "fields";
	private static final String FIELDS_PARAM_SEPARATOR = ",";

	/**
	 * Set the default value for the list of fields as request property as fields override.
	 *
	 * The fields filter will then use this request property (as a servlet request attribute) to
	 * filter the fields.
	 *
	 * This property is only set if the fields parameter was not provided by the user so it will not
	 * override anything that the user provides.
	 *
	 * @param requestContext is a JAX-RS context object.
	 * @param fieldsDefaults is the list of fields that should be used as default value.
	 */
    public static void configureFieldsDefault(ContainerRequestContext requestContext, List<String> fieldsDefaults) {
    	if (!fieldsQueryParamProvided(requestContext)) {
    		requestContext.setProperty(
    				FIELDS_OVERRIDE_REQUEST_CONTEXT_PROPERTY,
    				String.join(FIELDS_PARAM_SEPARATOR, fieldsDefaults));
    	}
    }

    private static boolean fieldsQueryParamProvided(ContainerRequestContext requestContext) {
    	return requestContext.getUriInfo().getQueryParameters().containsKey(FIELDS_PARAM_NAME);
	}

    /**
     * Strips the HAL structure from the provided field names.
     * <p>
     * This will remove "_links." and "_embedded." from the provided fields
     * names. This ensures that these fields don't contain this HAL-specific
     * structure in their nesting format so their hierarchy should match that
     * of the POJOs.
     * </p>
     * @param fields is a list of field names from which the HAL structure
     * should be removed.
     * @return the list of field names with their HAL structure removed.
     */
    public static List<String> stripHALStructure(List<String> fields) {
    	return flattenQueryParameters(fields).stream()
    		.map(QueryParamUtils::stripHALStructure)
    		.map(String::trim)
    		.filter(trimmed -> !trimmed.isBlank())
    		.collect(Collectors.toList());
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
        String stripped = value.replaceAll(HAL_LINKS_OBJECT_NAME+".", "").replaceAll(HAL_EMBEDDED_OBJECT_NAME+".", "");
        if(HAL_LINKS_OBJECT_NAME.equals(stripped) || HAL_EMBEDDED_OBJECT_NAME.equals(stripped)) {
            return "";
        }
        return stripped;
    }

    /**
     * Parse sort values as field name and sort order and remove the HAL structure.
     * <p>
     * This will parse the sort fields and sort order correctly, mapping them to a
     * String and Boolean value respectively. The HAL-structure will also be removed
     * from the sort fields so they match the hierarchy of the POJOs. If no sort order
     * is provided, the default sort order, asc, is used.
     *
     * The Boolean value in the map uses true for ascending and false for descending order.
     *
     * </p>
     * @param sortValues is the list of sort values that should be parsed.
     * @return a List containing the field name and sort order as SortOrder object for each
     * field that should be sorted.
     */
    public static List<SortOrder> parseSort(List<String> sortValues) {
    	return stripHALStructure(sortValues).stream()
    		.filter(sortValue -> !sortValue.isBlank())
    		.map(String::trim)
    		.map(SortOrder::from)
    		.collect(Collectors.toList());
    }

    private static List<String> flattenQueryParameters(List<String> parameters){
    	return parameters.stream()
    		.flatMap(param -> Stream.of(param.split(QUERY_PARAM_VALUE_DELIMITER)))
    		.collect(Collectors.toList());
    }
}