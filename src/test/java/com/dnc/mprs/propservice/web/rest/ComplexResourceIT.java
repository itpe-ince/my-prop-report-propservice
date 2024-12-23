package com.dnc.mprs.propservice.web.rest;

import static com.dnc.mprs.propservice.domain.ComplexAsserts.*;
import static com.dnc.mprs.propservice.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.dnc.mprs.propservice.IntegrationTest;
import com.dnc.mprs.propservice.domain.Complex;
import com.dnc.mprs.propservice.repository.ComplexRepository;
import com.dnc.mprs.propservice.repository.EntityManager;
import com.dnc.mprs.propservice.repository.search.ComplexSearchRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.data.util.Streamable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the {@link ComplexResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class ComplexResourceIT {

    private static final String DEFAULT_COMPLEX_NAME = "AAAAAAAAAA";
    private static final String UPDATED_COMPLEX_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_STATE = "AAAAAAAAAA";
    private static final String UPDATED_STATE = "BBBBBBBBBB";

    private static final String DEFAULT_COUNTY = "AAAAAAAAAA";
    private static final String UPDATED_COUNTY = "BBBBBBBBBB";

    private static final String DEFAULT_CITY = "AAAAAAAAAA";
    private static final String UPDATED_CITY = "BBBBBBBBBB";

    private static final String DEFAULT_TOWN = "AAAAAAAAAA";
    private static final String UPDATED_TOWN = "BBBBBBBBBB";

    private static final String DEFAULT_ADDRESS_CODE = "AAAAAAAAAA";
    private static final String UPDATED_ADDRESS_CODE = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_UPDATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_UPDATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/complexes";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/complexes/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ComplexRepository complexRepository;

    @Autowired
    private ComplexSearchRepository complexSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Complex complex;

    private Complex insertedComplex;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Complex createEntity() {
        return new Complex()
            .complexName(DEFAULT_COMPLEX_NAME)
            .state(DEFAULT_STATE)
            .county(DEFAULT_COUNTY)
            .city(DEFAULT_CITY)
            .town(DEFAULT_TOWN)
            .addressCode(DEFAULT_ADDRESS_CODE)
            .createdAt(DEFAULT_CREATED_AT)
            .updatedAt(DEFAULT_UPDATED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Complex createUpdatedEntity() {
        return new Complex()
            .complexName(UPDATED_COMPLEX_NAME)
            .state(UPDATED_STATE)
            .county(UPDATED_COUNTY)
            .city(UPDATED_CITY)
            .town(UPDATED_TOWN)
            .addressCode(UPDATED_ADDRESS_CODE)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Complex.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @BeforeEach
    public void setupCsrf() {
        webTestClient = webTestClient.mutateWith(csrf());
    }

    @BeforeEach
    public void initTest() {
        complex = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedComplex != null) {
            complexRepository.delete(insertedComplex).block();
            complexSearchRepository.delete(insertedComplex).block();
            insertedComplex = null;
        }
        deleteEntities(em);
    }

    @Test
    void createComplex() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(complexSearchRepository.findAll().collectList().block());
        // Create the Complex
        var returnedComplex = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(complex))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(Complex.class)
            .returnResult()
            .getResponseBody();

        // Validate the Complex in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertComplexUpdatableFieldsEquals(returnedComplex, getPersistedComplex(returnedComplex));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(complexSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });

        insertedComplex = returnedComplex;
    }

    @Test
    void createComplexWithExistingId() throws Exception {
        // Create the Complex with an existing ID
        complex.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(complexSearchRepository.findAll().collectList().block());

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(complex))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Complex in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(complexSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkComplexNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(complexSearchRepository.findAll().collectList().block());
        // set the field null
        complex.setComplexName(null);

        // Create the Complex, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(complex))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(complexSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(complexSearchRepository.findAll().collectList().block());
        // set the field null
        complex.setCreatedAt(null);

        // Create the Complex, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(complex))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(complexSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void getAllComplexes() {
        // Initialize the database
        insertedComplex = complexRepository.save(complex).block();

        // Get all the complexList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(complex.getId().intValue()))
            .jsonPath("$.[*].complexName")
            .value(hasItem(DEFAULT_COMPLEX_NAME))
            .jsonPath("$.[*].state")
            .value(hasItem(DEFAULT_STATE))
            .jsonPath("$.[*].county")
            .value(hasItem(DEFAULT_COUNTY))
            .jsonPath("$.[*].city")
            .value(hasItem(DEFAULT_CITY))
            .jsonPath("$.[*].town")
            .value(hasItem(DEFAULT_TOWN))
            .jsonPath("$.[*].addressCode")
            .value(hasItem(DEFAULT_ADDRESS_CODE))
            .jsonPath("$.[*].createdAt")
            .value(hasItem(DEFAULT_CREATED_AT.toString()))
            .jsonPath("$.[*].updatedAt")
            .value(hasItem(DEFAULT_UPDATED_AT.toString()));
    }

    @Test
    void getComplex() {
        // Initialize the database
        insertedComplex = complexRepository.save(complex).block();

        // Get the complex
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, complex.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(complex.getId().intValue()))
            .jsonPath("$.complexName")
            .value(is(DEFAULT_COMPLEX_NAME))
            .jsonPath("$.state")
            .value(is(DEFAULT_STATE))
            .jsonPath("$.county")
            .value(is(DEFAULT_COUNTY))
            .jsonPath("$.city")
            .value(is(DEFAULT_CITY))
            .jsonPath("$.town")
            .value(is(DEFAULT_TOWN))
            .jsonPath("$.addressCode")
            .value(is(DEFAULT_ADDRESS_CODE))
            .jsonPath("$.createdAt")
            .value(is(DEFAULT_CREATED_AT.toString()))
            .jsonPath("$.updatedAt")
            .value(is(DEFAULT_UPDATED_AT.toString()));
    }

    @Test
    void getNonExistingComplex() {
        // Get the complex
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingComplex() throws Exception {
        // Initialize the database
        insertedComplex = complexRepository.save(complex).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();
        complexSearchRepository.save(complex).block();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(complexSearchRepository.findAll().collectList().block());

        // Update the complex
        Complex updatedComplex = complexRepository.findById(complex.getId()).block();
        updatedComplex
            .complexName(UPDATED_COMPLEX_NAME)
            .state(UPDATED_STATE)
            .county(UPDATED_COUNTY)
            .city(UPDATED_CITY)
            .town(UPDATED_TOWN)
            .addressCode(UPDATED_ADDRESS_CODE)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedComplex.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(updatedComplex))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Complex in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedComplexToMatchAllProperties(updatedComplex);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(complexSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Complex> complexSearchList = Streamable.of(complexSearchRepository.findAll().collectList().block()).toList();
                Complex testComplexSearch = complexSearchList.get(searchDatabaseSizeAfter - 1);

                // Test fails because reactive api returns an empty object instead of null
                // assertComplexAllPropertiesEquals(testComplexSearch, updatedComplex);
                assertComplexUpdatableFieldsEquals(testComplexSearch, updatedComplex);
            });
    }

    @Test
    void putNonExistingComplex() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(complexSearchRepository.findAll().collectList().block());
        complex.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, complex.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(complex))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Complex in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(complexSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithIdMismatchComplex() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(complexSearchRepository.findAll().collectList().block());
        complex.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(complex))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Complex in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(complexSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithMissingIdPathParamComplex() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(complexSearchRepository.findAll().collectList().block());
        complex.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(complex))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Complex in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(complexSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void partialUpdateComplexWithPatch() throws Exception {
        // Initialize the database
        insertedComplex = complexRepository.save(complex).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the complex using partial update
        Complex partialUpdatedComplex = new Complex();
        partialUpdatedComplex.setId(complex.getId());

        partialUpdatedComplex.state(UPDATED_STATE).town(UPDATED_TOWN).addressCode(UPDATED_ADDRESS_CODE).updatedAt(UPDATED_UPDATED_AT);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedComplex.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedComplex))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Complex in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertComplexUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedComplex, complex), getPersistedComplex(complex));
    }

    @Test
    void fullUpdateComplexWithPatch() throws Exception {
        // Initialize the database
        insertedComplex = complexRepository.save(complex).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the complex using partial update
        Complex partialUpdatedComplex = new Complex();
        partialUpdatedComplex.setId(complex.getId());

        partialUpdatedComplex
            .complexName(UPDATED_COMPLEX_NAME)
            .state(UPDATED_STATE)
            .county(UPDATED_COUNTY)
            .city(UPDATED_CITY)
            .town(UPDATED_TOWN)
            .addressCode(UPDATED_ADDRESS_CODE)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedComplex.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedComplex))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Complex in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertComplexUpdatableFieldsEquals(partialUpdatedComplex, getPersistedComplex(partialUpdatedComplex));
    }

    @Test
    void patchNonExistingComplex() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(complexSearchRepository.findAll().collectList().block());
        complex.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, complex.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(complex))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Complex in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(complexSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithIdMismatchComplex() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(complexSearchRepository.findAll().collectList().block());
        complex.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(complex))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Complex in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(complexSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithMissingIdPathParamComplex() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(complexSearchRepository.findAll().collectList().block());
        complex.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(complex))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Complex in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(complexSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void deleteComplex() {
        // Initialize the database
        insertedComplex = complexRepository.save(complex).block();
        complexRepository.save(complex).block();
        complexSearchRepository.save(complex).block();

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(complexSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the complex
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, complex.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(complexSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    void searchComplex() {
        // Initialize the database
        insertedComplex = complexRepository.save(complex).block();
        complexSearchRepository.save(complex).block();

        // Search the complex
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + complex.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(complex.getId().intValue()))
            .jsonPath("$.[*].complexName")
            .value(hasItem(DEFAULT_COMPLEX_NAME))
            .jsonPath("$.[*].state")
            .value(hasItem(DEFAULT_STATE))
            .jsonPath("$.[*].county")
            .value(hasItem(DEFAULT_COUNTY))
            .jsonPath("$.[*].city")
            .value(hasItem(DEFAULT_CITY))
            .jsonPath("$.[*].town")
            .value(hasItem(DEFAULT_TOWN))
            .jsonPath("$.[*].addressCode")
            .value(hasItem(DEFAULT_ADDRESS_CODE))
            .jsonPath("$.[*].createdAt")
            .value(hasItem(DEFAULT_CREATED_AT.toString()))
            .jsonPath("$.[*].updatedAt")
            .value(hasItem(DEFAULT_UPDATED_AT.toString()));
    }

    protected long getRepositoryCount() {
        return complexRepository.count().block();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Complex getPersistedComplex(Complex complex) {
        return complexRepository.findById(complex.getId()).block();
    }

    protected void assertPersistedComplexToMatchAllProperties(Complex expectedComplex) {
        // Test fails because reactive api returns an empty object instead of null
        // assertComplexAllPropertiesEquals(expectedComplex, getPersistedComplex(expectedComplex));
        assertComplexUpdatableFieldsEquals(expectedComplex, getPersistedComplex(expectedComplex));
    }

    protected void assertPersistedComplexToMatchUpdatableProperties(Complex expectedComplex) {
        // Test fails because reactive api returns an empty object instead of null
        // assertComplexAllUpdatablePropertiesEquals(expectedComplex, getPersistedComplex(expectedComplex));
        assertComplexUpdatableFieldsEquals(expectedComplex, getPersistedComplex(expectedComplex));
    }
}
