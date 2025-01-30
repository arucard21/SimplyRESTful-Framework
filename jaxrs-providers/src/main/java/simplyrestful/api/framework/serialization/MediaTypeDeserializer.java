package simplyrestful.api.framework.serialization;

import java.io.IOException;

import jakarta.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Deserialize a String to a MediaType object
 */
public class MediaTypeDeserializer extends StdDeserializer<MediaType> {
	private static final long serialVersionUID = -8747576302501492858L;

	/**
	 * Create a new instance of the deserializer.
	 */
	public MediaTypeDeserializer() {
		super(MediaType.class);
	}

	@Override
	public MediaType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
		return MediaType.valueOf(p.getValueAsString("*/*"));
	}
}
