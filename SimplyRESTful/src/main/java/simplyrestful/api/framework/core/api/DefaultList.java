package simplyrestful.api.framework.core.api;

import java.util.List;
import java.util.Map;

import simplyrestful.api.framework.resources.HALResource;

public interface DefaultList<T extends HALResource> {
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
    public List<T> list(int pageStart, int pageSize, List<String> fields, String query, Map<String, Boolean> sort);
}
