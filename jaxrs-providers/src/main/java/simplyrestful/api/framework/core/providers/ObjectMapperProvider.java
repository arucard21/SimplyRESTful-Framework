package simplyrestful.api.framework.core.providers;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Provider
@Produces(MediaType.WILDCARD)
@Consumes(MediaType.WILDCARD)
public class ObjectMapperProvider implements ContextResolver<ObjectMapper>{
    private final ObjectMapper mapper;
    
    public ObjectMapperProvider() {
	this.mapper = createMediaTypeSpecificMapper();
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
	return mapper;
    }

    private ObjectMapper createMediaTypeSpecificMapper() {
	ObjectMapper jsonObjectMapper = new ObjectMapper();
	jsonObjectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	jsonObjectMapper.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);
	jsonObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	jsonObjectMapper.disable(SerializationFeature.WRITE_NULL_MAP_VALUES);
        return jsonObjectMapper;
    }
}
