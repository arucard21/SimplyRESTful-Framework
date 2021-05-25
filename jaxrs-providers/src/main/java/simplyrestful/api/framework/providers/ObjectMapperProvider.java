package simplyrestful.api.framework.providers;

import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Named
@Provider
@Produces(MediaType.WILDCARD)
@Consumes(MediaType.WILDCARD)
public class ObjectMapperProvider implements ContextResolver<ObjectMapper>{
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
	jsonObjectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	jsonObjectMapper.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);
	jsonObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	jsonObjectMapper.setSerializationInclusion(Include.NON_EMPTY);
        return jsonObjectMapper;
    }
}
