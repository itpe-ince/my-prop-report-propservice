package com.dnc.mprs.propservice.web.rest;

import static com.dnc.mprs.propservice.domain.ComplexAsserts.*;
import static com.dnc.mprs.propservice.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.dnc.mprs.propservice.IntegrationTest;
import com.dnc.mprs.propservice.domain.Complex;
import com.dnc.mprs.propservice.repository.ComplexRepository;
import com.dnc.mprs.propservice.repository.search.ComplexSearchRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.util.Streamable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ComplexResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
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
    private MockMvc restComplexMockMvc;

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

    @BeforeEach
    public void initTest() {
        complex = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedComplex != null) {
            complexRepository.delete(insertedComplex);
            complexSearchRepository.delete(insertedComplex);
            insertedComplex = null;
        }
    }

    @Test
    @Transactional
    void createComplex() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(complexSearchRepository.findAll());
        // Create the Complex
        var returnedComplex = om.readValue(
            restComplexMockMvc
                .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(complex)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Complex.class
        );

        // Validate the Complex in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertComplexUpdatableFieldsEquals(returnedComplex, getPersistedComplex(returnedComplex));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(complexSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });

        insertedComplex = returnedComplex;
    }

    @Test
    @Transactional
    void createComplexWithExistingId() throws Exception {
        // Create the Complex with an existing ID
        complex.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(complexSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restComplexMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(complex)))
            .andExpect(status().isBadRequest());

        // Validate the Complex in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(complexSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkComplexNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(complexSearchRepository.findAll());
        // set the field null
        complex.setComplexName(null);

        // Create the Complex, which fails.

        restComplexMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(complex)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(complexSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(complexSearchRepository.findAll());
        // set the field null
        complex.setCreatedAt(null);

        // Create the Complex, which fails.

        restComplexMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(complex)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(complexSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllComplexes() throws Exception {
        // Initialize the database
        insertedComplex = complexRepository.saveAndFlush(complex);

        // Get all the complexList
        restComplexMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(complex.getId().intValue())))
            .andExpect(jsonPath("$.[*].complexName").value(hasItem(DEFAULT_COMPLEX_NAME)))
            .andExpect(jsonPath("$.[*].state").value(hasItem(DEFAULT_STATE)))
            .andExpect(jsonPath("$.[*].county").value(hasItem(DEFAULT_COUNTY)))
            .andExpect(jsonPath("$.[*].city").value(hasItem(DEFAULT_CITY)))
            .andExpect(jsonPath("$.[*].town").value(hasItem(DEFAULT_TOWN)))
            .andExpect(jsonPath("$.[*].addressCode").value(hasItem(DEFAULT_ADDRESS_CODE)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));
    }

    @Test
    @Transactional
    void getComplex() throws Exception {
        // Initialize the database
        insertedComplex = complexRepository.saveAndFlush(complex);

        // Get the complex
        restComplexMockMvc
            .perform(get(ENTITY_API_URL_ID, complex.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(complex.getId().intValue()))
            .andExpect(jsonPath("$.complexName").value(DEFAULT_COMPLEX_NAME))
            .andExpect(jsonPath("$.state").value(DEFAULT_STATE))
            .andExpect(jsonPath("$.county").value(DEFAULT_COUNTY))
            .andExpect(jsonPath("$.city").value(DEFAULT_CITY))
            .andExpect(jsonPath("$.town").value(DEFAULT_TOWN))
            .andExpect(jsonPath("$.addressCode").value(DEFAULT_ADDRESS_CODE))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()))
            .andExpect(jsonPath("$.updatedAt").value(DEFAULT_UPDATED_AT.toString()));
    }

    @Test
    @Transactional
    void getNonExistingComplex() throws Exception {
        // Get the complex
        restComplexMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingComplex() throws Exception {
        // Initialize the database
        insertedComplex = complexRepository.saveAndFlush(complex);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        complexSearchRepository.save(complex);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(complexSearchRepository.findAll());

        // Update the complex
        Complex updatedComplex = complexRepository.findById(complex.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedComplex are not directly saved in db
        em.detach(updatedComplex);
        updatedComplex
            .complexName(UPDATED_COMPLEX_NAME)
            .state(UPDATED_STATE)
            .county(UPDATED_COUNTY)
            .city(UPDATED_CITY)
            .town(UPDATED_TOWN)
            .addressCode(UPDATED_ADDRESS_CODE)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        restComplexMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedComplex.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedComplex))
            )
            .andExpect(status().isOk());

        // Validate the Complex in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedComplexToMatchAllProperties(updatedComplex);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(complexSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Complex> complexSearchList = Streamable.of(complexSearchRepository.findAll()).toList();
                Complex testComplexSearch = complexSearchList.get(searchDatabaseSizeAfter - 1);

                assertComplexAllPropertiesEquals(testComplexSearch, updatedComplex);
            });
    }

    @Test
    @Transactional
    void putNonExistingComplex() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(complexSearchRepository.findAll());
        complex.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restComplexMockMvc
            .perform(
                put(ENTITY_API_URL_ID, complex.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(complex))
            )
            .andExpect(status().isBadRequest());

        // Validate the Complex in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(complexSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchComplex() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(complexSearchRepository.findAll());
        complex.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restComplexMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(complex))
            )
            .andExpect(status().isBadRequest());

        // Validate the Complex in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(complexSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamComplex() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(complexSearchRepository.findAll());
        complex.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restComplexMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(complex)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Complex in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(complexSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateComplexWithPatch() throws Exception {
        // Initialize the database
        insertedComplex = complexRepository.saveAndFlush(complex);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the complex using partial update
        Complex partialUpdatedComplex = new Complex();
        partialUpdatedComplex.setId(complex.getId());

        partialUpdatedComplex.state(UPDATED_STATE).town(UPDATED_TOWN).addressCode(UPDATED_ADDRESS_CODE).updatedAt(UPDATED_UPDATED_AT);

        restComplexMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedComplex.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedComplex))
            )
            .andExpect(status().isOk());

        // Validate the Complex in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertComplexUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedComplex, complex), getPersistedComplex(complex));
    }

    @Test
    @Transactional
    void fullUpdateComplexWithPatch() throws Exception {
        // Initialize the database
        insertedComplex = complexRepository.saveAndFlush(complex);

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

        restComplexMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedComplex.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedComplex))
            )
            .andExpect(status().isOk());

        // Validate the Complex in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertComplexUpdatableFieldsEquals(partialUpdatedComplex, getPersistedComplex(partialUpdatedComplex));
    }

    @Test
    @Transactional
    void patchNonExistingComplex() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(complexSearchRepository.findAll());
        complex.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restComplexMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, complex.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(complex))
            )
            .andExpect(status().isBadRequest());

        // Validate the Complex in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(complexSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchComplex() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(complexSearchRepository.findAll());
        complex.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restComplexMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(complex))
            )
            .andExpect(status().isBadRequest());

        // Validate the Complex in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(complexSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamComplex() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(complexSearchRepository.findAll());
        complex.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restComplexMockMvc
            .perform(patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(complex)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Complex in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(complexSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteComplex() throws Exception {
        // Initialize the database
        insertedComplex = complexRepository.saveAndFlush(complex);
        complexRepository.save(complex);
        complexSearchRepository.save(complex);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(complexSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the complex
        restComplexMockMvc
            .perform(delete(ENTITY_API_URL_ID, complex.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(complexSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchComplex() throws Exception {
        // Initialize the database
        insertedComplex = complexRepository.saveAndFlush(complex);
        complexSearchRepository.save(complex);

        // Search the complex
        restComplexMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + complex.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(complex.getId().intValue())))
            .andExpect(jsonPath("$.[*].complexName").value(hasItem(DEFAULT_COMPLEX_NAME)))
            .andExpect(jsonPath("$.[*].state").value(hasItem(DEFAULT_STATE)))
            .andExpect(jsonPath("$.[*].county").value(hasItem(DEFAULT_COUNTY)))
            .andExpect(jsonPath("$.[*].city").value(hasItem(DEFAULT_CITY)))
            .andExpect(jsonPath("$.[*].town").value(hasItem(DEFAULT_TOWN)))
            .andExpect(jsonPath("$.[*].addressCode").value(hasItem(DEFAULT_ADDRESS_CODE)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));
    }

    protected long getRepositoryCount() {
        return complexRepository.count();
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
        return complexRepository.findById(complex.getId()).orElseThrow();
    }

    protected void assertPersistedComplexToMatchAllProperties(Complex expectedComplex) {
        assertComplexAllPropertiesEquals(expectedComplex, getPersistedComplex(expectedComplex));
    }

    protected void assertPersistedComplexToMatchUpdatableProperties(Complex expectedComplex) {
        assertComplexAllUpdatablePropertiesEquals(expectedComplex, getPersistedComplex(expectedComplex));
    }
}
