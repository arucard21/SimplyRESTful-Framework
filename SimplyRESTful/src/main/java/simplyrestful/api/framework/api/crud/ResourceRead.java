package simplyrestful.api.framework.api.crud;

import java.util.UUID;

import simplyrestful.api.framework.resources.ApiResource;

/**
 * Interface for retrieving an API resource.
 *
 * @param <T>is the API resource class used in the JAX-RS WebResource.
 */
public interface ResourceRead<T extends ApiResource> {
    /**
     * Retrieve the resource from the data store where it is stored.
     *
     * The identifier provided by the API is the URI of the resource. This does not
     * have to be the identifier used in the data store (UUID is more commonly used)
     * but each entity in the data store must be uniquely identifiable by the
     * information provided in the URI.
     *
     * @param resourceUUID is the identifier (from API perspective) for the resource
     * @return the resource that was requested or null if it doesn't exist
     */
    public abstract T read(UUID resourceUUID);
}
