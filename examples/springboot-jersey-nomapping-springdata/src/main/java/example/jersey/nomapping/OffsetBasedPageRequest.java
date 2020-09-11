package example.jersey.nomapping;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class OffsetBasedPageRequest implements Pageable{
    private final int pageOffset;
    private final int pageSize;
    private final Sort sort;    
    
    public OffsetBasedPageRequest(int pageOffset, int pageSize) {
	this.pageOffset = pageOffset;
	this.pageSize = pageSize;
	this.sort = Sort.unsorted();
    }
    
    public OffsetBasedPageRequest(int pageOffset, int pageSize, Sort sort) {
	this.pageOffset = pageOffset;
	this.pageSize = pageSize;
	this.sort = sort == null ? Sort.unsorted() : sort;
    }

    @Override
    public int getPageNumber() {
	return pageOffset / pageSize;
    }

    @Override
    public int getPageSize() {
	return pageSize;
    }

    @Override
    public long getOffset() {
	return pageOffset;
    }

    @Override
    public Sort getSort() {
	return sort;
    }

    @Override
    public Pageable next() {
	 return new OffsetBasedPageRequest(pageOffset + pageSize, pageSize, sort);
    }

    @Override
    public Pageable previousOrFirst() {
	return hasPrevious() ? new OffsetBasedPageRequest(pageOffset - pageSize, pageSize, sort) : first();
    }

    @Override
    public Pageable first() {
	return new OffsetBasedPageRequest(0, pageSize, sort);
    }

    @Override
    public boolean hasPrevious() {
	return pageOffset > pageSize;
    }
    
}
