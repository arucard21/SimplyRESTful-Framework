package example.jersey.nomapping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import simplyrestful.api.framework.filters.JsonFieldsServletFilter;

/**
 * Run the API server with the example endpoint and resource.
 *
 * @author RiaasM
 *
 */
@SpringBootApplication
public class JerseyNoMappingApplication {
    public static void main(String[] args) {
	SpringApplication.run(JerseyNoMappingApplication.class, args);
    }

    @Bean
    public FilterRegistrationBean<JsonFieldsServletFilter> jsonFieldsFilter() {
	FilterRegistrationBean<JsonFieldsServletFilter> registrationBean = new FilterRegistrationBean<>();
	registrationBean.setFilter(new JsonFieldsServletFilter());
	return registrationBean;
    }
}
