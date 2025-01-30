package simplyrestful.api.framework.serialization;

import jakarta.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * A Jackson module for serializing MediaType to String, and vice versa.
 */
public class MediaTypeModule extends SimpleModule {
	private static final long serialVersionUID = 1326213034034902320L;

	/**
	 * Create a new instance of this MediaType Jackson module.
	 */
	public MediaTypeModule() {
		addSerializer(new MediaTypeSerializer());
		addDeserializer(MediaType.class, new MediaTypeDeserializer());
	}
}
