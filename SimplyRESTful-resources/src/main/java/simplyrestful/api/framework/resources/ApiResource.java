package simplyrestful.api.framework.resources;

import jakarta.ws.rs.core.MediaType;

/**
 * Contract for API resources that defines the methods required by the framework.
 *
 * <p>This interface allows both classes and Java records to be used as API resources.</p>
 */
public interface ApiResource {
	/**
	 * Get the self-link for this resource.
	 *
	 * @return the self-link for this resource.
	 */
	Link self();

	/**
	 * Provide the custom JSON media type representing this resource.
	 *
	 * @return the custom JSON media type for this resource.
	 */
	MediaType customJsonMediaType();
}
