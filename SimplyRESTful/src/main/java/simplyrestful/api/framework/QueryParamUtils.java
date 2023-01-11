package simplyrestful.api.framework;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.UriInfo;
import simplyrestful.api.framework.filters.FieldsDefaultValueFilter;
import simplyrestful.api.framework.queryparams.SortOrder;
import simplyrestful.api.framework.webresource.api.implementation.DefaultCollectionGet;

public class QueryParamUtils {
	public static final String FIELDS_OVERRIDE_REQUEST_CONTEXT_PROPERTY = "simplyrestful.fields.json.override";
	public static final String QUERY_PARAM_VALUE_DELIMITER = ",";

	public static void configureFieldsDefaultOverride(ContainerRequestContext requestContext, String fieldsDefaults) {
		configureFieldsDefaultOverride(requestContext, flattenQueryParameters(List.of(fieldsDefaults)));
	}

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
    public static void configureFieldsDefaultOverride(ContainerRequestContext requestContext, List<String> fieldsDefaults) {
    	if (!fieldsQueryParamProvided(requestContext.getUriInfo())) {
    		requestContext.setProperty(
    				FIELDS_OVERRIDE_REQUEST_CONTEXT_PROPERTY,
    				String.join(QUERY_PARAM_VALUE_DELIMITER, fieldsDefaults));
    	}
    }

    public static void configureFieldsDefaultProperty(List<String> fieldsDefaults) {
    	System.setProperty(
    			FieldsDefaultValueFilter.FIELDS_OVERRIDE_PROPERTY_NAME,
    			String.join(QueryParamUtils.QUERY_PARAM_VALUE_DELIMITER, fieldsDefaults));
    }

    private static boolean fieldsQueryParamProvided(UriInfo uriInfo) {
    	return uriInfo.getQueryParameters().containsKey(DefaultCollectionGet.QUERY_PARAM_FIELDS);
	}

    /**
     * Parse sort values as field name and sort order.
     * <p>
     * This will parse the sort fields and sort order correctly, mapping them to a
     * String and Boolean value respectively. If no sort order is provided, the default
     * sort order, asc, is used.
     *
     * The Boolean value in the map uses true for ascending and false for descending order.
     *
     * </p>
     * @param sortValues is the list of sort values that should be parsed.
     * @return a List containing the field name and sort order as SortOrder object for each
     * field that should be sorted.
     */
    public static List<SortOrder> parseSort(List<String> sortValues) {
    	return sortValues.stream()
    		.filter(sortValue -> !sortValue.isBlank())
    		.map(String::trim)
    		.map(SortOrder::from)
    		.collect(Collectors.toList());
    }

    /**
     * Flatten the list of query parameter values so that each entry corresponds to a single value
     * (and never multiple comma-separated values).
     *
     * @param parameters list of query parameters, some of which may contain multiple comma-separated values
     * @return a flattened list of query parameters with one value each.
     */
    public static List<String> flattenQueryParameters(List<String> parameters){
    	return parameters.stream()
    		.flatMap(param -> Stream.of(param.split(QUERY_PARAM_VALUE_DELIMITER)))
    		.collect(Collectors.toList());
    }
}