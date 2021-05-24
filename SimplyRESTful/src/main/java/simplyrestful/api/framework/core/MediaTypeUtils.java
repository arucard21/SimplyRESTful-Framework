package simplyrestful.api.framework.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.HttpHeaders;
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
    * @param resourceInfo is the JAX-RS context object, used to detect media types producible by the server.
    * @param httpHeaders is the JAX-RS context object, used to detect media types considered acceptable by the client.
    * @return the most suitable media type, considering the client's preferences and the server's capabilities.
    */
    public static MediaType selectMediaType(ResourceInfo resourceInfo, HttpHeaders httpHeaders) {
        return selectMediaType(getProducibleMediaTypes(resourceInfo), httpHeaders.getAcceptableMediaTypes());
    }

    private static MediaType selectMediaType(List<MediaType> producibleMediaTypes, List<MediaType> acceptableMediaTypes) {
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

    /**
     * Return all producible media types for a request.
     *
     * This will check the same annotation inheritance as JAX-RS to accurately determine
     * which media types can be produced.
     *
     * @param resourceInfo is the JAX-RS ResourceInfo Context object that contains to which class and method the request was resolved.
     * @return the list of media types that can be produced. May be empty but never null.
     * @throws IllegalStateException if multiple implemented interfaces provide @Produces annotations for this method.
     */
    public static List<MediaType> getProducibleMediaTypes(ResourceInfo resourceInfo) {
        List<MediaType> producibleMediaTypes = getProducibleMediaTypesFromMethod(resourceInfo.getResourceMethod());
        if(!producibleMediaTypes.isEmpty()) {
            return producibleMediaTypes;
        }
        producibleMediaTypes = getProducibleMediaTypesFromSuperClassOrInterfaces(resourceInfo.getResourceMethod());
        if(!producibleMediaTypes.isEmpty()) {
            return producibleMediaTypes;
        }
        producibleMediaTypes = getProducibleMediaTypesFromClass(resourceInfo.getResourceClass());
        if(!producibleMediaTypes.isEmpty()) {
            return producibleMediaTypes;
        }
        return Collections.emptyList();
    }

    private static List<MediaType> getProducibleMediaTypesFromMethod(Method method) {
        List<Produces> producesAnnotations = List.of(method.getDeclaredAnnotationsByType(Produces.class));
        return producesAnnotations.stream()
                .flatMap(produces -> Stream.of(produces.value()))
                .map(MediaType::valueOf)
                .collect(Collectors.toList());
    }

    private static List<MediaType> getProducibleMediaTypesFromClass(Class<?> resourceClass) {
        List<Produces> producesAnnotations = List.of(resourceClass.getAnnotationsByType(Produces.class));
        return producesAnnotations.stream()
                .flatMap(produces -> Stream.of(produces.value()))
                .map(MediaType::valueOf)
                .collect(Collectors.toList());
    }

    private static List<MediaType> getProducibleMediaTypesFromSuperClassOrInterfaces(Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        Class<?> superClass = declaringClass.getSuperclass();
        if (superClass != null) {
            try {
                List<MediaType> superClassMediaTypes = getProducibleMediaTypesFromMethod(
                        superClass.getDeclaredMethod(method.getName(), method.getParameterTypes()));
                if(!superClassMediaTypes.isEmpty()) {
                    return superClassMediaTypes;
                }
            } catch (NoSuchMethodException e) { /* Super class did not contain the method so no further need to check it */ }
        }
      List<Map<String,List<MediaType>>> producibleMediaTypesPerInterface = Stream.of(declaringClass.getInterfaces())
                .map(iface -> Stream.of(iface.getMethods())
                        .filter(ifaceMethod -> ifaceMethod.getName().equals(method.getName()) && Arrays.equals(ifaceMethod.getParameterTypes(), method.getParameterTypes()))
                        .findFirst())
                .filter(ifaceMethodOptional -> ifaceMethodOptional.isPresent())
                .map(ifaceMethod -> ifaceMethod.get())
                .map(ifaceMethod -> Collections.singletonMap(ifaceMethod.getDeclaringClass().getName(), getProducibleMediaTypesFromMethod(ifaceMethod)))
                .distinct()
                .collect(Collectors.toList());
        if(producibleMediaTypesPerInterface.size() == 1) {
            return producibleMediaTypesPerInterface.get(0).values().stream().findFirst().orElseThrow();
        }
        else {
            if(producibleMediaTypesPerInterface.isEmpty()) {
                return Collections.emptyList();
            }
            else {
                throw new IllegalStateException("Multiple interfaces provide JAX-RS annotations for this method");
            }
        }
    }
}
