package example.jersey.nomapping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
}
