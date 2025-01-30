package simplyrestful.api.framework.api.crud;

import java.util.UUID;

/**
 * Interface for checking if an API resource exists.
 */
public interface ResourceExists {
    /**
     * Check if a resource, identified by its resourceURI, exists.
     *
     * @param resourceUUID is the identifier of a resource.
     * @return true if the resource identified by resourceURI exists, false
     *         otherwise.
     */
    public boolean exists(UUID resourceUUID);
}
