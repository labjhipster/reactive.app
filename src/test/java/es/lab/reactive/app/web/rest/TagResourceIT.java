package es.lab.reactive.app.web.rest;

import es.lab.reactive.app.ReactiveApp;
import es.lab.reactive.app.domain.Tag;
import es.lab.reactive.app.repository.TagRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

/**
 * Integration tests for the {@link TagResource} REST controller.
 */
@SpringBootTest(classes = ReactiveApp.class)
@AutoConfigureWebTestClient
@WithMockUser
public class TagResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private WebTestClient webTestClient;

    private Tag tag;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Tag createEntity() {
        Tag tag = new Tag()
            .name(DEFAULT_NAME);
        return tag;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Tag createUpdatedEntity() {
        Tag tag = new Tag()
            .name(UPDATED_NAME);
        return tag;
    }

    @BeforeEach
    public void initTest() {
        tagRepository.deleteAll().block();
        tag = createEntity();
    }

    @Test
    public void createTag() throws Exception {
        int databaseSizeBeforeCreate = tagRepository.findAll().collectList().block().size();
        // Create the Tag
        webTestClient.post().uri("/api/tags")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(tag))
            .exchange()
            .expectStatus().isCreated();

        // Validate the Tag in the database
        List<Tag> tagList = tagRepository.findAll().collectList().block();
        assertThat(tagList).hasSize(databaseSizeBeforeCreate + 1);
        Tag testTag = tagList.get(tagList.size() - 1);
        assertThat(testTag.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    public void createTagWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = tagRepository.findAll().collectList().block().size();

        // Create the Tag with an existing ID
        tag.setId("existing_id");

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient.post().uri("/api/tags")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(tag))
            .exchange()
            .expectStatus().isBadRequest();

        // Validate the Tag in the database
        List<Tag> tagList = tagRepository.findAll().collectList().block();
        assertThat(tagList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = tagRepository.findAll().collectList().block().size();
        // set the field null
        tag.setName(null);

        // Create the Tag, which fails.


        webTestClient.post().uri("/api/tags")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(tag))
            .exchange()
            .expectStatus().isBadRequest();

        List<Tag> tagList = tagRepository.findAll().collectList().block();
        assertThat(tagList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void getAllTags() {
        // Initialize the database
        tagRepository.save(tag).block();

        // Get all the tagList
        webTestClient.get().uri("/api/tags?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id").value(hasItem(tag.getId()))
            .jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME));
    }
    
    @Test
    public void getTag() {
        // Initialize the database
        tagRepository.save(tag).block();

        // Get the tag
        webTestClient.get().uri("/api/tags/{id}", tag.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id").value(is(tag.getId()))
            .jsonPath("$.name").value(is(DEFAULT_NAME));
    }
    @Test
    public void getNonExistingTag() {
        // Get the tag
        webTestClient.get().uri("/api/tags/{id}", Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    public void updateTag() throws Exception {
        // Initialize the database
        tagRepository.save(tag).block();

        int databaseSizeBeforeUpdate = tagRepository.findAll().collectList().block().size();

        // Update the tag
        Tag updatedTag = tagRepository.findById(tag.getId()).block();
        updatedTag
            .name(UPDATED_NAME);

        webTestClient.put().uri("/api/tags")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedTag))
            .exchange()
            .expectStatus().isOk();

        // Validate the Tag in the database
        List<Tag> tagList = tagRepository.findAll().collectList().block();
        assertThat(tagList).hasSize(databaseSizeBeforeUpdate);
        Tag testTag = tagList.get(tagList.size() - 1);
        assertThat(testTag.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    public void updateNonExistingTag() throws Exception {
        int databaseSizeBeforeUpdate = tagRepository.findAll().collectList().block().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient.put().uri("/api/tags")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(tag))
            .exchange()
            .expectStatus().isBadRequest();

        // Validate the Tag in the database
        List<Tag> tagList = tagRepository.findAll().collectList().block();
        assertThat(tagList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    public void deleteTag() {
        // Initialize the database
        tagRepository.save(tag).block();

        int databaseSizeBeforeDelete = tagRepository.findAll().collectList().block().size();

        // Delete the tag
        webTestClient.delete().uri("/api/tags/{id}", tag.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNoContent();

        // Validate the database contains one less item
        List<Tag> tagList = tagRepository.findAll().collectList().block();
        assertThat(tagList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
