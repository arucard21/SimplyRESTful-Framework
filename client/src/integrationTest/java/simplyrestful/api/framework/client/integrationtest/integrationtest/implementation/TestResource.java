package simplyrestful.api.framework.client.integrationtest.integrationtest.implementation;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import simplyrestful.api.framework.resources.APIResource;
import simplyrestful.api.framework.resources.Link;

public class TestResource extends APIResource {
	public static final String MEDIA_TYPE_JSON = "application/x.testresource-v1+json";
	public static final UUID TEST_RESOURCE_ID = UUID.randomUUID();
	public static final String ADDITIONAL_FIELD_TEST_VALUE = "additional-field-value";

	private String additionalField;

	public static URI getResourceUri(UUID id) {
		return UriBuilder.fromUri(TestWebResource.getBaseUri()).path(TestWebResource.class).path(id.toString()).build();
	}

	private TestResource(URI resourceUri, String additionalField) {
		this.additionalField = additionalField;
		this.setSelf(new Link(resourceUri, customJsonMediaType()));
	}

	public TestResource() {
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
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(additionalField);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestResource other = (TestResource) obj;
		return Objects.equals(additionalField, other.additionalField);
	}
}
