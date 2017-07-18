package simplyrestful.api.framework.core.hal;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import dk.nykredit.jackson.dataformat.hal.HALLink;
import dk.nykredit.jackson.dataformat.hal.annotation.EmbeddedResource;
import dk.nykredit.jackson.dataformat.hal.annotation.Link;
import dk.nykredit.jackson.dataformat.hal.annotation.Resource;
import simplyrestful.api.framework.core.MediaType;

@Resource
public class HALCollection<T extends HALResource> extends HALResource {
	private int page;
	private int pageSize;
	private int total;

	@Link
	private HALLink first;
	@Link
	private HALLink last;
	@Link
	private HALLink prev;
	@Link
	private HALLink next;
	@Link
	private List<HALLink> item;
	@EmbeddedResource("item")
	private List<T> itemEmbedded;

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

	public List<HALLink> getItem() {
		return item;
	}

	public void setItem(List<HALLink> item) {
		this.item = item;
	}

	public List<T> getItemEmbedded() {
		return itemEmbedded;
	}

	public void setItemEmbedded(List<T> itemEmbedded) {
		this.itemEmbedded = itemEmbedded;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	@Override
	public URI getProfile() {
		return URI.create(MediaType.Profile.HALCOLLECTION);
	}

	@Override
	public int hashCode() {
		return page + pageSize + total + (item == null ? 0 : item.hashCode()) + (itemEmbedded == null ? 0 : itemEmbedded.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof HALCollection<?>)){
			return false;
		}
		HALCollection<?> otherCollection = (HALCollection<?>) obj;

		/**
		 * This section of booleans can be removed once HALLink has its own equals() method
		 */
		boolean firstEquals = (first == null && otherCollection.getFirst() == null) ||
				(first != null && otherCollection.getFirst() != null && first.getHref().equals(otherCollection.getFirst().getHref()));
		boolean lastEquals = (last == null && otherCollection.getLast() == null) ||
				(last != null && otherCollection.getLast() != null && last.getHref().equals(otherCollection.getLast().getHref()));
		boolean prevEquals = (prev == null && otherCollection.getPrev() == null) ||
				(prev != null && otherCollection.getPrev() != null && prev.getHref().equals(otherCollection.getPrev().getHref()));
		boolean nextEquals = (next == null && otherCollection.getNext() == null) ||
				(next != null && otherCollection.getNext() != null && next.getHref().equals(otherCollection.getNext().getHref()));
		return
				page == otherCollection.getPage() &&
				pageSize == otherCollection.getPageSize() &&
				total == otherCollection.getTotal() &&
				firstEquals &&
				lastEquals &&
				prevEquals &&
				nextEquals &&
				Objects.equals(item, otherCollection.getItem()) &&
				Objects.equals(itemEmbedded, otherCollection.getItemEmbedded());
	}
}
