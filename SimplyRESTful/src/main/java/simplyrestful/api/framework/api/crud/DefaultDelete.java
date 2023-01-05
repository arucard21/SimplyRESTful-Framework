package simplyrestful.api.framework.api.crud;

import java.util.UUID;

import simplyrestful.api.framework.resources.APIResource;

public interface DefaultDelete<T extends APIResource> {
    /**
     * Remove a resource from the data store.
     *
     * @param resourceUUID is the identifier of the resource that should be removed
     * @return the removed resource, or null if it did not exist
     */
    public abstract T delete(UUID resourceUUID);
}
