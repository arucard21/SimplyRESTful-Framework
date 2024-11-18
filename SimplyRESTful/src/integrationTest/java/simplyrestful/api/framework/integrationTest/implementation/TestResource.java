package simplyrestful.api.framework.integrationTest.implementation;

import java.net.URI;
import java.util.UUID;

import jakarta.ws.rs.core.MediaType;

import simplyrestful.api.framework.resources.ApiResource;
import simplyrestful.api.framework.resources.Link;

public class TestResource extends ApiResource {
	public static final String MEDIA_TYPE_JSON = "application/x.testresource-v1+json";
	public static final String TEST_REQUEST_URI = "testresources/";
	public static final UUID TEST_RESOURCE_ID = UUID.fromString("bb2adabf-effe-4fb4-900b-d3b32cd9eed3");

	private TestResource(URI resourceUri) {
		this.setSelf(new Link(resourceUri, this.customJsonMediaType()));
	}

	public TestResource() {
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
}
