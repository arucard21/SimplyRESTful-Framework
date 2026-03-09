package simplyrestful.api.framework.integrationTest.implementation;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import jakarta.ws.rs.core.MediaType;

import simplyrestful.api.framework.resources.ApiResource;
import simplyrestful.api.framework.resources.Link;

public class TestResource implements ApiResource {
	public static final String MEDIA_TYPE_JSON = "application/x.testresource-v1+json";
	public static final String TEST_REQUEST_URI = "testresources/";
	public static final UUID TEST_RESOURCE_ID = UUID.fromString("bb2adabf-effe-4fb4-900b-d3b32cd9eed3");

	private Link self;

	private TestResource(URI resourceUri) {
		this.self(new Link(resourceUri, this.customJsonMediaType()));
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

	public static TestResource testInstance(URI baseUri) {
		return new TestResource(baseUri.resolve(TEST_REQUEST_URI).resolve(TEST_RESOURCE_ID.toString()));
	}

	public static TestResource random(URI baseUri) {
		return new TestResource(baseUri.resolve(TEST_REQUEST_URI).resolve(UUID.randomUUID().toString()));
	}

	public static TestResource custom(URI baseUri, UUID uuid) {
		return new TestResource(baseUri.resolve(TEST_REQUEST_URI).resolve(uuid.toString()));
	}

	@Override
	public MediaType customJsonMediaType() {
		return MediaType.valueOf(MEDIA_TYPE_JSON);
	}

	@Override
	public int hashCode() {
		return Objects.hash(self);
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
		return Objects.equals(self, other.self);
	}
}