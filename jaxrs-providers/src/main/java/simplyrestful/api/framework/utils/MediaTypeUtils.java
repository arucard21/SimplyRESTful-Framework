package simplyrestful.api.framework.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.MediaType;

public class MediaTypeUtils {
	public static final String ERROR_PREMATCHING_NOT_SUPPORTED = "This method must be called after JAX-RS matching is done. It cannot be called from a @PreMatching JAX-RS filter, use getAllProducibleMediaTypes() instead";
    public static final String MEDIA_TYPE_PARAMETER_QUALITY_SERVER = "qs";
    public static final String MEDIA_TYPE_PARAMETER_QUALITY_CLIENT = "q";

    public static final String MEDIA_TYPE_STRUCTURED_SYNTAX_SUFFIX_JSON = "+json";
    public static final String TYPE_APPLICATION = "application";

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

    /**
     * Return all producible media types for the entire API.
     *
     * This will check the same annotation inheritance as JAX-RS to accurately determine
     * which media types can be produced.
     *
     * @param configuration is the JAX-RS Configuration Context object that contains, among other things, which web
     * resource classes have been registered.
     * @return the list of media types that can be produced. May be empty but never null.
     * @throws IllegalStateException if multiple implemented interfaces provide @Produces annotations for this method.
     */
    public static List<MediaType> getAllProducibleMediaTypes(Configuration configuration) {
        List<Class<?>> webResourceClasses = Stream.concat(configuration.getClasses().stream(), configuration.getInstances().stream().map(Object::getClass))
                .filter(webResourceClass -> webResourceClass.getAnnotation(Path.class) != null)
                .collect(Collectors.toList());
        List<MediaType> allProducibleMediaTypes = new ArrayList<>();
        for (Class<?> webResourceClass : webResourceClasses) {
            for (Method jaxrsMethod : webResourceClass.getMethods()) {
                allProducibleMediaTypes.addAll(getProducibleMediaTypes(webResourceClass, jaxrsMethod));
            }
        }
        return allProducibleMediaTypes.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Return all producible media types for a specific request.
     *
     * This will check the same annotation inheritance as JAX-RS to accurately determine
     * which media types can be produced.
     *
     * @param resourceInfo is the JAX-RS ResourceInfo Context object that contains to which class and method the request was resolved.
     * @return the list of media types that can be produced. May be empty but never null.
     * @throws IllegalStateException if multiple implemented interfaces provide @Produces annotations for this method.
     */
    public static List<MediaType> getProducibleMediaTypes(ResourceInfo resourceInfo) {
        if(resourceInfo.getResourceMethod() == null && resourceInfo.getResourceClass() == null) {
            throw new IllegalStateException(ERROR_PREMATCHING_NOT_SUPPORTED);
        }
        return getProducibleMediaTypes(resourceInfo.getResourceClass(), resourceInfo.getResourceMethod());
    }

    /**
     * Return all producible media types for a specific method.
     *
     * This will check the same annotation inheritance as JAX-RS to accurately determine
     * which media types can be produced.
     *
     * @param resourceClass is the web resource class that defines the JAX-RS API.
     * @param method is the method for which we want to discover all producible media types.
     * @return the list of media types that can be produced. May be empty but never null.
     * @throws IllegalStateException if multiple implemented interfaces provide @Produces annotations for this method.
     */
    public static List<MediaType> getProducibleMediaTypes(Class<?> resourceClass, Method method) {
        List<MediaType> producibleMediaTypes = getDeclaredProducibleMediaTypesFromMethod(method);
        if(!producibleMediaTypes.isEmpty()) {
            return producibleMediaTypes;
        }
        producibleMediaTypes = getDeclaredProducibleMediaTypesFromSuperClass(method.getDeclaringClass().getSuperclass(), method);
        if(!producibleMediaTypes.isEmpty()) {
            return producibleMediaTypes;
        }
        producibleMediaTypes = getDeclaredProducibleMediaTypesFromInterfaces(method.getDeclaringClass().getInterfaces(), method);
        if(!producibleMediaTypes.isEmpty()) {
            return producibleMediaTypes;
        }
        producibleMediaTypes = getDeclaredProducibleMediaTypesFromClass(resourceClass);
        if(!producibleMediaTypes.isEmpty()) {
            return producibleMediaTypes;
        }
        return Collections.emptyList();
    }

    public static List<MediaType> getDeclaredProducibleMediaTypesFromMethod(Method method) {
        List<Produces> producesAnnotations = List.of(method.getDeclaredAnnotationsByType(Produces.class));
        return producesAnnotations.stream()
                .flatMap(produces -> Stream.of(produces.value()))
                .map(MediaType::valueOf)
                .collect(Collectors.toList());
    }

    public static List<MediaType> getDeclaredProducibleMediaTypesFromClass(Class<?> resourceClass) {
        List<Produces> producesAnnotations = List.of(resourceClass.getAnnotationsByType(Produces.class));
        return producesAnnotations.stream()
                .flatMap(produces -> Stream.of(produces.value()))
                .map(MediaType::valueOf)
                .collect(Collectors.toList());
    }

    public static List<MediaType> getDeclaredProducibleMediaTypesFromSuperClass(Class<?> superClass, Method method) {
        if(superClass == null) {
            return Collections.emptyList();
        }
        try {
            List<MediaType> superClassMediaTypes = getDeclaredProducibleMediaTypesFromMethod(
                    superClass.getDeclaredMethod(method.getName(), method.getParameterTypes()));
            if (!superClassMediaTypes.isEmpty()) {
                return superClassMediaTypes;
            }
        } catch (NoSuchMethodException e) {/* Super class did not contain the method so no further need to check it */ }
        return getDeclaredProducibleMediaTypesFromSuperClass(superClass.getSuperclass(), method);
    }

    public static List<MediaType> getDeclaredProducibleMediaTypesFromInterfaces(Class<?>[] interfaceClasses, Method method) {
        if(interfaceClasses == null || interfaceClasses.length == 0) {
            return Collections.emptyList();
        }
        List<List<MediaType>> producibleMediaTypesFromInterfaces = Stream.of(interfaceClasses)
                .map(interfaceClass -> getDeclaredProducibleMediaTypesFromInterface(interfaceClass, method))
                .filter(mediaTypeList -> !mediaTypeList.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        if (producibleMediaTypesFromInterfaces.isEmpty()) {
            return Collections.emptyList();
        }
        else if (producibleMediaTypesFromInterfaces.size() == 1) {
            return producibleMediaTypesFromInterfaces.get(0);
        }
        else {
            throw new IllegalStateException("Multiple interfaces provide JAX-RS annotations for this method");
        }
    }

    public static List<MediaType> getDeclaredProducibleMediaTypesFromInterface(Class<?> interfaceClass, Method method) {
        Optional<Method> interfaceMethodOptional = Stream.of(interfaceClass.getMethods())
                .filter(ifaceMethod -> ifaceMethod.getName().equals(method.getName())
                        && Arrays.equals(ifaceMethod.getParameterTypes(), method.getParameterTypes()))
                .findFirst();
        if(interfaceMethodOptional.isEmpty()) {
            return Collections.emptyList();
        }
        Method interfaceMethod = interfaceMethodOptional.get();
        List<MediaType> producibleMediaTypes = getDeclaredProducibleMediaTypesFromMethod(interfaceMethod);
        if(!producibleMediaTypes.isEmpty()) {
            return producibleMediaTypes;
        }
        return getDeclaredProducibleMediaTypesFromInterfaces(interfaceClass.getInterfaces(), method);
    }
}
