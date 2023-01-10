package example.resources.jpa;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.ws.rs.core.MediaType;

import com.fasterxml.jackson.annotation.JsonIgnore;

import simplyrestful.api.framework.resources.APIResource;

@Entity
public class ExampleResource extends APIResource {
    public static final String EXAMPLE_MEDIA_TYPE_JSON = "application/x.testresource-v1+json";
    public static final String EXAMPLE_PROFILE_STRING = "https://arucard21.github.io/SimplyRESTful-Framework/ExampleResource/v1";
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @JsonIgnore
    @Column(columnDefinition = "uuid") // necessary for h2 database, otherwise it will default to binary(16) which doesn't seem to work
    private UUID uuid;
    private String description;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private ExampleComplexAttribute complexAttribute;
    private ZonedDateTime dateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
    public MediaType customJsonMediaType() {
        return MediaType.valueOf(EXAMPLE_MEDIA_TYPE_JSON);
    }

    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(ZonedDateTime dateTime) {
        this.dateTime = dateTime;
    }
}
