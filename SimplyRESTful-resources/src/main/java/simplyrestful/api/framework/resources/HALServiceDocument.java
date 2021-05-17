package simplyrestful.api.framework.resources;

import java.net.URI;

import javax.ws.rs.core.MediaType;

import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.annotation.Link;

public class HALServiceDocument extends HALResource {
    public static final String MEDIA_TYPE_JSON = "application/x.simplyrestful-servicedocument-v1+json";
    public static final String PROFILE_STRING = "https://arucard21.github.io/SimplyRESTful-Framework/ServiceDocument/v1";
    @Link
    private HALLink describedBy;

    @Override
    public URI getProfile() {
	return URI.create(PROFILE_STRING);
    }

    @Override
    public MediaType getCustomJsonMediaType() {
	return MediaType.valueOf(MEDIA_TYPE_JSON);
    }

    public HALLink getDescribedBy() {
	return describedBy;
    }

    public void setDescribedBy(HALLink describedBy) {
	this.describedBy = describedBy;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = super.hashCode();
	result = prime * result + ((describedBy == null) ? 0 : describedBy.hashCode());
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
	if (describedBy == null) {
	    if (other.describedBy != null)
		return false;
	} else if (!describedBy.equals(other.describedBy))
	    return false;
	return true;
    }
}
