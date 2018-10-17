package example.jersey.nomapping.resources;

import java.net.URI;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import simplyrestful.springdata.repository.nomapping.NoMappingHALResource;

@Entity
public class ExampleResource extends NoMappingHALResource{
	public static final String EXAMPLE_PROFILE_STRING = "https://arucard21.github.io/SimplyRESTful-Framework/ExampleResource/v1";

	private String description;
	@OneToOne(cascade=CascadeType.ALL)
	private ExampleComplexAttribute complexAttribute;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ExampleComplexAttribute getComplexAttribute(){
		return complexAttribute;
	}

	public void setComplexAttribute(ExampleComplexAttribute complexAttribute) {
		this.complexAttribute = complexAttribute;
	}

	@Override
	public URI getProfile() {
		return URI.create(EXAMPLE_PROFILE_STRING);
	}

}
