package simplyrestful.api.framework.core.providers;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

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

    public JacksonHALJsonProvider(@Context ObjectMapper mapper) {
	super(mapper.registerModule(JACKSON_HAL_MODULE));
    }

    public JacksonHALJsonProvider(@Context ObjectMapper mapper, Annotations[] annotationsToUse) {
	super(mapper.registerModule(JACKSON_HAL_MODULE), annotationsToUse);
    }
}
