package simplyrestful.api.framework.core.filters;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;

import simplyrestful.api.framework.core.DefaultWebResource;

@CollectionResource
@Provider
public class JsonSortFilter implements ContainerResponseFilter{
    private static final String FIELDS_VALUE_ALL = "all";
    @Context
    private ObjectMapper mapper;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
	// TODO Perhaps make this a WriteInterceptor instead?
    }

    private Map<String, String> getSortQueryParameter(ContainerRequestContext requestContext) {
	return requestContext
		.getUriInfo()
		.getQueryParameters()
		.get(DefaultWebResource.QUERY_PARAM_SORT)
		.stream()
		.filter(sortField -> sortField.contains(DefaultWebResource.QUERY_PARAM_SORT_DELIMITER))
		.flatMap(sortMultipleEntries -> Arrays.asList(sortMultipleEntries.split(DefaultWebResource.QUERY_PARAM_SORT_DELIMITER)).stream())
		.map(String::trim)
		.filter(sortSingleEntry -> sortSingleEntry.contains(DefaultWebResource.QUERY_PARAM_SORT_ORDER_DELIMITER))
		.collect(Collectors.toMap(
			sortWithOrderDelimeter -> sortWithOrderDelimeter.split(DefaultWebResource.QUERY_PARAM_SORT_ORDER_DELIMITER)[0], 
			sortWithOrderDelimeter -> sortWithOrderDelimeter.split(DefaultWebResource.QUERY_PARAM_SORT_ORDER_DELIMITER)[1]));
    }
}
