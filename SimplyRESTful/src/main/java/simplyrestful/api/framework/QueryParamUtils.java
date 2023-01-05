package simplyrestful.api.framework;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.container.ContainerRequestContext;

import simplyrestful.api.framework.queryparams.SortOrder;
import simplyrestful.api.framework.webresource.api.implementation.DefaultCollectionGet;

public class QueryParamUtils {
	public static final String HAL_EMBEDDED_OBJECT_NAME = "_embedded";
	public static final String HAL_LINKS_OBJECT_NAME = "_links";
	public static final String FIELDS_OVERRIDE_REQUEST_CONTEXT_PROPERTY = "simplyrestful.fields.json.override";
	public static final String QUERY_PARAM_VALUE_DELIMITER = ",";

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
    				String.join(QUERY_PARAM_VALUE_DELIMITER, fieldsDefaults));
    	}
    }

    public static boolean fieldsQueryParamProvided(ContainerRequestContext requestContext) {
    	return requestContext.getUriInfo().getQueryParameters().containsKey(DefaultCollectionGet.QUERY_PARAM_FIELDS);
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
    	return sortValues.stream()
    		.filter(sortValue -> !sortValue.isBlank())
    		.map(String::trim)
    		.map(SortOrder::from)
    		.collect(Collectors.toList());
    }

    public static List<String> flattenQueryParameters(List<String> parameters){
    	return parameters.stream()
    		.flatMap(param -> Stream.of(param.split(QUERY_PARAM_VALUE_DELIMITER)))
    		.collect(Collectors.toList());
    }
}