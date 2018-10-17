package simplyrestful.springdata.repository.nomapping;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.openapitools.jackson.dataformat.hal.HALLink;
import simplyrestful.api.framework.resources.HALResource;

@MappedSuperclass
@Inheritance
public abstract class NoMappingHALResource extends HALResource {
	@JsonIgnore
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	long id;
	@JsonIgnore
	@Column(unique=true)
	UUID uuid;

	public long getId() {
		return id;
	}

	public UUID getUUID() {
		return uuid;
	}

	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}

	@Transient
	@Override
	public HALLink getSelf(){
		return super.getSelf();
	}
}
