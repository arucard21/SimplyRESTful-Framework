package simplyrestful.api.framework.resources.test;

import jakarta.ws.rs.core.MediaType;

import simplyrestful.api.framework.resources.ApiResource;
import simplyrestful.api.framework.resources.Link;

public record TestRecordResource(Link self, MediaType customJsonMediaType) implements ApiResource {
	public static final MediaType MEDIA_TYPE = new MediaType("application", "x.testrecordresource-v1+json");

	public TestRecordResource() {
		this(null, MEDIA_TYPE);
	}

	public TestRecordResource(Link self) {
		this(self, MEDIA_TYPE);
	}
}
