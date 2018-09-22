package example.jetty;

import example.jetty.resources.ExampleApiEndpoint;
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
		APIServer apiServer = new APIServer("http://localhost:9000", ExampleApiEndpoint.class);
		try {
			Thread.sleep(60 * 60 * 1000);
		}
		catch (InterruptedException e) {
			apiServer.getCXFServer().destroy();
		}
	}
}
