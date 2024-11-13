package simplyrestful.api.framework.utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import simplyrestful.api.framework.queryparams.SortOrder;

public class QueryParamUtils {
	public static final String QUERY_PARAM_FIELDS = "fields";
	public static final String QUERY_PARAM_VALUE_DELIMITER = ",";
	public static final String FIELDS_VALUE_ALL = "all";

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

    public static List<String> flattenQueryParameters(List<String> parameters){
    	return parameters.stream()
    		.flatMap(param -> Stream.of(param.split(QUERY_PARAM_VALUE_DELIMITER)))
    		.map(String::trim)
    		.collect(Collectors.toList());
    }
}