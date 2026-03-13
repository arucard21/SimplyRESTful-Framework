package example.jersey.nomapping.resources;

import java.time.ZonedDateTime;

import jakarta.ws.rs.core.MediaType;

import simplyrestful.api.framework.resources.ApiResource;
import simplyrestful.api.framework.resources.Link;

public record ExampleResourceRecord(
        Link self,
        String description,
        ExampleComplexAttributeRecord complexAttribute,
        ZonedDateTime dateTime) implements ApiResource {

    public static final String EXAMPLE_MEDIA_TYPE_JSON = "application/x.testresource-v1+json";
    public static final String EXAMPLE_PROFILE_STRING = "https://arucard21.github.io/SimplyRESTful-Framework/ExampleResource/v1";

    @Override
    public Link self() {
        return self;
    }

    @Override
    public MediaType customJsonMediaType() {
        return MediaType.valueOf(EXAMPLE_MEDIA_TYPE_JSON);
    }
}
