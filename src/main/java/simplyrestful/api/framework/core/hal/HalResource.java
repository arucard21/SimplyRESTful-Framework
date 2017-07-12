package simplyrestful.api.framework.core.hal;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonIgnore;

import dk.nykredit.jackson.dataformat.hal.HALLink;
import dk.nykredit.jackson.dataformat.hal.annotation.Link;
import dk.nykredit.jackson.dataformat.hal.annotation.Resource;

@Resource
public abstract class HalResource{
	@Link
	protected HALLink self;

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
}
