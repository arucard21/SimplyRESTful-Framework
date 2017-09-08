package example.datastore;

import java.util.UUID;

import example.datastore.StoredEmbeddedObject;

public class StoredObject {
	private UUID id;
	private String description;
	private StoredEmbeddedObject embedded;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public StoredEmbeddedObject getEmbedded() {
		return embedded;
	}

	public void setEmbedded(StoredEmbeddedObject embedded) {
		this.embedded = embedded;
	}
}
