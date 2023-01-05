package simplyrestful.api.framework.resources.test;

import javax.ws.rs.core.MediaType;

import simplyrestful.api.framework.resources.APIResource;

public class TestResource extends APIResource {
	@Override
	public MediaType customJsonMediaType() {
		return new MediaType("application", "x.testresource-v1+json");
	}
}
