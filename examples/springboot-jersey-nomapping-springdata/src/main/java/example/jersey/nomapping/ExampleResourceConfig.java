package example.jersey.nomapping;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.context.annotation.Configuration;

import example.jersey.nomapping.resources.ExampleWebResource;

@Configuration
public class ExampleResourceConfig extends ResourceConfig {
    public ExampleResourceConfig() {
        register(ExampleWebResource.class);
        property(ServletProperties.FILTER_FORWARD_ON_404, true);
    }
}
