package example.jersey.nomapping;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

import example.jersey.nomapping.resources.ExampleWebResource;
import simplyrestful.api.framework.providers.ObjectMapperProvider;

@Configuration
public class ExampleResourceConfig extends ResourceConfig {
    public ExampleResourceConfig() {
        register(ExampleWebResource.class);
        register(ObjectMapperProvider.class);
    }
}
