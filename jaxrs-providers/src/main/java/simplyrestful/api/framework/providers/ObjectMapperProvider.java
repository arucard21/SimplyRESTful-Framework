package simplyrestful.api.framework.providers;

import jakarta.inject.Named;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Named
@Provider
@Produces(MediaType.WILDCARD)
@Consumes(MediaType.WILDCARD)
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {
    private final ObjectMapper mapper;

    public ObjectMapperProvider() {
        this.mapper = createMapper();
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }

    private ObjectMapper createMapper() {
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.findAndRegisterModules();
        jsonObjectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        jsonObjectMapper.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);
        jsonObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        jsonObjectMapper.setSerializationInclusion(Include.NON_EMPTY);
        return jsonObjectMapper;
    }
}
