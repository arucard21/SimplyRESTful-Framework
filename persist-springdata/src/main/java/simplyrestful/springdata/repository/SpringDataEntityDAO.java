package simplyrestful.springdata.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;

import simplyrestful.api.framework.core.EntityDAO;

public abstract class SpringDataEntityDAO<E> extends EntityDAO<E>{
	private final SpringDataRepository<E> repo;
	
	public SpringDataEntityDAO(SpringDataRepository<E> repo) {
		this.repo = repo;
	}

	public long count() {
		return repo.count();
	}

	public List<E> findAllForPage(int pageNumber, int pageSize) {
		return repo.findAll(PageRequest.of(pageNumber-1, pageSize)).getContent();
		
	}

	public E findByUUID(UUID entityID) {
		Optional<E> result = repo.findByUuid(entityID);
		if (result.isPresent()) {
			E retrievedEntity = result.get();
			return retrievedEntity;
		}
		return null;
	}

	public E persist(E entity) {
		return repo.save(entity);
	}

	public E remove(UUID entityID) {
		E previousValue = findByUUID(entityID);
		if (previousValue == null) {
			return null;
		}
		repo.delete(previousValue);
		return previousValue;
	}

	protected SpringDataRepository<E> getRepo() {
		return repo;
	}
}
