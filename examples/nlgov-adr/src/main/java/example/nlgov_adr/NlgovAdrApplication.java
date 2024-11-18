package example.nlgov_adr;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import jakarta.servlet.Filter;

/**
 * Run the API server with the example endpoint and resource.
 */
@SpringBootApplication(exclude = WebMvcAutoConfiguration.class)
@EntityScan("example.resources.jpa")
@OpenAPIDefinition( info = @Info(title = "test title", description = "test description", version = "1.0.0"))
public class NlgovAdrApplication {
    public static void main(String[] args) {
        SpringApplication.run(NlgovAdrApplication.class, args);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
    	CorsConfiguration config = new CorsConfiguration();
    	config.setAllowedOrigins(List.of("*"));
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
    	UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    	source.registerCorsConfiguration("/**", config);
    	return source;
    }

    @Bean
    Filter corsFilter(CorsConfigurationSource configSource) {
    	return new CorsFilter(configSource);
    }
}
