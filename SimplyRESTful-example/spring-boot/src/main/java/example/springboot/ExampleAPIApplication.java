package example.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Run the API server with the example endpoint and resource.
 *
 * @author RiaasM
 *
 */
@SpringBootApplication(scanBasePackages="simplyrestful.springboot,example.springboot")
public class ExampleAPIApplication{
	public static void main(String[] args) {
		SpringApplication.run(ExampleAPIApplication.class, args);
	}
}
