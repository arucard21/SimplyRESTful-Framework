package simplyrestful.springdata.repository.nomapping;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.openapitools.jackson.dataformat.hal.HALLink;
import simplyrestful.api.framework.core.hal.HALResource;

@Entity
public abstract class NoMappingHALResource extends HALResource {
	@JsonIgnore
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
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
