package simplyrestful.api.framework.core.swagger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;

import io.swagger.v3.jaxrs2.Reader;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;

public class ReaderUsingProducesWithoutQsParameter extends Reader{

    @Override
    public Operation parseMethod(Method method, List<Parameter> globalParameters, Produces methodProduces,
	    Produces classProduces, Consumes methodConsumes, Consumes classConsumes,
	    List<SecurityRequirement> classSecurityRequirements, Optional<ExternalDocumentation> classExternalDocs,
	    Set<String> classTags, List<Server> classServers, boolean isSubresource, RequestBody parentRequestBody,
	    ApiResponses parentResponses, JsonView jsonViewAnnotation, ApiResponse[] classResponses) {
	return super.parseMethod(method, globalParameters, withoutQsParameter(methodProduces), withoutQsParameter(classProduces), methodConsumes, classConsumes,
		classSecurityRequirements, classExternalDocs, classTags, classServers, isSubresource, parentRequestBody,
		parentResponses, jsonViewAnnotation, classResponses);
    }

    @Override
    protected Operation parseMethod(Class<?> cls, Method method, List<Parameter> globalParameters,
	    Produces methodProduces, Produces classProduces, Consumes methodConsumes, Consumes classConsumes,
	    List<SecurityRequirement> classSecurityRequirements, Optional<ExternalDocumentation> classExternalDocs,
	    Set<String> classTags, List<Server> classServers, boolean isSubresource, RequestBody parentRequestBody,
	    ApiResponses parentResponses, JsonView jsonViewAnnotation, ApiResponse[] classResponses,
	    AnnotatedMethod annotatedMethod) {
	return super.parseMethod(cls, method, globalParameters, withoutQsParameter(methodProduces), withoutQsParameter(classProduces), methodConsumes, classConsumes,
		classSecurityRequirements, classExternalDocs, classTags, classServers, isSubresource, parentRequestBody,
		parentResponses, jsonViewAnnotation, classResponses, annotatedMethod);
    }

    @Override
    public Operation parseMethod(Method method, List<Parameter> globalParameters, Produces methodProduces,
	    Produces classProduces, Consumes methodConsumes, Consumes classConsumes,
	    List<SecurityRequirement> classSecurityRequirements, Optional<ExternalDocumentation> classExternalDocs,
	    Set<String> classTags, List<Server> classServers, boolean isSubresource, RequestBody parentRequestBody,
	    ApiResponses parentResponses, JsonView jsonViewAnnotation, ApiResponse[] classResponses,
	    AnnotatedMethod annotatedMethod) {
	return super.parseMethod(method, globalParameters, withoutQsParameter(methodProduces), withoutQsParameter(classProduces), methodConsumes, classConsumes,
		classSecurityRequirements, classExternalDocs, classTags, classServers, isSubresource, parentRequestBody,
		parentResponses, jsonViewAnnotation, classResponses, annotatedMethod);
    }

    private Produces withoutQsParameter(Produces produces) {
	if(produces == null) {
	    return produces;
	}
	String[] value = Stream.of(produces.value())
		.filter(Objects::nonNull)
		.flatMap(entry -> Stream.of(entry.split(",")))
		.map(String::trim)
		.filter(singleEntry -> !singleEntry.isBlank())
		.map(producesString -> MediaType.valueOf(producesString))
		.map(this::withoutQsParameter)
		.map(MediaType::toString)
		.toArray(String[]::new);
	return new Produces() {
	    @Override
	    public Class<? extends Annotation> annotationType() {
		return produces.annotationType();
	    }

	    @Override
	    public String[] value() {
		return value;
	    }
	};
    }

    private MediaType withoutQsParameter(MediaType mediaType) {
	Map<String, String> parameters = mediaType.getParameters().entrySet().stream()
		.filter(entry -> !entry.getKey().equalsIgnoreCase("qs"))
		.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	return new MediaType(mediaType.getType(), mediaType.getSubtype(), parameters);
    }
}
