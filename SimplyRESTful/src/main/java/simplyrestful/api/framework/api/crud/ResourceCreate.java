package simplyrestful.api.framework.api.crud;

import java.util.UUID;

import simplyrestful.api.framework.resources.ApiResource;

/**
 * Interface for creating a new API resource.
 *
 * @param <T>is the API resource class used in the JAX-RS WebResource.
 */
public interface ResourceCreate<T extends ApiResource> {
    /**
     * Create the resource in the data store where it is stored.
     *
     * The resource should contain a self-link that contains the unique ID for this
     * resource.
     *
     * @param resource     is the resource that should be created, containing a
     *                     self-link with its unique ID
     * @param resourceUUID is the unique ID of the resource which should match the
     *                     UUID used in the self-link
     * @return the created resource as persisted
     */
    public abstract T create(T resource, UUID resourceUUID);
}
