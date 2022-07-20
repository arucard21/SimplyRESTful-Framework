package simplyrestful.api.framework.resources;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.MediaType;

import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.annotation.EmbeddedResource;
import io.openapitools.jackson.dataformat.hal.annotation.Link;
import io.openapitools.jackson.dataformat.hal.annotation.Resource;

@Resource
public class HALCollectionV2<T extends HALResource> extends HALCollection<T> {
    public static final String PROFILE_STRING = "https://arucard21.github.io/SimplyRESTful-Framework/HALCollection/v2";
    public static final String MEDIA_TYPE_HAL_JSON = "application/hal+json;profile=\""+PROFILE_STRING+"\"";
    public static final String MEDIA_TYPE_JSON = "application/x.simplyrestful-halcollection-v2+json";
    public static final String MEDIA_TYPE_PARAMETER_ITEM_TYPE = "item-type";

    private int total;
    @Link
    private HALLink first;
    @Link
    private HALLink last;
    @Link
    private HALLink prev;
    @Link
    private HALLink next;
    @EmbeddedResource
    private List<T> item;

    public HALLink getFirst() {
	return first;
    }

    public void setFirst(HALLink first) {
	this.first = first;
    }

    public HALLink getLast() {
	return last;
    }

    public void setLast(HALLink last) {
	this.last = last;
    }

    public HALLink getPrev() {
	return prev;
    }

    public void setPrev(HALLink prev) {
	this.prev = prev;
    }

    public HALLink getNext() {
	return next;
    }

    public void setNext(HALLink next) {
	this.next = next;
    }

    public List<T> getItem() {
	return item;
    }

    public void setItem(List<T> item) {
	this.item = item;
    }

    public int getTotal() {
	return total;
    }

    public void setTotal(int total) {
	this.total = total;
    }

    @Override
    public URI getProfile() {
	return URI.create(PROFILE_STRING);
    }

    @Override
    public MediaType getCustomJsonMediaType() {
	return MediaType.valueOf(MEDIA_TYPE_JSON);
    }

    @Override
    public final int hashCode() {
	return Objects.hash(getSelf(), getProfile(), total, first, last, prev, next, item);
    }

    @Override
    public final boolean equals(Object obj) {
	if (obj instanceof HALCollectionV2<?>) {
	    HALCollectionV2<?> otherCollection = (HALCollectionV2<?>) obj;
	    return otherCollection.canEqual(this) && Objects.equals(getSelf(), otherCollection.getSelf())
		    && Objects.equals(getProfile(), otherCollection.getProfile())
		    && Objects.equals(total, otherCollection.getTotal())
		    && Objects.equals(first, otherCollection.getFirst())
		    && Objects.equals(last, otherCollection.getLast())
		    && Objects.equals(prev, otherCollection.getPrev())
		    && Objects.equals(next, otherCollection.getNext())
		    && Objects.equals(item, otherCollection.getItem());
	}
	return false;
    }

    protected boolean canEqual(Object obj) {
	return (obj instanceof HALCollectionV2<?>);
    }
}
