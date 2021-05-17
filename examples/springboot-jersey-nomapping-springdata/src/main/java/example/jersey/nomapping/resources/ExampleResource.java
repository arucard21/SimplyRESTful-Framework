package example.jersey.nomapping.resources;

import java.net.URI;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.annotation.JsonIgnore;

import simplyrestful.api.framework.resources.HALResource;

@Entity
public class ExampleResource extends HALResource {
    public static final String EXAMPLE_MEDIA_TYPE_JSON = "application/x.testresource-v1+json";
    public static final String EXAMPLE_PROFILE_STRING = "https://arucard21.github.io/SimplyRESTful-Framework/ExampleResource/v1";
    @JsonIgnore
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @JsonIgnore
    @NotNull
    private UUID uuid;
    private String description;
    @OneToOne(cascade = CascadeType.ALL)
    private ExampleComplexAttribute complexAttribute;

    public long getId() {
	return id;
    }

    public void setId(long id) {
	this.id = id;
    }

    public UUID getUUID() {
	return uuid;
    }

    public void setUUID(UUID uuid) {
	this.uuid = uuid;
    }

    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public ExampleComplexAttribute getComplexAttribute() {
	return complexAttribute;
    }

    public void setComplexAttribute(ExampleComplexAttribute complexAttribute) {
	this.complexAttribute = complexAttribute;
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
