package simplyrestful.api.framework.filters;

import java.io.IOException;
import java.util.List;

import jakarta.inject.Named;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import simplyrestful.api.framework.QueryParamUtils;

@Named
@Provider
public class FieldsDefaultValueFilter implements ContainerResponseFilter {
	public static final String FIELDS_OVERRIDE_PROPERTY_NAME = "SIMPLYRESTFUL_FIELDS_JSON_OVERRIDE";

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		String fieldsDefaultValue = System.getProperty(FIELDS_OVERRIDE_PROPERTY_NAME, System.getenv(FIELDS_OVERRIDE_PROPERTY_NAME));
		if(fieldsDefaultValue != null) {
			QueryParamUtils.configureFieldsDefaultOverride(requestContext, QueryParamUtils.flattenQueryParameters(List.of(fieldsDefaultValue)));
		}

	}

}
