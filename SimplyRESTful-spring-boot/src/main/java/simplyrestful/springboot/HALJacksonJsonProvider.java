package simplyrestful.springboot;

import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import io.openapitools.jackson.dataformat.hal.HALMapper;
import simplyrestful.api.framework.core.MediaType;

@Named
@Provider
@Consumes(MediaType.APPLICATION_HAL_JSON)
@Produces(MediaType.APPLICATION_HAL_JSON)
public class HALJacksonJsonProvider extends JacksonJsonProvider{
	public HALJacksonJsonProvider() {
		super(new HALMapper());
	}
}