package simplyrestful.jersey.springboot.configuration;

import javax.inject.Inject;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.autoconfigure.jersey.ResourceConfigCustomizer;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;

@Configuration
public class JerseySpringBootConfiguration implements ResourceConfigCustomizer{
	@Inject
	private JacksonJsonProvider customJsonProvider;
	
	@Override
	public void customize(ResourceConfig config) {
		config.registerInstances(customJsonProvider);
		config.packages("simplyrestful");
		config.register(ApiListingResource.class);
		config.register(SwaggerSerializers.class);
	}
}
