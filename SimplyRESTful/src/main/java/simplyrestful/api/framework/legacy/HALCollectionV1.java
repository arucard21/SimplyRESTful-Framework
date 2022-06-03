package simplyrestful.api.framework.legacy;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.MediaType;

import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.annotation.EmbeddedResource;
import io.openapitools.jackson.dataformat.hal.annotation.Link;
import io.openapitools.jackson.dataformat.hal.annotation.Resource;
import simplyrestful.api.framework.resources.HALCollection;
import simplyrestful.api.framework.resources.HALCollectionV2;
import simplyrestful.api.framework.resources.HALResource;
import simplyrestful.api.framework.webresource.api.implementation.DefaultCollectionGet;

/**
 * @deprecated Use {@link DefaultCollectionGet} with the {@link HALCollectionV2} resource instead.
 */
@Resource
@Deprecated(since="0.12.0")
public class HALCollectionV1<T extends HALResource> extends HALCollection<T>{
	public static final String PROFILE_STRING = "https://arucard21.github.io/SimplyRESTful-Framework/HALCollection/v1";
	public static final String MEDIA_TYPE_HAL_JSON = "application/hal+json;profile=\""+PROFILE_STRING+"\"";

	private long page;
	private long pageSize;
	private long total;

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

	public long getPage() {
		return page;
	}

	public void setPage(long page) {
		this.page = page;
	}

	public long getPageSize() {
		return pageSize;
	}

	public void setPageSize(long pageSize) {
		this.pageSize = pageSize;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	@Override
	public URI getProfile() {
		return URI.create(PROFILE_STRING);
	}

	@Override
	public MediaType getCustomJsonMediaType() {
	    return MediaType.APPLICATION_JSON_TYPE;
	}

	@Override
	public final int hashCode() {
		return Objects.hash(getSelf(), getProfile(), page, pageSize, total, first, last, prev, next, item, itemEmbedded);
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj instanceof HALCollectionV1<?>){
			HALCollectionV1<?> otherCollection = (HALCollectionV1<?>) obj;
			return otherCollection.canEqual(this) &&
					Objects.equals(getSelf(), otherCollection.getSelf()) &&
					Objects.equals(getProfile(), otherCollection.getProfile()) &&
					Objects.equals(page, otherCollection.getPage()) &&
					Objects.equals(pageSize, otherCollection.getPageSize()) &&
					Objects.equals(total, otherCollection.getTotal()) &&
					Objects.equals(first, otherCollection.getFirst())&&
					Objects.equals(last, otherCollection.getLast()) &&
					Objects.equals(prev, otherCollection.getPrev()) &&
					Objects.equals(next, otherCollection.getNext()) &&
					Objects.equals(item, otherCollection.getItem()) &&
					Objects.equals(itemEmbedded, otherCollection.getItemEmbedded());
		}
		return false;
	}

	protected boolean canEqual(Object obj) {
		return (obj instanceof HALCollectionV1<?>);
	}
}
