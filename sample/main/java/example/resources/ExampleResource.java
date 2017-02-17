package example.resources;

import api.framework.core.hal.HalResource;

public class ExampleResource extends HalResource{

	private String description;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
