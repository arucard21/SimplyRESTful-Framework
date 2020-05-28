package simplyrestful.springdata.resources;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import simplyrestful.api.framework.resources.HALResource;
/**
 * @deprecated Direct mapping of API resources to database entities is not useful enough to maintain this convenience library.
 * Use the standard SimplyRESTful library (without automated mapping) instead.
 * 
 */
@Deprecated
@JsonIgnoreProperties(ignoreUnknown = true)
@MappedSuperclass
public abstract class SpringDataHALResource extends HALResource{
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@JsonIgnore
	private long id;
	
	@JsonIgnore
	@NotNull
	UUID uuid;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public UUID getUUID() {
		return uuid;
	}

	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}
}
