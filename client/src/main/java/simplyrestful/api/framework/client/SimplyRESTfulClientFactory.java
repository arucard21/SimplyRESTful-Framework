package simplyrestful.api.framework.client;

import java.net.URI;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.openapitools.jackson.dataformat.hal.HALMapper;
import simplyrestful.api.framework.resources.HALResource;

@Named
public class SimplyRESTfulClientFactory<T extends HALResource> {
	private final Client client;
	private ObjectMapper halMapper;

	@Inject
	public SimplyRESTfulClientFactory(Client client) {
		this.client = client;
		this.halMapper = new HALMapper();
	}

	public SimplyRESTfulClientFactory<T> withMapper(ObjectMapper halMapper){
		this.halMapper = halMapper;
		return this;
	}

	public SimplyRESTfulClient<T> newClient(URI baseApiUri, Class<T> resourceClass){
		return new SimplyRESTfulClient<T>(client, halMapper, baseApiUri, resourceClass);
	}
}
