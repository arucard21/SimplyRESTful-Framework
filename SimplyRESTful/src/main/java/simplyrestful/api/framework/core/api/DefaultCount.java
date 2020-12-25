package simplyrestful.api.framework.core.api;

public interface DefaultCount {
    /**
     * Retrieve how many resources are available after filtering according to the provided query.
     *
     * @param query is a FIQL query that defines how the resources should be filtered.
     * @return the total amount of resources that are available
     */
    int count(String query);
}
