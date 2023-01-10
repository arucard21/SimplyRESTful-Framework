package simplyrestful.api.framework.serialization;

import java.io.IOException;

import jakarta.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class MediaTypeSerializer extends StdSerializer<MediaType> {
	private static final long serialVersionUID = -8747576302501492858L;

	public MediaTypeSerializer() {
		super(MediaType.class);
	}

	@Override
	public void serialize(MediaType value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeString(value.toString());
	}

}
