package simplyrestful.springboot.configuration.cxf;

import org.apache.cxf.jaxrs.swagger.Swagger2Feature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import io.openapitools.jackson.dataformat.hal.HALMapper;

@Configuration
@ComponentScan(basePackages = { "simplyrestful" })
public class CXFSpringBootConfiguration { /* Ensure that the simplyrestful package is scanned */
    @Bean
    @ConditionalOnMissingBean
    public JacksonJsonProvider halProvider() {
	return new JacksonJsonProvider(new HALMapper());
    }
    
    @Bean
    @ConditionalOnMissingBean
    public Swagger2Feature swaggerFeature() {
	return new Swagger2Feature();
    }
}
