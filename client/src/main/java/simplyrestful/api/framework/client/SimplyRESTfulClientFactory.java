package simplyrestful.api.framework.client;

import java.net.URI;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;

import simplyrestful.api.framework.resources.APIResource;

@Named
public class SimplyRESTfulClientFactory<T extends APIResource> {
	private final Client client;

	@Inject
	public SimplyRESTfulClientFactory(Client client) {
		this.client = client;
	}

	public SimplyRESTfulClient<T> newClient(URI baseApiUri, Class<T> resourceClass){
		return new SimplyRESTfulClient<T>(client, baseApiUri, resourceClass);
	}
}
