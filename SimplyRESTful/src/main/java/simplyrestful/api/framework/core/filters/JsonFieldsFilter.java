package simplyrestful.api.framework.core.filters;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import simplyrestful.api.framework.core.DefaultWebResource;

@Provider
// TODO Perhaps make this a WriteInterceptor instead?
public class JsonFieldsFilter implements ContainerResponseFilter{
    private static final String FIELDS_VALUE_ALL = "all";
    @Context
    private ObjectMapper mapper;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
	if(!isJsonCompatibleMediaType(responseContext)) {
	    return;
	}
	showOnlyRequestedFields(requestContext, responseContext);
    }

    private boolean isJsonCompatibleMediaType(ContainerResponseContext responseContext) {
	if(responseContext.hasEntity() && responseContext.getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
	    return true;
	}
	return false;
    }

    private void showOnlyRequestedFields(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
	    throws JsonProcessingException {
	List<String> fields = getFieldsQueryParameter(requestContext);
	if (fields.contains(FIELDS_VALUE_ALL)) {
	    return;
	}
	// TODO maybe create a custom PropertyFilter that can filter nested fields
	mapper.setFilterProvider(
		new SimpleFilterProvider().addFilter(
			"fieldsFilter",
			SimpleBeanPropertyFilter.filterOutAllExcept(Set.copyOf(fields))));
    }

    private List<String> getFieldsQueryParameter(ContainerRequestContext requestContext) {
	return requestContext
		.getUriInfo()
		.getQueryParameters()
		.get(DefaultWebResource.QUERY_PARAM_FIELDS)
		.stream()
        	.flatMap(delimitedString -> Arrays.asList(delimitedString.split(DefaultWebResource.QUERY_PARAM_FIELDS_DELIMITER)).stream())
        	.map(String::trim)
        	.collect(Collectors.toList());
    }
}
