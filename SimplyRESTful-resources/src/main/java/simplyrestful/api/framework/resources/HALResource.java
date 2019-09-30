package simplyrestful.api.framework.resources;

import java.net.URI;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.annotation.Link;
import io.openapitools.jackson.dataformat.hal.annotation.Resource;

@Resource
public abstract class HALResource{
	@Link
	private HALLink self;

	public void setSelf(HALLink selfLink){
		this.self = selfLink;
	}

	public HALLink getSelf(){
		return self;
	}

	/**
	 * Provide a profile URI.
	 *
	 * This profile is a further specialization of your media type. It should point to a location where
	 * human-readable documentation about this profile, in this case your HAL-based resource, can be found.
	 *
	 * @return the URI that represents the profile for this resource.
	 */
	@JsonIgnore
	public abstract URI getProfile();

	@Override
	public int hashCode() {
		return Objects.hash(self);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof HALResource){
			HALResource resource = ((HALResource) obj);
			return resource.canEqual(this) &&
					Objects.equals(self, resource.getSelf());
		}
		return false;
	}

	protected boolean canEqual(Object obj) {
		return (obj instanceof HALResource);
	}
}
