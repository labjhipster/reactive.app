package es.lab.reactive.app.repository;

import es.lab.reactive.app.domain.Post;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB reactive repository for the Post entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PostRepository extends ReactiveMongoRepository<Post, String> {


}
