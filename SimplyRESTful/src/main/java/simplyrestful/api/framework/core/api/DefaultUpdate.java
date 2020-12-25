package simplyrestful.api.framework.core.api;

import java.util.UUID;

import simplyrestful.api.framework.resources.HALResource;

public interface DefaultUpdate<T extends HALResource> {
    /**
     * Update the resource in the data store where it is stored.
     *
     * The resource should contain a self-link in order to identify which resource
     * needs to be updated.
     *
     * @param resource     is the updated resource (which contains a self-link with
     *                     which to identify the resource)
     * @param resourceUUID is the identifier of the resource that should be updated
     * @return the updated resource as persisted
     */
    public abstract T update(T resource, UUID resourceUUID);
}
