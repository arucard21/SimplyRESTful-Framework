package example.jetty.resources.dao;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import example.jetty.resources.ExampleResource;

public interface ExampleEntityDAO {
    public long count();
    public boolean exists(UUID entityID);
    public List<ExampleResource> findAllForPage(int pageNumber, int pageSize);
    public Stream<ExampleResource> stream();
    public ExampleResource findByUUID(UUID entityID);
    public ExampleResource persist(ExampleResource entity);
    public ExampleResource remove(UUID entityID);
}
