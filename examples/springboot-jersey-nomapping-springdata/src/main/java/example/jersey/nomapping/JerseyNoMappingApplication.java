package example.jersey.nomapping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.servers.ServerVariable;

/**
 * Run the API server with the example endpoint and resource.
 */
@SpringBootApplication
@EntityScan("example.resources.jpa")
@OpenAPIDefinition(
		info = @Info(
				title = "test title",
				description = "test description",
				version = "1.0.0"
		),
		servers = {
				@Server(
						url = "http://{hostname}:{port number}",
						variables = {
								@ServerVariable(
										name = "hostname",
										defaultValue = "localhost"
								),
								@ServerVariable(
										name = "port number",
										defaultValue = "8888"
								)

						}
				)
		}
)
public class JerseyNoMappingApplication {
    public static void main(String[] args) {
        SpringApplication.run(JerseyNoMappingApplication.class, args);
    }
}
