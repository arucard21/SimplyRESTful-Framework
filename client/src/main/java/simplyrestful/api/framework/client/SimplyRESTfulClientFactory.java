package simplyrestful.api.framework.client;

import java.net.URI;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;

import simplyrestful.api.framework.resources.HALResource;

@Named
public class SimplyRESTfulClientFactory<T extends HALResource> {
	private final Client client;

	@Inject
	public SimplyRESTfulClientFactory(Client client) {
		this.client = client;
	}

	public SimplyRESTfulClient<T> newClient(URI baseApiUri, Class<T> resourceClass){
		return new SimplyRESTfulClient<T>(client, baseApiUri, resourceClass);
	}
}
