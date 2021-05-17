package simplyrestful.api.framework.core;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;

public class MediaTypeUtils {
    private static final int MEDIA_TYPE_SPECIFICITY_WILDCARD_TYPE = 0; // Example: */*
    private static final int MEDIA_TYPE_SPECIFICITY_WILDCARD_SUBTYPE = 1; // Example: application/*
    private static final int MEDIA_TYPE_SPECIFICITY_CONCRETE_TYPE_WITHOUT_PARAMETERS = 2; // Example: application/json
    private static final int MEDIA_TYPE_SPECIFICITY_CONCRETE_TYPE_WITH_PARAMETERS = 3; // Example: application/hal+json;profile="https://arucard21.github.io/SimplyRESTful-Framework/HALCollection/v2"
    public static final String MEDIA_TYPE_PARAMETER_QUALITY_SERVER = "qs";
    public static final String MEDIA_TYPE_PARAMETER_QUALITY_CLIENT = "q";

    public static final String MEDIA_TYPE_STRUCTURED_SYNTAX_SUFFIX_JSON = "+json";
    public static final String APPLICATION_HAL_JSON = "application/hal+json";
    public static final String TYPE_APPLICATION = "application";
    public static final String APPLICATION_HAL_JSON_SUBTYPE = "hal+json";
    public static final String APPLICATION_HAL_JSON_PARAMETER_PROFILE = "profile";
    public static final MediaType APPLICATION_HAL_JSON_TYPE = new MediaType(TYPE_APPLICATION, APPLICATION_HAL_JSON_SUBTYPE);

    /**
     *
     * Select the most suitable media type according to JAX-RS specification.
     *
     * This custom method is provided since some JAX-RS frameworks do not consider
     * media type parameters as a more specific media type (at least, when using
     * the {@link javax.ws.rs.core.Request}.selectVariant() method). This method provides a correct
     * implementation for this, limited to only the media type.
     *
     * This framework requires the media type parameters to be considered since that
     * is where the profile is found. This profile is what uniquely typifies each
     * API resource (representing a single version of a specific API resource).
     *
     * This method does not change the Content-Type HTTP header that the JAX-RS
     * framework returns. The JAX-RS framework must already set this correctly,
     * based on the @{@link Produces} or @(@link Consumes} annotations. Or it must be
     * adjusted in some other way, e.g. using a {@link ContainerResponseFilter}.
     *
     * @param producibleMediaTypes is the list of media types that the server can produce.
     * @param acceptableMediaTypes is the list of media types that the client prefers to accept.
     * @return the most suitable media type, considering the client's preferences and
     * the server's capabilities.
     */
    public static MediaType selectMediaType(List<MediaType> producibleMediaTypes, List<MediaType> acceptableMediaTypes) {
        List<MediaType> selectedMediaTypes = new ArrayList<>();
        for(MediaType acceptableMediaType : acceptableMediaTypes) {
            for(MediaType producibleMediaType : producibleMediaTypes) {
        	if(acceptableMediaType.isCompatible(producibleMediaType)) {
        	    selectedMediaTypes.add(getMostSpecificMediaType(acceptableMediaType, producibleMediaType));
        	}
            }
        }
        if(selectedMediaTypes.isEmpty()) {
            throw new NotAcceptableException();
        }
        selectedMediaTypes.sort(
        	Comparator.comparingInt(MediaTypeUtils::determineMediaTypeSpecificity)
        	.thenComparingDouble(MediaTypeUtils::getQParameter)
        	.thenComparingDouble(MediaTypeUtils::getQSParameter)
        	.reversed());
        for(MediaType selectedMediaType : selectedMediaTypes) {
            if(isConcreteMediaType(selectedMediaType)) {
        	return withoutQualityParameters(selectedMediaType);
            }
            if((selectedMediaType.getType().equals(MediaType.MEDIA_TYPE_WILDCARD) ||
        	    selectedMediaType.getType().equals(MediaType.APPLICATION_OCTET_STREAM_TYPE.getType())) &&
        	    selectedMediaType.getSubtype().equals(MediaType.MEDIA_TYPE_WILDCARD)) {
        	return MediaType.APPLICATION_OCTET_STREAM_TYPE;
            }
        }
        throw new NotAcceptableException();
    }

    public static MediaType addQualityParameters(MediaType mediaType, double q, double qs) {
        return addQSParameter(addQParameter(mediaType, q), qs);
    }

    public static MediaType addQSParameter(MediaType mediaType, double qs) {
        Map<String, String> newParameters = new HashMap<>(mediaType.getParameters());
        newParameters.put(MEDIA_TYPE_PARAMETER_QUALITY_SERVER, Double.toString(qs));
        return new MediaType(mediaType.getType(), mediaType.getSubtype(), newParameters);
    }

    public static MediaType addQParameter(MediaType mediaType, double q) {
        Map<String, String> newParameters = new HashMap<>(mediaType.getParameters());
        newParameters.put(MEDIA_TYPE_PARAMETER_QUALITY_CLIENT, Double.toString(q));
        return new MediaType(mediaType.getType(), mediaType.getSubtype(), newParameters);
    }

