package simplyrestful.api.framework.api.crud;

import java.util.UUID;

import simplyrestful.api.framework.resources.APIResource;

public interface DefaultRead<T extends APIResource> {
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
