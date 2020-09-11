package simplyrestful.api.framework.core.providers;

import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.openapitools.jackson.dataformat.hal.HALMapper;

@Named
@Provider
@Produces(HALMapperProvider.MEDIA_TYPE_APPLICATION_HAL_JSON)
@Consumes(HALMapperProvider.MEDIA_TYPE_APPLICATION_HAL_JSON)
public class HALMapperProvider implements ContextResolver<ObjectMapper>{
    public static final String MEDIA_TYPE_APPLICATION_HAL_JSON = "application/hal+json";
    private final ObjectMapper mapper;
    
    public HALMapperProvider() {
	this.mapper = createMediaTypeSpecificMapper();
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
	return mapper;
    }

    private ObjectMapper createMediaTypeSpecificMapper() {
	HALMapper halMapper = new HALMapper();
	halMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	halMapper.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);
	halMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	halMapper.disable(SerializationFeature.WRITE_NULL_MAP_VALUES);
        return halMapper;
    }
}
