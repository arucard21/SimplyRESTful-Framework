package example.jersey.nomapping.resources;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class ExampleEmbeddedResource {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private UUID id;

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
