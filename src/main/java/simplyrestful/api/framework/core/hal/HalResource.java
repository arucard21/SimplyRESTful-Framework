package simplyrestful.api.framework.core.hal;

import dk.nykredit.jackson.dataformat.hal.HALLink;
import dk.nykredit.jackson.dataformat.hal.annotation.Link;
import dk.nykredit.jackson.dataformat.hal.annotation.Resource;

@Resource
public class HalResource{
	@Link
	protected HALLink self;

	public void setSelf(HALLink selfLink){
		this.self = selfLink;
	}

	public HALLink getSelf(){
		return self;
	}
}
