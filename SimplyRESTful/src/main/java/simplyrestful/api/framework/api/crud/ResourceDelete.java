package simplyrestful.api.framework.api.crud;

import java.util.UUID;

import simplyrestful.api.framework.resources.ApiResource;

/**
 * Interface for creating a new API resource.
 *
 * @param <T>is the API resource class used in the JAX-RS WebResource.
 */
public interface ResourceDelete<T extends ApiResource> {
    /**
     * Remove a resource from the data store.
     *
     * @param resourceUUID is the identifier of the resource that should be removed
     * @return the removed resource, or null if it did not exist
     */
    public abstract T delete(UUID resourceUUID);
}
