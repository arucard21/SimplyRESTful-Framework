package simplyrestful.api.framework.core.api.crud;

import java.util.UUID;

import simplyrestful.api.framework.resources.HALResource;

public interface DefaultDelete<T extends HALResource> {
    /**
     * Remove a resource from the data store.
     *
     * @param resourceUUID is the identifier of the resource that should be removed
     * @return the removed resource, or null if it did not exist
     */
    public abstract T delete(UUID resourceUUID);
}
