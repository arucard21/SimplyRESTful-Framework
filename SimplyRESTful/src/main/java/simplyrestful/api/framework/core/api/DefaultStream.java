package simplyrestful.api.framework.core.api;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import simplyrestful.api.framework.resources.HALResource;

public interface DefaultStream<T extends HALResource> {
    /**
     * Retrieve the stream of resources that have been requested.
     *
     * For proper discoverability of the API, all links (href values in each HALLink
     * object) should contain absolute URI's and a self-link must be available in
     * each resource.
     *
     * @param fields is the list of fields on which to filter. This is only provided to optimize data
     * retrieval as the actual filtering of fields is done by the framework.
     * @param query is a FIQL query that defines how the resources should be filtered.
     * @param sort is the list of fields according to which the collection should be sorted. Each entry
     * provides the field name (dot-separated for nested fields) and the sort order (true for ascending, false for descending)
     * @return the filtered and sorted stream of resources for the requested page.
     */
    public Stream<T> stream(List<String> fields, String query, Map<String, Boolean> sort);
}
