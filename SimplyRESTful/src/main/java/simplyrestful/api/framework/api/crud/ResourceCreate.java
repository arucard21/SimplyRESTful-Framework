package simplyrestful.api.framework.api.crud;

import java.util.UUID;

import simplyrestful.api.framework.resources.ApiResource;

/**
 * Interface for creating a new API resource.
 *
 * @param <T>is the API resource type used in the JAX-RS WebResource.
 */
public interface ResourceCreate<T extends ApiResource> {
    /**
     * Create the resource.
     *
     * The resource returned by this method must contain the self link with the absolute URL to
     * this resource which ends with the UUID identifier of this resource.
     *
     * @param resource is the resource that should be created which does not contain a self link.
     * @return the created resource, containing a self-link with the absolute URL to itself.
     */
    public abstract T create(T resource, UUID resourceUUID);
}
