package simplyrestful.api.framework.resources.test;

import jakarta.ws.rs.core.MediaType;

import simplyrestful.api.framework.resources.ApiResource;

public class TestResource extends ApiResource {
	@Override
	public MediaType customJsonMediaType() {
		return new MediaType("application", "x.testresource-v1+json");
	}
}
