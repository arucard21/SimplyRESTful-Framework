package example.nlgov_adr;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.context.annotation.Configuration;

import example.nlgov_adr.resources.ExampleWebResource;
import jakarta.ws.rs.ApplicationPath;

@ApplicationPath("v0")
@Configuration
public class ExampleResourceConfig extends ResourceConfig {
    public ExampleResourceConfig() {
        register(ExampleWebResource.class);
        property(ServletProperties.FILTER_FORWARD_ON_404, true);
    }
}
