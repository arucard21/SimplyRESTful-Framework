package example.jetty;

import org.apache.cxf.endpoint.Server;

import example.jetty.resources.ExampleWebResource;
import example.jetty.resources.dao.ExampleEntityDAOProvider;
import simplyrestful.jetty.deploy.ServerBuilder;

/**
 * Run the API server with the example endpoint and resource.
 *
 * @author RiaasM
 *
 */
public class ExampleCXFJettyServer {
	public static void main(String[] args) throws ReflectiveOperationException {
		Server apiServer = new ServerBuilder()
				.withAddress("http://localhost:8080")
				.withWebResource(ExampleWebResource.class)
				.withProvider(new ExampleEntityDAOProvider())
				.build();
		try {
			Thread.sleep(60 * 60 * 1000);
		}
		catch (InterruptedException e) {
			apiServer.destroy();
		}
	}
}
