package example.microprofile.openliberty.resources;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import example.resources.jpa.ExampleResource;

// FIXME Make this actually work
@RequestScoped
public class ExampleDao {
    @PersistenceContext(name = "jpa-unit")
    private EntityManager em;

    public ExampleResource create(ExampleResource exampleResource) {
        em.persist(exampleResource);
        return em.find(ExampleResource.class, exampleResource.getId());
    }

    public ExampleResource read(long id) {
        return em.find(ExampleResource.class, id);
    }

    public ExampleResource update(ExampleResource exampleResource) {
        return em.merge(exampleResource);
    }

    public void delete(ExampleResource exampleResource) {
        em.remove(exampleResource);
    }

    // TODO add query and paging parameter
    public List<ExampleResource> findAll() {
        return em.createQuery("SELECT e FROM ExampleResource e", ExampleResource.class).getResultList();
    }

    // TODO add query parameter
    public Stream<ExampleResource> streamAll() {
        return em.createQuery("SELECT e FROM ExampleResource e", ExampleResource.class).getResultStream();
    }

    public int count() {
        return em.createQuery("SELECT e FROM ExampleResource e", ExampleResource.class).getResultList().size();
    }

    public Optional<ExampleResource> findByUuid(UUID uuid) {
        return Optional.of(em.createQuery("SELECT e FROM ExampleResource e WHERE uuid = :uuid", ExampleResource.class)
            .setParameter("uuid", uuid).getSingleResult());
    }

    public boolean existsByUuid(UUID uuid) {
        return em.createQuery("SELECT e FROM ExampleResource e WHERE uuid = :uuid", ExampleResource.class)
            .setParameter("uuid", uuid).getResultList().size() > 0;
    }
}
