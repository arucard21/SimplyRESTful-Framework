package simplyrestful.api.framework.serialization;

import jakarta.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.module.SimpleModule;

public class MediaTypeModule extends SimpleModule {
	private static final long serialVersionUID = 1326213034034902320L;

	public MediaTypeModule() {
		addSerializer(new MediaTypeSerializer());
		addDeserializer(MediaType.class, new MediaTypeDeserializer());
	}
}
