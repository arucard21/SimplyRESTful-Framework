package simplyrestful.api.framework.api.crud;

import java.util.UUID;

import simplyrestful.api.framework.resources.HALResource;

public interface DefaultUpdate<T extends HALResource> {
    /**
     * Update the existing resource in the data store where it is stored.
     *
     * The provided resource may contain a self-link that identifies itself through a URI containing a UUID.
     * For convenience, this UUID is also provided directly as resourceUUID parameter.
     *
     * @param resource is the updated resource
     * @param resourceUUID is the identifier of the resource that should be updated
     * @return the updated resource as persisted
     */
    public abstract T update(T resource, UUID resourceUUID);
}
