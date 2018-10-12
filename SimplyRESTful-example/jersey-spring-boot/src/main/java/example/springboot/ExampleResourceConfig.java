package example.springboot;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import example.springboot.resources.CustomRequestPropertiesFilter;
import example.springboot.resources.ExampleWebResource;
import io.openapitools.jackson.dataformat.hal.HALMapper;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import simplyrestful.api.framework.core.servicedocument.WebResourceRoot;

@Configuration
public class ExampleResourceConfig extends ResourceConfig {
	public ExampleResourceConfig() {
		register(CustomRequestPropertiesFilter.class);
		register(ExampleWebResource.class);
		registerInstances(new JacksonJsonProvider(new HALMapper()));
		packages(WebResourceRoot.class.getPackage().getName());
		register(ApiListingResource.class);
		register(SwaggerSerializers.class);
	}
}
