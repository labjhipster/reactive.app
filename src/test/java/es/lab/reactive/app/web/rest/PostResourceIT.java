package es.lab.reactive.app.web.rest;

import es.lab.reactive.app.ReactiveApp;
import es.lab.reactive.app.domain.Post;
import es.lab.reactive.app.repository.PostRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.Base64Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

/**
 * Integration tests for the {@link PostResource} REST controller.
 */
@SpringBootTest(classes = ReactiveApp.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient
@WithMockUser
public class PostResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_CONTENT = "AAAAAAAAAA";
    private static final String UPDATED_CONTENT = "BBBBBBBBBB";

    private static final Instant DEFAULT_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    @Autowired
    private PostRepository postRepository;

    @Mock
    private PostRepository postRepositoryMock;

    @Autowired
    private WebTestClient webTestClient;

    private Post post;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Post createEntity() {
        Post post = new Post()
            .title(DEFAULT_TITLE)
            .content(DEFAULT_CONTENT)
            .date(DEFAULT_DATE);
        return post;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Post createUpdatedEntity() {
        Post post = new Post()
            .title(UPDATED_TITLE)
            .content(UPDATED_CONTENT)
            .date(UPDATED_DATE);
        return post;
    }

    @BeforeEach
    public void initTest() {
        postRepository.deleteAll().block();
        post = createEntity();
    }

    @Test
    public void createPost() throws Exception {
        int databaseSizeBeforeCreate = postRepository.findAll().collectList().block().size();
        // Create the Post
        webTestClient.post().uri("/api/posts")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(post))
            .exchange()
            .expectStatus().isCreated();

        // Validate the Post in the database
        List<Post> postList = postRepository.findAll().collectList().block();
        assertThat(postList).hasSize(databaseSizeBeforeCreate + 1);
        Post testPost = postList.get(postList.size() - 1);
        assertThat(testPost.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testPost.getContent()).isEqualTo(DEFAULT_CONTENT);
        assertThat(testPost.getDate()).isEqualTo(DEFAULT_DATE);
    }

    @Test
    public void createPostWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = postRepository.findAll().collectList().block().size();

        // Create the Post with an existing ID
        post.setId("existing_id");

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient.post().uri("/api/posts")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(post))
            .exchange()
            .expectStatus().isBadRequest();

        // Validate the Post in the database
        List<Post> postList = postRepository.findAll().collectList().block();
        assertThat(postList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    public void checkTitleIsRequired() throws Exception {
        int databaseSizeBeforeTest = postRepository.findAll().collectList().block().size();
        // set the field null
        post.setTitle(null);

        // Create the Post, which fails.


        webTestClient.post().uri("/api/posts")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(post))
            .exchange()
            .expectStatus().isBadRequest();

        List<Post> postList = postRepository.findAll().collectList().block();
        assertThat(postList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void checkDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = postRepository.findAll().collectList().block().size();
        // set the field null
        post.setDate(null);

        // Create the Post, which fails.


        webTestClient.post().uri("/api/posts")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(post))
            .exchange()
            .expectStatus().isBadRequest();

        List<Post> postList = postRepository.findAll().collectList().block();
        assertThat(postList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void getAllPosts() {
        // Initialize the database
        postRepository.save(post).block();

        // Get all the postList
        webTestClient.get().uri("/api/posts?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id").value(hasItem(post.getId()))
            .jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE))
            .jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT.toString()))
            .jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString()));
    }
    
    @SuppressWarnings({"unchecked"})
    public void getAllPostsWithEagerRelationshipsIsEnabled() {
        when(postRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri("/api/posts?eagerload=true")
            .exchange()
            .expectStatus().isOk();

        verify(postRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({"unchecked"})
    public void getAllPostsWithEagerRelationshipsIsNotEnabled() {
        when(postRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri("/api/posts?eagerload=true")
            .exchange()
            .expectStatus().isOk();

        verify(postRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    public void getPost() {
        // Initialize the database
        postRepository.save(post).block();

        // Get the post
        webTestClient.get().uri("/api/posts/{id}", post.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id").value(is(post.getId()))
            .jsonPath("$.title").value(is(DEFAULT_TITLE))
            .jsonPath("$.content").value(is(DEFAULT_CONTENT.toString()))
            .jsonPath("$.date").value(is(DEFAULT_DATE.toString()));
    }
    @Test
    public void getNonExistingPost() {
        // Get the post
        webTestClient.get().uri("/api/posts/{id}", Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    public void updatePost() throws Exception {
        // Initialize the database
        postRepository.save(post).block();

        int databaseSizeBeforeUpdate = postRepository.findAll().collectList().block().size();

        // Update the post
        Post updatedPost = postRepository.findById(post.getId()).block();
        updatedPost
            .title(UPDATED_TITLE)
            .content(UPDATED_CONTENT)
            .date(UPDATED_DATE);

        webTestClient.put().uri("/api/posts")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedPost))
            .exchange()
            .expectStatus().isOk();

        // Validate the Post in the database
        List<Post> postList = postRepository.findAll().collectList().block();
        assertThat(postList).hasSize(databaseSizeBeforeUpdate);
        Post testPost = postList.get(postList.size() - 1);
        assertThat(testPost.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testPost.getContent()).isEqualTo(UPDATED_CONTENT);
        assertThat(testPost.getDate()).isEqualTo(UPDATED_DATE);
    }

    @Test
    public void updateNonExistingPost() throws Exception {
        int databaseSizeBeforeUpdate = postRepository.findAll().collectList().block().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient.put().uri("/api/posts")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(post))
            .exchange()
            .expectStatus().isBadRequest();

        // Validate the Post in the database
        List<Post> postList = postRepository.findAll().collectList().block();
        assertThat(postList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    public void deletePost() {
        // Initialize the database
        postRepository.save(post).block();

        int databaseSizeBeforeDelete = postRepository.findAll().collectList().block().size();

        // Delete the post
        webTestClient.delete().uri("/api/posts/{id}", post.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNoContent();

        // Validate the database contains one less item
        List<Post> postList = postRepository.findAll().collectList().block();
        assertThat(postList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
