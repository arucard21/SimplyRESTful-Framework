package simplyrestful.api.framework.resources;

import java.util.Objects;

import jakarta.ws.rs.core.MediaType;

public class ApiServiceDocument implements ApiResource {
	public static final String MEDIA_TYPE_JSON = "application/x.simplyrestful-servicedocument-v1+json";
	private Link self;
	private Link describedBy;

	// For JSON serialization
	public Link getSelf() {
		return this.self();
	}

	// For JSON deserialization
	public void setSelf(Link self) {
		this.self = self;
	}

	@Override
	public Link self() {
		return self;
	}

	@Override
	public MediaType customJsonMediaType() {
		return MediaType.valueOf(MEDIA_TYPE_JSON);
	}

	public Link getDescribedBy() {
		return describedBy;
	}

	public void setDescribedBy(Link describedBy) {
		this.describedBy = describedBy;
	}

	@Override
	public int hashCode() {
		return Objects.hash(self, describedBy);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ApiServiceDocument other = (ApiServiceDocument) obj;
		return Objects.equals(self, other.self) && Objects.equals(describedBy, other.describedBy);
	}
}
