package simplyrestful.api.framework.core.providers;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import jakarta.inject.Named;

@Named
@Provider
@Produces(JacksonHALJsonProvider.MEDIA_TYPE_APPLICATION_HAL_JSON)
@Consumes(JacksonHALJsonProvider.MEDIA_TYPE_APPLICATION_HAL_JSON)
public class JacksonHALJsonProvider extends JacksonJsonProvider{
    public static final String MEDIA_TYPE_APPLICATION_HAL_JSON = "application/hal+json";
}
