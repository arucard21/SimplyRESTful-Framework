package simplyrestful.api.framework.core.filters;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Named;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import simplyrestful.api.framework.core.MediaTypeUtils;

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
    private Configuration jaxrsConfiguration;
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
			    .anyMatch(acceptableMediaType -> MediaTypeUtils.withoutQualityParameters(acceptableMediaType).equals(MediaTypeUtils.withoutQualityParameters(customMediaType))))
		    .collect(Collectors.toList());
	    customJsonMediaTypes.removeAll(existingMediaTypes);
	    double quality = Double.valueOf(plainJson.get().getParameters().getOrDefault(MediaTypeUtils.MEDIA_TYPE_PARAMETER_QUALITY_CLIENT, "1.0"));
	    if(quality < 1.0) {
		customJsonMediaTypes.forEach(
			mediaType -> mediaType.getParameters().put(
				MediaTypeUtils.MEDIA_TYPE_PARAMETER_QUALITY_CLIENT,
				Double.toString(quality)));
	    }
	    modifiedAcceptableMediaTypes = Stream.concat(
		    modifiedAcceptableMediaTypes.stream(),
		    customJsonMediaTypes.stream().map(MediaType::toString))
		    .collect(Collectors.toList());
	}
	requestContext.getHeaders().put(HttpHeaders.ACCEPT, modifiedAcceptableMediaTypes);
    }

    private List<MediaType> getCustomJsonMediaTypes() {
	return MediaTypeUtils.getProducibleMediaTypes(jaxrsConfiguration).stream()
		.filter(producibleMediaType -> producibleMediaType.getSubtype().endsWith(MediaTypeUtils.MEDIA_TYPE_STRUCTURED_SYNTAX_SUFFIX_JSON))
		.collect(Collectors.toList());
    }
}
