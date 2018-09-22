package example.jetty.resources;

import java.net.URI;

import simplyrestful.api.framework.core.hal.HALResource;

public class ExampleEmbeddedResource extends HALResource{

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
