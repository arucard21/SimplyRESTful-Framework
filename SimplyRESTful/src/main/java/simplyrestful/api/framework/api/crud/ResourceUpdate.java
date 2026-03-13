package simplyrestful.api.framework.api.crud;

import java.net.URI;
import java.util.UUID;

import simplyrestful.api.framework.resources.ApiResource;

/**
 * Interface for updating an API resource.
 *
 * @param <T>is the API resource type used in the JAX-RS WebResource.
 */
public interface ResourceUpdate<T extends ApiResource> {
    /**
     * Update the existing resource in the data store where it is stored.
     *
     * The provided resource will contain a self-link that identifies itself through a URI containing a UUID.
     * Since the UUID must always be the last segment of the self-link, it can be easily parsed from the self-link with the
     * {@link simplyrestful.api.framework.utils.WebResourceUtils#parseUuidFromLastSegmentOfUri(URI resourceUri)} method.
     *
     * @param resource is the updated resource
     * @return the updated resource as persisted
     */
    public abstract T update(T resource, UUID resourceUUID);
}
