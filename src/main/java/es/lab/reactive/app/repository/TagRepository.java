package es.lab.reactive.app.repository;

import es.lab.reactive.app.domain.Tag;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB reactive repository for the Tag entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TagRepository extends ReactiveMongoRepository<Tag, String> {


}
