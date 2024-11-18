package simplyrestful.api.framework.resources;

import java.util.Objects;

import jakarta.ws.rs.core.MediaType;

public abstract class ApiResource {
	private Link self;

	public void setSelf(Link selfLink) {
		this.self = selfLink;
	}

	public Link getSelf() {
		return self;
	}

	/**
	 * Provide the custom JSON media type representing this resource.
	 *
	 * @return the custom JSON media type for this resource.
	 */
	public abstract MediaType customJsonMediaType();

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
		ApiResource other = (ApiResource) obj;
		return Objects.equals(self, other.self);
	}
}
