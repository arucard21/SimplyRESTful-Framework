package simplyrestful.api.framework.client;

import java.net.URI;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;

import simplyrestful.api.framework.resources.APIResource;

/**
 * A factory method for creating the SimplyRESTful client.
 *
 * @param <T> is the class of the resource used in the SimplyRESTful API that you wish to access.
 */
@Named
public class SimplyRESTfulClientFactory<T extends APIResource> {
	private final Client client;

	/**
	 * Create the factory.
	 *
	 * @param client is the JAX-RS client that should be used when creating the SimplyRESTful client.
	 */
	@Inject
	public SimplyRESTfulClientFactory(Client client) {
		this.client = client;
	}

	/**
	 * Create a new SimplyRESTful client.
	 *
	 * @param baseApiUri is the base URI of the SimplyRESTful-based API that the client should access.
	 * @param resourceClass is the class of the resource used in the SimplyRESTful-based API that the client should access.
	 * @return a new SimplyRESTful-based client.
	 */
	public SimplyRESTfulClient<T> newClient(URI baseApiUri, Class<T> resourceClass){
		return new SimplyRESTfulClient<T>(client, baseApiUri, resourceClass);
	}
}
