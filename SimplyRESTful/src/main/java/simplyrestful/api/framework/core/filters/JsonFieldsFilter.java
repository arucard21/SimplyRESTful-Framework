package simplyrestful.api.framework.core.filters;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import simplyrestful.api.framework.core.DefaultWebResource;

@Named
@Provider
public class JsonFieldsFilter implements ContainerResponseFilter{
    private static final String MEDIA_TYPE_STRUCTURE_SUFFIX_JSON = "+json";
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
	if(responseContext.hasEntity() && 
		(
			responseContext.getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE) ||
			responseContext.getMediaType().getSubtype().endsWith(MEDIA_TYPE_STRUCTURE_SUFFIX_JSON))) {
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
	if(!fields.isEmpty()) {
	    throw new ServerErrorException("This API does not yet filter fields", 501);
	}
	// FIXME implement this missing functionality (maybe as WriteInterceptor?)
    }

    private List<String> getFieldsQueryParameter(ContainerRequestContext requestContext) {
	List<String> fields = requestContext
		.getUriInfo()
		.getQueryParameters()
		.get(DefaultWebResource.QUERY_PARAM_FIELDS);
	if(fields == null) {
	    return Collections.emptyList();
	}
	return fields
		.stream()
        	.flatMap(delimitedString -> Arrays.asList(delimitedString.split(DefaultWebResource.QUERY_PARAM_FIELDS_DELIMITER)).stream())
        	.map(String::trim)
        	.collect(Collectors.toList());
    }
}
