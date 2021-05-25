package simplyrestful.api.framework.filters;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Named;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import simplyrestful.api.framework.MediaTypeUtils;

/**
 *
 * Modify the HTTP Accept header to ensure that if "application/json" is requested, any media type with
 * the "+json" structured suffix would also be considered acceptable.
 *
 */
@Named
@PreMatching
public class AcceptHeaderModifier implements ContainerRequestFilter {
    @Context
    private ResourceInfo resourceInfo;
    @Context
    private HttpHeaders httpHeaders;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
	List<MediaType> acceptableMediaTypes = httpHeaders.getAcceptableMediaTypes();
	List<String> modifiedAcceptableMediaTypes = acceptableMediaTypes.stream().map(MediaType::toString).collect(Collectors.toList());

	Optional<MediaType> plainJson = acceptableMediaTypes.stream()
		.filter(acceptableMediaType -> MediaTypeUtils.withoutQualityParameters(acceptableMediaType).equals(MediaType.APPLICATION_JSON_TYPE))
		.findFirst();
	if(plainJson.isPresent()) {
	    List<MediaType> customJsonMediaTypes = getCustomJsonMediaTypes();
	    List<MediaType> existingMediaTypes = customJsonMediaTypes.stream()
		    .filter(customMediaType -> acceptableMediaTypes.stream()
			    .anyMatch(acceptableMediaType -> MediaTypeUtils.withoutQualityParameters(acceptableMediaType).equals(customMediaType)))
		    .collect(Collectors.toList());
	    customJsonMediaTypes.removeAll(existingMediaTypes);
	    double quality = Double.valueOf(plainJson.get().getParameters().getOrDefault(MediaTypeUtils.MEDIA_TYPE_PARAMETER_QUALITY_CLIENT, "1.0"));
	    List<String> customJsonMediaTypesWithQ = customJsonMediaTypes.stream()
		    .map(mediaType -> quality < 1.0 ? MediaTypeUtils.addQParameter(mediaType, quality) : mediaType)
		    .map(MediaType::toString)
		    .collect(Collectors.toList());
	    modifiedAcceptableMediaTypes = Stream.concat(
		    modifiedAcceptableMediaTypes.stream(),
		    customJsonMediaTypesWithQ.stream())
		    .collect(Collectors.toList());
	}
	requestContext.getHeaders().put(HttpHeaders.ACCEPT, modifiedAcceptableMediaTypes);
    }

    /**
     * Detect the media types that can be produced and return them without their server-side quality parameters.
     * This will only return media types of the same level of specificity as plain JSON, which means that any media
     * types with non-quality parameters will be removed as that makes them more specific than plain JSON.
     *
     * @return the list of similarly-specific plain JSON media types that can be produced.
     */
    private List<MediaType> getCustomJsonMediaTypes() {
        return MediaTypeUtils.getProducibleMediaTypes(resourceInfo).stream()
    	        .filter(producibleMediaType -> producibleMediaType.getSubtype().endsWith(MediaTypeUtils.MEDIA_TYPE_STRUCTURED_SYNTAX_SUFFIX_JSON))
    	        .map(MediaTypeUtils::withoutQualityParameters)
    	        .filter(this::withPlainJsonLevelOfSpecificity)
    	        .collect(Collectors.toList());
    }

    private boolean withPlainJsonLevelOfSpecificity(MediaType mediaType) {
        return mediaType.getParameters().isEmpty();
    }
}
