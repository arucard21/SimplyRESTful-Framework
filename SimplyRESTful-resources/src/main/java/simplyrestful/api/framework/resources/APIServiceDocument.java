package simplyrestful.api.framework.resources;

import java.util.Objects;

import jakarta.ws.rs.core.MediaType;

public class APIServiceDocument extends APIResource {
	public static final String MEDIA_TYPE_JSON = "application/x.simplyrestful-servicedocument-v1+json";
	private Link describedBy;

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
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(describedBy);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		APIServiceDocument other = (APIServiceDocument) obj;
		return Objects.equals(describedBy, other.describedBy);
	}
}
