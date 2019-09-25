package example.jetty.resources;

import java.net.URI;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.openapitools.jackson.dataformat.hal.annotation.EmbeddedResource;
import simplyrestful.api.framework.resources.HALResource;

public class ExampleResource extends HALResource{
	public static final String EXAMPLE_PROFILE_STRING = "https://arucard21.github.io/SimplyRESTful-Framework/ExampleResource/v1";
	@JsonIgnore
	@NotNull
	private UUID uuid;
	private String description;
	@EmbeddedResource
	private ExampleEmbeddedResource embeddedResource;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ExampleEmbeddedResource getEmbeddedResource(){
		return embeddedResource;
	}

	public void setEmbeddedResource(ExampleEmbeddedResource embedded) {
		this.embeddedResource = embedded;
	}

	@Override
	public URI getProfile() {
		return URI.create(EXAMPLE_PROFILE_STRING);
	}

	public UUID getUUID() {
		return uuid;
	}

	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}

}
