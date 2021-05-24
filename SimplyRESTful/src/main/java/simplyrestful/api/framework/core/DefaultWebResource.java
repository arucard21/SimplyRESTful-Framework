package simplyrestful.api.framework.core;

import simplyrestful.api.framework.core.webresource.api.implementation.DefaultCollectionGet;
import simplyrestful.api.framework.core.webresource.api.implementation.DefaultCollectionPost;
import simplyrestful.api.framework.core.webresource.api.implementation.DefaultResourceDelete;
import simplyrestful.api.framework.core.webresource.api.implementation.DefaultResourceGet;
import simplyrestful.api.framework.core.webresource.api.implementation.DefaultResourcePut;
import simplyrestful.api.framework.resources.HALResource;

public interface DefaultWebResource<T extends HALResource> extends
	DefaultCollectionGet<T>,
	DefaultCollectionPost<T>,
	DefaultResourceGet<T>,
	DefaultResourcePut<T>,
	DefaultResourceDelete<T>{ /* Convenience web resource that provides all default REST API methods */ }