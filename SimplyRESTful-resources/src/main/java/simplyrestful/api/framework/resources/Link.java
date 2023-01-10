package simplyrestful.api.framework.resources;

import java.net.URI;
import java.util.Objects;

import jakarta.ws.rs.core.MediaType;

public class Link{
	private URI href;
	private MediaType type;

	public Link() {}

	public Link(String href, String type) {
		this.href = href== null ? null : URI.create(href);
		this.type = type == null ? null : MediaType.valueOf(type);
	}

	public Link(URI href, MediaType type) {
		this.href = href;
		this.type = type;
	}

	public URI getHref() {
		return href;
	}

	public void setHref(URI href) {
		this.href = href;
	}

	public MediaType getType() {
		return type;
	}

	public void setType(MediaType type) {
		this.type = type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(href, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Link other = (Link) obj;
		return Objects.equals(href, other.href) && Objects.equals(type, other.type);
	}
}
