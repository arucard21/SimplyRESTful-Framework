package simplyrestful.api.framework.core.swagger;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.openapitools.jackson.dataformat.hal.JacksonHALModule;
import io.swagger.v3.oas.integration.api.ObjectMapperProcessor;

public class HALMapperProcessor implements ObjectMapperProcessor {

    @Override
    public void processJsonObjectMapper(ObjectMapper mapper) {
	mapper.registerModule(new JacksonHALModule());
    }

    @Override
    public void processYamlObjectMapper(ObjectMapper mapper) {
	/* This is deprecated so this should not do anything*/
    }
}
