package example.jetty;

import java.lang.reflect.InvocationTargetException;

import org.apache.cxf.endpoint.Server;

import example.jetty.resources.CustomRequestPropertiesFilter;
import example.jetty.resources.ExampleResourceDAO;
import example.jetty.resources.ExampleWebResource;
import simplyrestful.jetty.deploy.ServerBuilder;

/**
 * Run the API server with the example endpoint and resource.
 *
 * @author RiaasM
 *
 */
public class ExampleAPIServer {
	public static void main(String[] args) throws IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException {
		Server apiServer = new ServerBuilder()
				.withAddress("http://localhost:9000")
				.withWebResource(ExampleWebResource.class, ExampleResourceDAO.class)
				.withProvider(CustomRequestPropertiesFilter.class)
				.build();
		try {
			Thread.sleep(60 * 60 * 1000);
		}
		catch (InterruptedException e) {
			apiServer.destroy();
		}
	}
}
