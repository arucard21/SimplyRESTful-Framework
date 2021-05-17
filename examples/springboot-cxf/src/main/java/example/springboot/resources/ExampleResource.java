package example.springboot.resources;

import java.net.URI;

import javax.ws.rs.core.MediaType;

import io.openapitools.jackson.dataformat.hal.annotation.EmbeddedResource;
import simplyrestful.api.framework.resources.HALResource;

public class ExampleResource extends HALResource {
    public static final String EXAMPLE_MEDIA_TYPE_JSON = "application/x.testresource-v1+json";
    public static final String EXAMPLE_PROFILE_STRING = "https://arucard21.github.io/SimplyRESTful-Framework/ExampleResource/v1";
    private String description;
    @EmbeddedResource
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
    public URI getProfile() {
	return URI.create(EXAMPLE_PROFILE_STRING);
    }

    @Override
    public MediaType getCustomJsonMediaType() {
	return MediaType.valueOf(EXAMPLE_MEDIA_TYPE_JSON);
    }
}
