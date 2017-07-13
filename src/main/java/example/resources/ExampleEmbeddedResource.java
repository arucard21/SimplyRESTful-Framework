package example.resources;

import java.net.URI;

import simplyrestful.api.framework.core.hal.HalResource;

public class ExampleEmbeddedResource extends HalResource{

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public URI getProfile() {
		return URI.create("https://arucard21.github.io/SimplyRESTful/ExampleEmbeddedResource/v1");
	}

}
