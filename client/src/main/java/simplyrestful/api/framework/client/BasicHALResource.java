package simplyrestful.api.framework.client;

import java.net.URI;

import javax.ws.rs.core.MediaType;

import simplyrestful.api.framework.resources.HALResource;

public class BasicHALResource extends HALResource {
	@Override
	public URI getProfile() {
	    return null;
	}

	@Override
	public MediaType getCustomJsonMediaType() {
	    return null;
	}
}
