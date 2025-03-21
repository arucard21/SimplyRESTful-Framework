package simplyrestful.api.framework.swagger;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import io.swagger.v3.core.filter.AbstractSpecFilter;
import io.swagger.v3.core.model.ApiDescription;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import jakarta.ws.rs.core.MediaType;
import simplyrestful.api.framework.resources.ApiCollection;
import simplyrestful.api.framework.resources.ApiResource;
import simplyrestful.api.framework.utils.MediaTypeUtils;

/**
 * A filter that cleans up the generated OpenAPI Specification document.
 *
 * Due to technical limitations, the generated OpenAPI Specification document
 * still contains some details that are incorrect. This filter fixes those
 * details (as much as possible).
 */
public class SimplyRestfulOpenApiFilter extends AbstractSpecFilter {
	/**
	 * The name of the media type parameter indicating the server-side preferences.
	 *
	 * This media type parameter is defined by JAX-RS. It is similar to the "q" parameter
	 * for client-side preferences, which is defined for HTTP.
	 */
	public static final String MEDIA_TYPE_PARAMETER_NAME_QS = "qs";

	/**
	 * Remove the "qs" parameter from all media types.
	 *
	 * If the removal of this parameter causes the map to contain duplicates, it is
	 * assumed that their values would have been the same (since this parameter
	 * should not have any effect on the schema that is mapped to it). This allows
	 * it to be easily resolved by simply using the value of the first entry and
	 * ignoring that of the second entry.
	 */
	@Override
	public Optional<Operation> filterOperation(Operation operation, ApiDescription api,
			Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
		if(operation == null) {
			return Optional.ofNullable(null);
		}
		ApiResponses responses = operation.getResponses();
		if (responses == null || responses.isEmpty()) {
			return Optional.of(operation);
		}
		for(ApiResponse response : responses.values()) {
			Content content = response.getContent();
			if(content == null) {
				continue;
			}
			Content modifiedContent = new Content();
			for(Entry<String, io.swagger.v3.oas.models.media.MediaType> mediaTypeEntry: content.entrySet()) {
				String modifiedMediaType = MediaTypeUtils.withoutQualityParameters(MediaType.valueOf(mediaTypeEntry.getKey())).toString();
				modifiedContent.addMediaType(modifiedMediaType, mediaTypeEntry.getValue());
			}
			response.setContent(modifiedContent);
		}
		return super.filterOperation(operation, api, params, cookies, headers);
	}

	/**
	 * Remove the schema that's detected for the {@link ApiCollection} or {@link ApiResource} parent class.
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Optional<Schema> filterSchema(Schema schema, Map<String, List<String>> params, Map<String, String> cookies,
			Map<String, List<String>> headers) {
		String apiResourceSchemaName = ApiResource.class.getSimpleName();
		String apiCollectionParentSchemaName = ApiCollection.class.getSimpleName();
		String apiCollectionSchemaName = apiCollectionParentSchemaName + apiResourceSchemaName;
		String schemaName = schema.getName();
		if (
				schemaName.equals(apiCollectionParentSchemaName) ||
				schemaName.equals(apiCollectionSchemaName) ||
				schemaName.equals(apiResourceSchemaName)) {
			return Optional.empty();
		}
		return super.filterSchema(schema, params, cookies, headers);
	}
}
