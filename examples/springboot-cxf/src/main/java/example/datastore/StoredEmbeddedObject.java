package example.datastore;

import java.util.UUID;

public class StoredEmbeddedObject {
	private UUID id;
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}
}
