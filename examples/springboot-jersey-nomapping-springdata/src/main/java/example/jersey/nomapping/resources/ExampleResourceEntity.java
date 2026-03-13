package example.jersey.nomapping.resources;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class ExampleResourceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(columnDefinition = "uuid") // necessary for h2 database, otherwise it will default to binary(16) which doesn't seem to work
    private UUID uuid;

    private String description;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private ExampleComplexAttributeEntity complexAttribute;

    private ZonedDateTime dateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ExampleComplexAttributeEntity getComplexAttribute() {
        return complexAttribute;
    }

    public void setComplexAttribute(ExampleComplexAttributeEntity complexAttribute) {
        this.complexAttribute = complexAttribute;
    }

    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(ZonedDateTime dateTime) {
        this.dateTime = dateTime;
    }
}
