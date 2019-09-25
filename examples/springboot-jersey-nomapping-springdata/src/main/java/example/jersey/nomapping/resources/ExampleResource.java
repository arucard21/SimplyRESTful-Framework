package example.jersey.nomapping.resources;

import java.net.URI;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import simplyrestful.api.framework.resources.HALResource;

@Entity
public class ExampleResource extends HALResource{
	public static final String EXAMPLE_PROFILE_STRING = "https://arucard21.github.io/SimplyRESTful-Framework/ExampleResource/v1";
	
	@JsonIgnore
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	long id;
	@JsonIgnore
	@NotNull
	UUID uuid;

	private String description;
	@OneToOne(cascade=CascadeType.ALL)
	private ExampleComplexAttribute complexAttribute;
	
	public long getId() {
		return id;
	}

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

	public UUID getUUID() {
		return uuid;
	}

	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}
}
