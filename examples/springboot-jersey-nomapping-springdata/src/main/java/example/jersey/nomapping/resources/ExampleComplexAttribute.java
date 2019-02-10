package example.jersey.nomapping.resources;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class ExampleComplexAttribute {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@JsonIgnore
	private UUID id;

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
