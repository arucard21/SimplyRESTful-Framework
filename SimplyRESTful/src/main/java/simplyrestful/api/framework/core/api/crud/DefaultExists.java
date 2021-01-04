package simplyrestful.api.framework.core.api.crud;

import java.util.UUID;

public interface DefaultExists {
    /**
     * Check if a resource, identified by its resourceURI, exists.
     *
     * @param resourceUUID is the identifier of a resource.
     * @return true if the resource identified by resourceURI exists, false
     *         otherwise.
     */
    public boolean exists(UUID resourceUUID);
}
