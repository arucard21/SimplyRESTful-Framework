package example.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataStore {
	private Map<UUID, StoredResource> data;

    public DataStore() {
    	data = Collections.synchronizedMap(new HashMap<UUID, StoredResource>());
    	StoredResource resource1 =  new StoredResource();
    	resource1.setId(UUID.randomUUID());
        resource1.setDescription("This is the first resource");
        StoredResource resource2 =  new StoredResource();
        resource2.setId(UUID.randomUUID());
        resource2.setDescription("This is the second resource");
        data.put(resource1.getId(), resource1);
        data.put(resource2.getId(), resource2);
    }

	public Map<UUID, StoredResource> getData() {
		return data;
	}

	public void setResourcesByID(Map<UUID, StoredResource> resourcesByID) {
		this.data = resourcesByID;
	}
}
