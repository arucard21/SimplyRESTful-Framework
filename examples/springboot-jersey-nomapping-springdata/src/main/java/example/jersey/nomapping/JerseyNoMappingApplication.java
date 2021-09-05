package example.jersey.nomapping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * Run the API server with the example endpoint and resource.
 */
@SpringBootApplication
@EntityScan("example.resources.jpa")
public class JerseyNoMappingApplication {
    public static void main(String[] args) {
        SpringApplication.run(JerseyNoMappingApplication.class, args);
    }
}
