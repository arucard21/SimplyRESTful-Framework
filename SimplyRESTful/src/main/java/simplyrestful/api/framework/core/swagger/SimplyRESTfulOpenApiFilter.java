package simplyrestful.api.framework.core.swagger;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import io.swagger.v3.core.filter.AbstractSpecFilter;
import io.swagger.v3.core.model.ApiDescription;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import simplyrestful.api.framework.resources.HALCollection;
import simplyrestful.api.framework.resources.HALResource;

public class SimplyRESTfulOpenApiFilter extends AbstractSpecFilter{

    /**
     * Remove the "qs" parameter from all media types.
     *
     * If the removal of this parameter causes the map to contain duplicates, it
     * is assumed that their values would have been the same (since this parameter
     * should not have any effect on the schema that is mapped to it). This allows it
     * to be easily resolved by simply using the value of the first entry and
     * ignoring that of the second entry.
     */
    @Override
    public Optional<Operation> filterOperation(
	    Operation operation,
	    ApiDescription api,
	    Map<String, List<String>> params,
	    Map<String, String> cookies,
	    Map<String, List<String>> headers) {
	Content openApiMediaTypes = operation.getResponses().getDefault().getContent();
	Content modifiedOpenApiMediaTypes = new Content();
	openApiMediaTypes.entrySet().stream()
		.collect(Collectors.toMap(
			entry -> withoutQsParameter(entry.getKey()),
			Entry::getValue,
			(duplicate1, duplicate2) -> duplicate1))
		.forEach(modifiedOpenApiMediaTypes::addMediaType);

	operation.getResponses().getDefault().setContent(modifiedOpenApiMediaTypes);
	return super.filterOperation(operation, api, params, cookies, headers);
    }

    /**
     * Remove the schema that's detected for the {@link HALResource} and {@link HALCollection} parent classes.
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Optional<Schema> filterSchema(
	    Schema schema,
	    Map<String, List<String>> params,
	    Map<String, String> cookies,
	    Map<String, List<String>> headers) {
	String halResourceSchemaName = HALResource.class.getSimpleName();
	String halCollectionSchemaName = HALCollection.class.getSimpleName() + HALResource.class.getSimpleName();
	String schemaName = schema.getName();
	if (schemaName.equals(halResourceSchemaName) || schemaName.equals(halCollectionSchemaName)) {
	    return Optional.empty();
	}
	return super.filterSchema(schema, params, cookies, headers);
    }

    private String withoutQsParameter(String mediaTypeString) {
        MediaType mediaType = MediaType.valueOf(mediaTypeString);
        Map<String, String> parameters = mediaType.getParameters().entrySet().stream()
        	.filter(entry -> !entry.getKey().equalsIgnoreCase("qs"))
        	.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        return new MediaType(mediaType.getType(), mediaType.getSubtype(), parameters).toString();
    }
}
