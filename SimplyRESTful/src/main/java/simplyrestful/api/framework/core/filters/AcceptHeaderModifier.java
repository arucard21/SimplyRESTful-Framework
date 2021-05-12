package simplyrestful.api.framework.core.filters;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Named;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

@Named
@PreMatching
public class AcceptHeaderModifier implements ContainerRequestFilter {
    private static final String MEDIA_TYPE_STRUCTURED_SYNTAX_SUFFIX_JSON = "+json";
    private static final String MEDIA_TYPE_PARAMETER_QUALITY_SERVER = "qs";
    private static final String MEDIA_TYPE_PARAMETER_QUALITY_CLIENT = "q";
    @Context
    private Configuration jaxrsConfiguration;
    @Context
    private HttpHeaders httpHeaders;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
	List<MediaType> acceptableMediaTypes = httpHeaders.getAcceptableMediaTypes();
	List<MediaType> producibleMediaTypes = getProducibleMediaTypes();
	List<String> modifiedAcceptableMediaTypes = acceptableMediaTypes.stream().map(MediaType::toString).collect(Collectors.toList());

	if(acceptableMediaTypes.stream().anyMatch(acceptableMediaType -> acceptableMediaType.equals(MediaType.APPLICATION_JSON_TYPE))) {
	    List<String> customJsonMediaTypes = getCustomJsonMediaTypes(producibleMediaTypes);
	    modifiedAcceptableMediaTypes = Stream.concat(
		    modifiedAcceptableMediaTypes.stream(),
		    customJsonMediaTypes.stream()).collect(Collectors.toList());
	}

	List<String> unacceptableMediaTypes = getUnacceptableMediaTypesWithQualitySetToZero(acceptableMediaTypes, producibleMediaTypes);
	modifiedAcceptableMediaTypes = Stream.concat(
		modifiedAcceptableMediaTypes.stream(),
		unacceptableMediaTypes.stream())
		.collect(Collectors.toList());
	requestContext.getHeaders().put(HttpHeaders.ACCEPT, modifiedAcceptableMediaTypes);
    }

    private List<String> getCustomJsonMediaTypes(List<MediaType> producibleMediaTypes) {
	return producibleMediaTypes.stream()
		.filter(producibleMediaType -> producibleMediaType.getSubtype().endsWith(MEDIA_TYPE_STRUCTURED_SYNTAX_SUFFIX_JSON))
		.map(MediaType::toString)
		.collect(Collectors.toList());
    }

    private List<String> getUnacceptableMediaTypesWithQualitySetToZero(List<MediaType> acceptableMediaTypes, List<MediaType> producibleMediaTypes) {
	return acceptableMediaTypes.stream()
		.flatMap(acceptableMediaType -> producibleMediaTypes.stream()
			.filter(producibleMediaType -> !producibleMediaType.equals(MediaType.WILDCARD_TYPE))
			.filter(producibleMediaType -> !producibleMediaType.getSubtype().equals(MediaType.MEDIA_TYPE_WILDCARD))
			.filter(producibleMediaType -> withoutQualityParameters(producibleMediaType).getParameters().size() > 0)
			.filter(producibleMediaType -> acceptableMediaType.isCompatible(producibleMediaType))
			.filter(producibleMediaType -> !withoutQualityParameters(acceptableMediaType).equals(withoutQualityParameters(producibleMediaType)))
			.map(this::mediaTypeWithQualityZero))
		.map(MediaType::toString)
		.collect(Collectors.toList());
    }

    private MediaType mediaTypeWithQualityZero(MediaType producibleMediaType) {
	Map<String, String> newParameters = new HashMap<>(withoutQualityParameters(producibleMediaType).getParameters());
	newParameters.put(MEDIA_TYPE_PARAMETER_QUALITY_CLIENT, Double.toString(0));
	return new MediaType(producibleMediaType.getType(), producibleMediaType.getSubtype(), newParameters);
    }

    private List<MediaType> getProducibleMediaTypes() {
	return getResourceClasses().stream()
		.<AnnotatedElement>flatMap(resourceClass -> Stream.concat(Stream.of(resourceClass), Stream.of(resourceClass.getMethods())))
		.flatMap(resourceClass -> Stream.of(resourceClass.getAnnotationsByType(Produces.class)))
		.flatMap(produces -> Stream.of(produces.value()))
		.map(MediaType::valueOf)
		.collect(Collectors.toList());
    }

    private Set<Class<?>> getResourceClasses() {
	return Stream.concat(jaxrsConfiguration.getClasses().stream(), jaxrsConfiguration.getInstances().stream().map(Object::getClass))
		.filter(configuredClass -> configuredClass.getAnnotationsByType(Path.class).length > 0)
		.collect(Collectors.toSet());
    }

    private MediaType withoutQualityParameters(MediaType selectedMediaType) {
        Map<String, String> parametersWithoutQAndQS = selectedMediaType.getParameters().entrySet().stream()
        	.filter(entry -> !entry.getKey().equals(MEDIA_TYPE_PARAMETER_QUALITY_CLIENT))
        	.filter(entry -> !entry.getKey().equals(MEDIA_TYPE_PARAMETER_QUALITY_SERVER))
        	.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        return new MediaType(selectedMediaType.getType(), selectedMediaType.getSubtype(), parametersWithoutQAndQS);
    }
}
