package simplyrestful.api.framework.client.test.implementation;

import jakarta.ws.rs.core.MediaType;
import simplyrestful.api.framework.resources.ApiResource;

public class TestResource extends ApiResource {
	public static final String MEDIA_TYPE_JSON = "application/x.testresource-v1+json";

	@Override
	public MediaType customJsonMediaType() {
		return MediaType.valueOf(MEDIA_TYPE_JSON);
	}
}
