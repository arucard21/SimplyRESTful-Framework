package simplyrestful.api.framework.resources;

import java.net.URI;

import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.annotation.Link;

public class HALServiceDocument extends HALResource {
	public static final String PROFILE_STRING = "https://arucard21.github.io/SimplyRESTful-Framework/ServiceDocument/v1";
	
	@Link
	private HALLink describedby;

	@Override
	public URI getProfile() {
		return URI.create(PROFILE_STRING);
	}

	public HALLink getDescribedby() {
		return describedby;
	}

	public void setDescribedby(HALLink describedby) {
		this.describedby = describedby;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((describedby == null) ? 0 : describedby.hashCode());
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
		HALServiceDocument other = (HALServiceDocument) obj;
		if (describedby == null) {
			if (other.describedby != null)
				return false;
		}
		else if (!describedby.equals(other.describedby))
			return false;
		return true;
	}
}
