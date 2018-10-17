package simplyrestful.springboot.configuration.jersey;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.autoconfigure.jersey.ResourceConfigCustomizer;
import org.springframework.context.annotation.Configuration;

import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;

@Configuration
public class JerseySpringBootConfiguration implements ResourceConfigCustomizer{
	@Override
	public void customize(ResourceConfig config) {
		config.packages("simplyrestful");
		config.register(ApiListingResource.class);
		config.register(SwaggerSerializers.class);
	}
}
