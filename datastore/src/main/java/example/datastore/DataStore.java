package example.datastore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import example.datastore.StoredEmbeddedObject;
import example.datastore.StoredObject;

public class DataStore {
	private List<StoredObject> data;

	/**
	 * Create the data store that is used in the example and populate it with some
	 * initial data.
	 */
    public DataStore() {
    	data = Collections.synchronizedList(new ArrayList<StoredObject>());
    	StoredObject resource1 =  new StoredObject();
    	resource1.setId(UUID.randomUUID());
        resource1.setDescription("This is the first stored object");
        StoredObject resource2 =  new StoredObject();
        resource2.setId(UUID.randomUUID());
        resource2.setDescription("This is the second stored object");

        StoredEmbeddedObject embedded1 = new StoredEmbeddedObject();
        embedded1.setId(UUID.randomUUID());
        embedded1.setName("Embedded 1");
        StoredEmbeddedObject embedded2 = new StoredEmbeddedObject();
        embedded2.setId(UUID.randomUUID());
        embedded2.setName("Embedded 2");

        resource1.setEmbedded(embedded1);
        resource2.setEmbedded(embedded2);

        data.add(resource1);
        data.add(resource2);
    }

	public List<StoredObject> getData() {
		return data;
	}

	public void setData(List<StoredObject> data) {
		this.data = data;
	}

	/**
	 * Convenience method to search for a stored object based on its ID.
	 *
	 * @param objectID is the ID of the stored object
	 * @return the stored object matching the provided ID, null if not found
	 */
	public StoredObject getObject(UUID objectID){
		for (StoredObject curObject : data){
			if (curObject.getId().equals(objectID)){
				return curObject;
			}
		}
		return null;
	}
}