    public static Double getQSParameter(MediaType mediaType) {
        String qsParameter = mediaType.getParameters().get(MEDIA_TYPE_PARAMETER_QUALITY_SERVER);
        return qsParameter == null ? 1.0 : Double.valueOf(qsParameter);
    }

    public static Double getQParameter(MediaType mediaType) {
        String qParameter = mediaType.getParameters().get(MEDIA_TYPE_PARAMETER_QUALITY_CLIENT);
        return qParameter == null ? 1.0 : Double.valueOf(qParameter);
    }

    public static MediaType withoutQualityParameters(MediaType selectedMediaType) {
        Map<String, String> parametersWithoutQAndQS = selectedMediaType.getParameters().entrySet().stream()
        	.filter(entry -> !entry.getKey().equals(MEDIA_TYPE_PARAMETER_QUALITY_CLIENT))
        	.filter(entry -> !entry.getKey().equals(MEDIA_TYPE_PARAMETER_QUALITY_SERVER))
        	.collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        return new MediaType(selectedMediaType.getType(), selectedMediaType.getSubtype(), parametersWithoutQAndQS);
    }

    private static boolean isConcreteMediaType(MediaType selectedMediaType) {
        if(!selectedMediaType.getType().equals(MediaType.MEDIA_TYPE_WILDCARD) &&
        	!selectedMediaType.getSubtype().equals(MediaType.MEDIA_TYPE_WILDCARD)) {
            return true;
        }
        return false;
    }

    private static MediaType getMostSpecificMediaType(MediaType acceptableMediaType, MediaType producibleMediaType) {
        int clientSpecificity = determineMediaTypeSpecificity(acceptableMediaType);
        int serverSpecificity = determineMediaTypeSpecificity(producibleMediaType);
        if(clientSpecificity == MEDIA_TYPE_SPECIFICITY_CONCRETE_TYPE_WITH_PARAMETERS && clientSpecificity == serverSpecificity) {
            Map<String, String> serverParameters = producibleMediaType.getParameters();
            for(Entry<String, String> clientParameter : acceptableMediaType.getParameters().entrySet()) {
        	String serverParameter = serverParameters.get(clientParameter.getKey());
        	if(serverParameter == null || !clientParameter.getValue().equals(serverParameter)) {
        	    /*
        	     * The client has this parameter in the media type while the server does not or
        	     * the client has a different value for this parameter than the server.
        	     *
        	     * This makes the client media type more specific than the server media type.
        	     */
        	    clientSpecificity++;
        	}
            }
        }
        return clientSpecificity > serverSpecificity ?
        	addQualityParameters(
        		acceptableMediaType,
        		getQParameter(acceptableMediaType),
        		getQSParameter(producibleMediaType)):
        	addQualityParameters(
        		producibleMediaType,
        		getQParameter(acceptableMediaType),
        		getQSParameter(producibleMediaType));
    }

    /**
     * Determines how specific the media type is.
     * A higher number denotes a more specific media type.
     *
     * @param mediaType is the media type for which to determine specificity
     * @return the specificity of the media type (higher is more specific)
     */
    private static int determineMediaTypeSpecificity(MediaType mediaType) {
        String type = mediaType.getType();
        String subType = mediaType.getSubtype();
        Map<String, String> parameters = mediaType.getParameters();
        if(type.equals(MediaType.MEDIA_TYPE_WILDCARD)) {
            return MEDIA_TYPE_SPECIFICITY_WILDCARD_TYPE;
        }
        if(subType.equals(MediaType.MEDIA_TYPE_WILDCARD)) {
            return MEDIA_TYPE_SPECIFICITY_WILDCARD_SUBTYPE;
        }
        if(parameters.isEmpty()) {
            return MEDIA_TYPE_SPECIFICITY_CONCRETE_TYPE_WITHOUT_PARAMETERS;
        }
        return MEDIA_TYPE_SPECIFICITY_CONCRETE_TYPE_WITH_PARAMETERS;
    }

    public static List<MediaType> getProducibleMediaTypes(Configuration configuration) {
	return getResourceClasses(configuration).stream()
		.<AnnotatedElement>flatMap(resourceClass -> Stream.concat(Stream.of(resourceClass), Stream.of(resourceClass.getMethods())))
		.flatMap(resourceClass -> Stream.of(resourceClass.getAnnotationsByType(Produces.class)))
		.flatMap(produces -> Stream.of(produces.value()))
		.map(MediaType::valueOf)
		.collect(Collectors.toList());
    }

    public static Set<Class<?>> getResourceClasses(Configuration configuration) {
	return Stream.concat(configuration.getClasses().stream(), configuration.getInstances().stream().map(Object::getClass))
		.filter(configuredClass -> configuredClass.getAnnotationsByType(Path.class).length > 0)
		.collect(Collectors.toSet());
    }
}
