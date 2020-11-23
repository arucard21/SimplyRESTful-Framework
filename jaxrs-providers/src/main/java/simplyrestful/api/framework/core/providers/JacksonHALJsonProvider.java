package simplyrestful.api.framework.core.providers;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.cfg.Annotations;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import io.openapitools.jackson.dataformat.hal.JacksonHALModule;
import jakarta.inject.Named;

@Named
@Provider
@Produces(JacksonHALJsonProvider.MEDIA_TYPE_APPLICATION_HAL_JSON)
@Consumes(JacksonHALJsonProvider.MEDIA_TYPE_APPLICATION_HAL_JSON)
public class JacksonHALJsonProvider extends JacksonJsonProvider{
    public static final String MEDIA_TYPE_APPLICATION_HAL_JSON = "application/hal+json";
    public static final JacksonHALModule JACKSON_HAL_MODULE = new JacksonHALModule();

    public JacksonHALJsonProvider(@Context Providers providers) {
	super(getObjectMapper(providers).registerModule(JACKSON_HAL_MODULE));
    }

    public JacksonHALJsonProvider(@Context Providers providers, Annotations[] annotationsToUse) {
	super(getObjectMapper(providers).registerModule(JACKSON_HAL_MODULE), annotationsToUse);
    }

    private static ObjectMapper getObjectMapper(Providers providers) {
	return providers.getContextResolver(ObjectMapper.class, MediaType.WILDCARD_TYPE).getContext(ObjectMapper.class).copy();
    }
}
