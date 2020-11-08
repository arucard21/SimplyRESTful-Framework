package simplyrestful.api.framework.core;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

public interface ResourceAccess<T> {
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

    /**
     * Remove a resource from the data store.
     *
     * @param resourceUUID is the identifier of the resource that should be removed
     * @return the removed resource, or null if it did not exist
     */
    public abstract T delete(UUID resourceUUID);

    /**
     * Retrieve the paged collection of resources that have been requested.
     *
     * For proper discoverability of the API, all links (href values in each HALLink
     * object) should contain absolute URI's and a self-link must be available in
     * each resource.
     *
     * @param pageStart is the offset at which the requested page starts.
     * @param pageSize is the requested size of each page.
     * @param fields is the list of fields on which to filter. This is only provided to optimize data
     * retrieval as the actual filtering of fields is done by the framework.
     * @param query is a FIQL query that defines how the resources should be filtered.
     * @param sort is the list of fields according to which the collection should be sorted. Each entry
     * provides the field name (dot-separated for nested fields) and the sort order (true for ascending, false for descending)
     * @return the filtered and sorted list of resources for the requested page.
     */
    public abstract List<T> list(int pageStart, int pageSize, List<String> fields, String query, Map<String, Boolean> sort);

    /**
     * Retrieve the stream of resources that have been requested.
     *
     * For proper discoverability of the API, all links (href values in each HALLink
     * object) should contain absolute URI's and a self-link must be available in
     * each resource.
     *
     * For convenience, the default implementation for this is to simply provide a stream based on the list retrieved
     * by the list() method. If your underlying data store supports streaming data, you should override this method and use
     * that streaming mechanism to enable proper streaming.
     *
     * @param fields is the list of fields on which to filter. This is only provided to optimize data
     * retrieval as the actual filtering of fields is done by the framework.
     * @param query is a FIQL query that defines how the resources should be filtered.
     * @param sort is the list of fields according to which the collection should be sorted. Each entry
     * provides the field name (dot-separated for nested fields) and the sort order (true for ascending, false for descending)
     * @return the filtered and sorted stream of resources for the requested page.
     */
    default Stream<T> stream(List<String> fields, String query, Map<String, Boolean> sort){
	return list(0, Integer.MAX_VALUE, fields, query, sort).stream();
    }

    /**
     * Retrieve how many resources are available.
     *
     * This provides a simple implementation for convenience but should be
     * overridden with an optimized implementation, if possible.
     *
     * @param query is a FIQL query that defines how the resources should be filtered.
     * @return the total amount of resources that are available
     */
    int count(String query);

    /**
     * Check if a resource, identified by its resourceURI, exists.
     *
     * This provides a simple implementation for convenience but should be
     * overridden with an optimized implementation, if possible.
     *
     * @param resourceUUID is the identifier of a resource.
     * @return true if the resource identified by resourceURI exists, false
     *         otherwise.
     */
    default boolean exists(UUID resourceUUID) {
        return Objects.nonNull(this.read(resourceUUID));
    }
}
