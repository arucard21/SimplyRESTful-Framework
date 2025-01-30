package simplyrestful.api.framework;

import simplyrestful.api.framework.resources.ApiResource;
import simplyrestful.api.framework.webresource.api.implementation.DefaultCollectionGet;
import simplyrestful.api.framework.webresource.api.implementation.DefaultCollectionPost;
import simplyrestful.api.framework.webresource.api.implementation.DefaultResourceDelete;
import simplyrestful.api.framework.webresource.api.implementation.DefaultResourceGet;
import simplyrestful.api.framework.webresource.api.implementation.DefaultResourcePut;

/**
 * Provide a default implementation for CRUDL functionality.
 *
 * This is provided as a convenience so you can easily include CRUDL
 * functionality in a JAX-RS WebResource, rather than having to
 * include them one-by-one.
 *
 * @param <T>is the API resource class used in the JAX-RS WebResource.
 */
public interface DefaultWebResource<T extends ApiResource> extends
	DefaultCollectionGet<T>,
	DefaultCollectionPost<T>,
	DefaultResourceGet<T>,
	DefaultResourcePut<T>,
	DefaultResourceDelete<T>{ /* Convenience web resource that provides a default implementation for CRUDL functionality for the API. */ }