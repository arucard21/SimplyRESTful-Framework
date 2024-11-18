package simplyrestful.api.framework.client;

import java.net.URI;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.GenericType;
import simplyrestful.api.framework.resources.ApiCollection;
import simplyrestful.api.framework.resources.ApiResource;

/**
 * A factory method for creating the SimplyRESTful client.
 *
 * @param <T> is the class of the resource used in the SimplyRESTful API that you wish to access.
 */
@Named
public class SimplyRestfulClientFactory<T extends ApiResource> {
	private final Client client;

	/**
	 * Create the factory.
	 *
	 * @param client is the JAX-RS client that should be used when the SimplyRESTful client executes HTTP requests.
	 */
	@Inject
	public SimplyRestfulClientFactory(Client client) {
		this.client = client;
	}

	/**
	 * Create a new SimplyRESTful client.
	 *
	 * @param baseApiUri is the base URI of the SimplyRESTful-based API that the client should access.
	 * @param typeForAPICollection is a GenericType object that indicates the typing for the collection of resources,
	 * e.g. {@code new GenericType<APICollection<YourApiResource>>() {}}. Due to type erasure, it needs to be provided here
	 * so the client knows the exact type for the collection containing API resources.
	 * @return a new SimplyRESTful-based client.
	 */
	public SimplyRestfulClient<T> newClient(URI baseApiUri, GenericType<ApiCollection<T>> typeForAPICollection){
		return new SimplyRestfulClient<T>(client, baseApiUri, typeForAPICollection);
	}
}
