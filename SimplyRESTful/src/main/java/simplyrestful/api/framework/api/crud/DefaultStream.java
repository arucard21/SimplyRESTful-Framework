package simplyrestful.api.framework.api.crud;

import java.util.List;
import java.util.stream.Stream;

import simplyrestful.api.framework.queryparams.SortOrder;
import simplyrestful.api.framework.resources.APIResource;

public interface DefaultStream<T extends APIResource> {
    /**
     * Retrieve the stream of resources that have been requested.
     *
     * For proper discoverability of the API, all links (href values in each Link
     * object) should contain absolute URI's and a self-link must be available in
     * each resource.
     *
     * @param fields is the list of fields on which to filter. This is only provided to optimize data
     * retrieval as the actual filtering of fields is done by the framework.
     * @param query is a FIQL query that defines how the resources should be filtered.
     * @param sort is the list of SortOrder objects, each containing the field name according to which
     * the collection should be sorted, along with whether is should be sorted ascending or not.
     * @return the filtered and sorted stream of resources for the requested page.
     */
    public Stream<T> stream(List<String> fields, String query, List<SortOrder> sort);
}
