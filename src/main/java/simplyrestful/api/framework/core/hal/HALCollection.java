package simplyrestful.api.framework.core.hal;

import java.net.URI;
import java.util.List;

import dk.nykredit.jackson.dataformat.hal.HALLink;
import dk.nykredit.jackson.dataformat.hal.annotation.EmbeddedResource;
import dk.nykredit.jackson.dataformat.hal.annotation.Link;
import dk.nykredit.jackson.dataformat.hal.annotation.Resource;

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
		return URI.create("https://arucard21.github.io/SimplyRESTful/HALCollection/v1");
	}
}
