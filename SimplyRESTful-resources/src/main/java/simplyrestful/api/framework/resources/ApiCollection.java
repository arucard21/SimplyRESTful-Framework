package simplyrestful.api.framework.resources;

import java.util.List;
import java.util.Objects;

import jakarta.ws.rs.core.MediaType;

public class ApiCollection<T extends ApiResource> implements ApiResource {
	public static final String MEDIA_TYPE_JSON = "application/x.simplyrestful-collection-v1+json";
	public static final String MEDIA_TYPE_PARAMETER_ITEM_TYPE = "item-type";
	public static final String FIELDS_VALUE_DEFAULT = "self,first,last,prev,next,total,item.self";

	private Link self;
	private int total;
	private Link first;
	private Link last;
	private Link prev;
	private Link next;
	private List<T> item;

	// For JSON serialization
	public Link getSelf() {
		return this.self();
	}

	// For JSON deserialization
	public void setSelf(Link self) {
		this.self = self;
	}

	@Override
	public Link self() {
		return self;
	}

	public Link getFirst() {
		return first;
	}

	public void setFirst(Link first) {
		this.first = first;
	}

	public Link getLast() {
		return last;
	}

	public void setLast(Link last) {
		this.last = last;
	}

	public Link getPrev() {
		return prev;
	}

	public void setPrev(Link prev) {
		this.prev = prev;
	}

	public Link getNext() {
		return next;
	}

	public void setNext(Link next) {
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
	public MediaType customJsonMediaType() {
		return MediaType.valueOf(MEDIA_TYPE_JSON);
	}

	public MediaType customJsonMediaType(MediaType itemResourceMediaType) {
		return MediaType.valueOf(MEDIA_TYPE_JSON + ";" + MEDIA_TYPE_PARAMETER_ITEM_TYPE + "=\""
				+ itemResourceMediaType.toString() + "\"");
	}

	@Override
	public int hashCode() {
		return Objects.hash(self, first, item, last, next, prev, total);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("unchecked")
		ApiCollection<T> other = (ApiCollection<T>) obj;
		return Objects.equals(self, other.self) && Objects.equals(first, other.first)
				&& Objects.equals(item, other.item) && Objects.equals(last, other.last)
				&& Objects.equals(next, other.next) && Objects.equals(prev, other.prev) && total == other.total;
	}

	@Override
	public String toString() {
		return "APICollection [total=" + total + ", first=" + first + ", last=" + last + ", prev=" + prev + ", next="
				+ next + ", item=" + item + "]";
	}
}
