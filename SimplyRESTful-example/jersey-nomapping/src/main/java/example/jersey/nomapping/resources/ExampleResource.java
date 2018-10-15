package example.jersey.nomapping.resources;

import java.net.URI;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import io.openapitools.jackson.dataformat.hal.annotation.EmbeddedResource;
import simplyrestful.springdata.repository.nomapping.NoMappingHALResource;

@Entity
public class ExampleResource extends NoMappingHALResource{
	private String description;
	@OneToOne(cascade=CascadeType.ALL)
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
		return URI.create("https://arucard21.github.io/SimplyRESTful/ExampleResource/v1");
	}

}
