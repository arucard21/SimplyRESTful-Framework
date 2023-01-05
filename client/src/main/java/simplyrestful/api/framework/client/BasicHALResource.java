package simplyrestful.api.framework.client;

import javax.ws.rs.core.MediaType;

import simplyrestful.api.framework.resources.APIResource;

public class BasicHALResource extends APIResource {
	@Override
	public MediaType customJsonMediaType() {
	    return null;
	}
}
