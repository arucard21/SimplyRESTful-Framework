package simplyrestful.api.framework.core.api;

import java.util.UUID;

import simplyrestful.api.framework.resources.HALResource;

public interface DefaultCreate<T extends HALResource> {
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
