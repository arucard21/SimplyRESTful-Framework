package example.jersey.nomapping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import simplyrestful.api.framework.filters.JsonFieldsServletFilter;

/**
 * Run the API server with the example endpoint and resource.
 */
@SpringBootApplication
@EntityScan("example.resources.jpa")
public class JerseyNoMappingApplication {
    public static void main(String[] args) {
        SpringApplication.run(JerseyNoMappingApplication.class, args);
    }

    @Bean
    public FilterRegistrationBean<JsonFieldsServletFilter> registerJsonFieldsFilter() {
        return new FilterRegistrationBean<JsonFieldsServletFilter>(new JsonFieldsServletFilter());
    }
}
