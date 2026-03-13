package simplyrestful.api.framework.client.test.implementation;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import jakarta.ws.rs.core.MediaType;
import simplyrestful.api.framework.resources.ApiResource;
import simplyrestful.api.framework.resources.Link;

public class TestResource implements ApiResource {
	public static final String MEDIA_TYPE_JSON = "application/x.testresource-v1+json";

	private Link self;

	@Override
	@JsonGetter("self")
	public Link self() {
		return self;
	}

	@JsonSetter("self")
	public void self(Link self) {
		this.self = self;
	}

	@Override
	public MediaType customJsonMediaType() {
		return MediaType.valueOf(MEDIA_TYPE_JSON);
	}

	@Override
	public int hashCode() {
		return Objects.hash(self);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestResource other = (TestResource) obj;
		return Objects.equals(self, other.self);
	}
}