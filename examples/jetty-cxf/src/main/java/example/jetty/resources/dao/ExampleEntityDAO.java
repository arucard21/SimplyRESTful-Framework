package example.jetty.resources.dao;

import java.util.List;
import java.util.UUID;

import example.jetty.resources.ExampleResource;

public interface ExampleEntityDAO {
	public long count();
	public List<ExampleResource> findAllForPage(long pageNumber, long pageSize);
	public ExampleResource findByUUID(UUID entityID);
	public ExampleResource persist(ExampleResource entity);
	public ExampleResource remove(UUID entityID);
}
