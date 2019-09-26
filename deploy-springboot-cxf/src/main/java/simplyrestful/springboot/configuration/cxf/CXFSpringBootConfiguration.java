package simplyrestful.springboot.configuration.cxf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import io.openapitools.jackson.dataformat.hal.HALMapper;

@Configuration
@ComponentScan(basePackages= {"simplyrestful"})
public class CXFSpringBootConfiguration{ /* Ensure that the simplyrestful package is scanned */
	@Bean
	public JacksonJsonProvider halProvider() {
		return new JacksonJsonProvider(new HALMapper());
	}
}
