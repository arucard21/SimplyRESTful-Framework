package example.springboot.resources;

import jakarta.ws.rs.core.MediaType;

import simplyrestful.api.framework.resources.APIResource;

public class ExampleResource extends APIResource {
	public static final String EXAMPLE_MEDIA_TYPE_JSON = "application/x.testresource-v1+json";
	public static final String EXAMPLE_PROFILE_STRING = "https://arucard21.github.io/SimplyRESTful-Framework/ExampleResource/v1";
	private String description;
	private ExampleEmbeddedResource embeddedResource;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ExampleEmbeddedResource getEmbeddedResource() {
		return embeddedResource;
	}

	public void setEmbeddedResource(ExampleEmbeddedResource embedded) {
		this.embeddedResource = embedded;
	}

	@Override
	public MediaType customJsonMediaType() {
		return MediaType.valueOf(EXAMPLE_MEDIA_TYPE_JSON);
	}
}
