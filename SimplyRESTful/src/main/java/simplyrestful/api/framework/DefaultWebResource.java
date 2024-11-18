package simplyrestful.api.framework;

import simplyrestful.api.framework.resources.ApiResource;
import simplyrestful.api.framework.webresource.api.implementation.DefaultCollectionGet;
import simplyrestful.api.framework.webresource.api.implementation.DefaultCollectionPost;
import simplyrestful.api.framework.webresource.api.implementation.DefaultResourceDelete;
import simplyrestful.api.framework.webresource.api.implementation.DefaultResourceGet;
import simplyrestful.api.framework.webresource.api.implementation.DefaultResourcePut;

public interface DefaultWebResource<T extends ApiResource> extends
	DefaultCollectionGet<T>,
	DefaultCollectionPost<T>,
	DefaultResourceGet<T>,
	DefaultResourcePut<T>,
	DefaultResourceDelete<T>{ /* Convenience web resource that provides all default REST API methods */ }