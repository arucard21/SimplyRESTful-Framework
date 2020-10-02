package example.jersey.nomapping;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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
    public ExecutorService threadPool() {
	return Executors.newCachedThreadPool();
    }
}
