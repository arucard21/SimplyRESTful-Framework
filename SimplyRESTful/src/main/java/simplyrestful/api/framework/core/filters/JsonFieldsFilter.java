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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import simplyrestful.api.framework.core.DefaultWebResource;

public class JsonFieldsFilter implements ContainerResponseFilter{
    @Context
    private ObjectMapper mapper;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
	if(!responseContext.hasEntity() || !responseContext.getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
	    return;
	}
	List<String> fields = requestContext
		.getUriInfo()
		.getQueryParameters()
		.get(DefaultWebResource.QUERY_PARAM_FIELDS)
		.stream()
        	.flatMap(delimitedString -> Arrays.asList(delimitedString.split(DefaultWebResource.QUERY_PARAM_FIELDS_DELIMITER)).stream())
        	.map(String::trim)
        	.collect(Collectors.toList());
	Object entity = responseContext.getEntity();
	responseContext.setEntity(
		mapper.writer(
			new SimpleFilterProvider().addFilter(
				"fieldsFilter",
				SimpleBeanPropertyFilter.filterOutAllExcept(Set.copyOf(fields))))
		.writeValueAsString(entity));
	
	
	
    }
}
