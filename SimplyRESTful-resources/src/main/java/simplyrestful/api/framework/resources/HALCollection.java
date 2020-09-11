package simplyrestful.api.framework.resources;

import io.openapitools.jackson.dataformat.hal.annotation.Resource;

@Resource
public abstract class HALCollection<T extends HALResource> extends HALResource {}
