package simplyrestful.api.framework.resources.test;

import java.util.Objects;

import jakarta.ws.rs.core.MediaType;

import simplyrestful.api.framework.resources.ApiResource;
import simplyrestful.api.framework.resources.Link;

public class TestResource implements ApiResource {
	private Link self;

	@Override
	public Link self() {
		return self;
	}

	public void setSelf(Link self) {
		this.self = self;
	}

	@Override
	public MediaType customJsonMediaType() {
		return new MediaType("application", "x.testresource-v1+json");
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
