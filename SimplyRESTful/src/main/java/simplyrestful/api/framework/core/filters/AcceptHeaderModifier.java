package simplyrestful.api.framework.core.filters;

import java.io.IOException;
import java.util.List;
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

	if(acceptableMediaTypes.stream().anyMatch(
		acceptableMediaType -> MediaTypeUtils.withoutQualityParameters(acceptableMediaType).equals(MediaType.APPLICATION_JSON_TYPE))) {
	    List<String> customJsonMediaTypes = getCustomJsonMediaTypes();
	    modifiedAcceptableMediaTypes = Stream.concat(
		    modifiedAcceptableMediaTypes.stream(),
		    customJsonMediaTypes.stream()).collect(Collectors.toList());
	}
	requestContext.getHeaders().put(HttpHeaders.ACCEPT, modifiedAcceptableMediaTypes);
    }

    private List<String> getCustomJsonMediaTypes() {
	return MediaTypeUtils.getProducibleMediaTypes(jaxrsConfiguration).stream()
		.filter(producibleMediaType -> producibleMediaType.getSubtype().endsWith(MediaTypeUtils.MEDIA_TYPE_STRUCTURED_SYNTAX_SUFFIX_JSON))
		.map(MediaType::toString)
		.collect(Collectors.toList());
    }
}
