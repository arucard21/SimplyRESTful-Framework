package example.jersey.nomapping.resources;

import java.net.URI;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

import simplyrestful.api.framework.resources.Link;
import simplyrestful.api.framework.utils.WebResourceUtils;

/**
 * Maps between {@link ExampleResourceRecord} (DTO) and {@link ExampleResourceEntity} (JPA entity).
 */
public class ExampleResourceMapper {

    private final UriInfo uriInfo;

    public ExampleResourceMapper(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    /**
     * Convert a JPA entity to a DTO record, including the self-link derived from the entity's UUID.
     *
     * @param entity the JPA entity to convert.
     * @return the corresponding DTO record.
     */
    public ExampleResourceRecord toRecord(ExampleResourceEntity entity) {
        if (entity == null) {
            return null;
        }
        Link selfLink = null;
        if (entity.getUuid() != null) {
            URI selfUri = WebResourceUtils.getAbsoluteWebResourceUri(uriInfo, ExampleWebResource.class, entity.getUuid());
            selfLink = new Link(selfUri, MediaType.valueOf(ExampleResourceRecord.EXAMPLE_MEDIA_TYPE_JSON));
        }
        ExampleComplexAttributeRecord complexAttribute = null;
        if (entity.getComplexAttribute() != null) {
            complexAttribute = new ExampleComplexAttributeRecord(entity.getComplexAttribute().getName());
        }
        return new ExampleResourceRecord(selfLink, entity.getDescription(), complexAttribute, entity.getDateTime());
    }

    /**
     * Convert a DTO record to a JPA entity. The self-link is not mapped; the UUID must be set separately.
     *
     * @param record the DTO record to convert.
     * @return the corresponding JPA entity.
     */
    public ExampleResourceEntity toEntity(ExampleResourceRecord record) {
        if (record == null) {
            return null;
        }
        ExampleResourceEntity entity = new ExampleResourceEntity();
        entity.setDescription(record.description());
        entity.setDateTime(record.dateTime());
        if (record.complexAttribute() != null) {
            ExampleComplexAttributeEntity complexAttr = new ExampleComplexAttributeEntity();
            complexAttr.setName(record.complexAttribute().name());
            entity.setComplexAttribute(complexAttr);
        }
        return entity;
    }
}
