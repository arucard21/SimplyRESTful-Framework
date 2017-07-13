import example.resources.ExampleApiEndpoint;
import simplyrestful.jetty.deploy.APIServer;

/**
 * Run the API server with the example endpoint and resource.
 *
 * @author RiaasM
 *
 */
public class ExampleAPIServer {
	public static void main(String[] args) {
		// Make the example API available on all network interfaces on port 9000
		APIServer.run("http://localhost:9000", ExampleApiEndpoint.class);
	}
}
