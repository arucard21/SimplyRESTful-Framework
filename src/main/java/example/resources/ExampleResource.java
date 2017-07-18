package example.resources;

import java.net.URI;

import simplyrestful.api.framework.core.hal.HALResource;

public class ExampleResource extends HALResource{

	private String description;
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
		return URI.create("https://arucard21.github.io/SimplyRESTful/ExampleResource/v1");
	}

}
