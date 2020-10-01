package simplyrestful.springboot.configuration.jersey;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.springframework.boot.autoconfigure.jersey.ResourceConfigCustomizer;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import io.swagger.v3.jaxrs2.integration.resources.AcceptHeaderOpenApiResource;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import simplyrestful.api.framework.core.filters.JsonFieldsFilter;
import simplyrestful.api.framework.core.providers.JacksonHALJsonProvider;
import simplyrestful.api.framework.core.providers.ObjectMapperProvider;
import simplyrestful.api.framework.core.servicedocument.WebResourceRoot;

@Configuration
public class JerseySpringBootConfiguration implements ResourceConfigCustomizer{
	@Override
	public void customize(ResourceConfig config) {
		config.register(WebResourceRoot.class);
		config.register(ObjectMapperProvider.class);
		config.register(JacksonHALJsonProvider.class);
		config.register(JacksonJsonProvider.class);
		config.register(JsonFieldsFilter.class);
		config.register(OpenApiResource.class);
		config.register(AcceptHeaderOpenApiResource.class);
		config.property(ServerProperties.WADL_FEATURE_DISABLE, true);
	}
}
