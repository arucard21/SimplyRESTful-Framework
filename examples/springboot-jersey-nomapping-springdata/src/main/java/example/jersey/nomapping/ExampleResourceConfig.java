package example.jersey.nomapping;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

import example.jersey.nomapping.resources.ExampleWebResource;
import simplyrestful.api.framework.core.filters.AcceptHeaderModifier;

@Configuration
public class ExampleResourceConfig extends ResourceConfig {
	public ExampleResourceConfig() {
		register(ExampleWebResource.class);
		register(AcceptHeaderModifier.class);
	}
}
