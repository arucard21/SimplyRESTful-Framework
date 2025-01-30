package simplyrestful.api.framework.api.crud;

/**
 * Interface for counting the amount of API resources in a filtered collection. 
 */
public interface ResourceCount {
    /**
     * Retrieve how many resources are available after filtering according to the provided query.
     *
     * @param query is a FIQL query that defines how the resources should be filtered.
     * @return the total amount of resources that are available
     */
    int count(String query);
}
