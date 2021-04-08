package simplyrestful.api.framework.core;

import simplyrestful.api.framework.core.api.webresource.DefaultCollectionGet;
import simplyrestful.api.framework.core.api.webresource.DefaultCollectionPost;
import simplyrestful.api.framework.core.api.webresource.DefaultResourceDelete;
import simplyrestful.api.framework.core.api.webresource.DefaultResourceGet;
import simplyrestful.api.framework.core.api.webresource.DefaultResourcePut;
import simplyrestful.api.framework.resources.HALResource;

public interface DefaultWebResource<T extends HALResource> extends
	DefaultCollectionGet<T>,
	DefaultCollectionPost<T>,
	DefaultResourceGet<T>,
	DefaultResourcePut<T>,
	DefaultResourceDelete<T>{ /* Convenience web resource that provides all default REST API methods */ }