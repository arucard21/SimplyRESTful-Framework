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
		APIServer.run(ExampleApiEndpoint.class);
	}
}
