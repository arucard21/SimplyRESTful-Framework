package example.resources;

import java.net.URI;

import simplyrestful.api.framework.core.hal.HalResource;

public class ExampleResource extends HalResource{

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
