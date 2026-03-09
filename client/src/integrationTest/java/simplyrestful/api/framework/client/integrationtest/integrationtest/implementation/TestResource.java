package simplyrestful.api.framework.client.integrationtest.integrationtest.implementation;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import simplyrestful.api.framework.resources.ApiResource;
import simplyrestful.api.framework.resources.Link;

public class TestResource implements ApiResource {
	public static final String MEDIA_TYPE_JSON = "application/x.testresource-v1+json";
	public static final UUID TEST_RESOURCE_ID = UUID.randomUUID();
	public static final String ADDITIONAL_FIELD_TEST_VALUE = "additional-field-value";

	private Link self;
	private String additionalField;

	public static URI getResourceUri(UUID id) {
		return UriBuilder.fromUri(TestWebResource.getBaseUri()).path(TestWebResource.class).path(id.toString()).build();
	}

	private TestResource(URI resourceUri, String additionalField) {
		this.additionalField = additionalField;
		this.self(new Link(resourceUri, customJsonMediaType()));
	}

	public TestResource() {
	}

	@Override
	@JsonGetter("self")
	public Link self() {
		return self;
	}

	@Override
	@JsonSetter("self")
	public void self(Link self) {
		this.self = self;
	}

	public static TestResource testInstance() {
		return new TestResource(TestResource.getResourceUri(TEST_RESOURCE_ID), ADDITIONAL_FIELD_TEST_VALUE);
	}

	public static TestResource random() {
		return TestResource.withId(UUID.randomUUID());
	}

	public static TestResource withId(UUID resourceId) {
		return new TestResource(getResourceUri(resourceId), ADDITIONAL_FIELD_TEST_VALUE);
	}

	@Override
	public MediaType customJsonMediaType() {
		return MediaType.valueOf(MEDIA_TYPE_JSON);
	}

	public String getAdditionalField() {
		return additionalField;
	}

	public void setAdditionalField(String additionalField) {
		this.additionalField = additionalField;
	}

	@Override
	public int hashCode() {
		return Objects.hash(self, additionalField);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestResource other = (TestResource) obj;
		return Objects.equals(self, other.self) && Objects.equals(additionalField, other.additionalField);
	}
}
