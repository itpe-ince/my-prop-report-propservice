package com.dnc.mprs.propservice.web.rest;

import static com.dnc.mprs.propservice.domain.PropertyAsserts.*;
import static com.dnc.mprs.propservice.web.rest.TestUtil.createUpdateProxyForBean;
import static com.dnc.mprs.propservice.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.dnc.mprs.propservice.IntegrationTest;
import com.dnc.mprs.propservice.domain.Property;
import com.dnc.mprs.propservice.repository.EntityManager;
import com.dnc.mprs.propservice.repository.PropertyRepository;
import com.dnc.mprs.propservice.repository.search.PropertySearchRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
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
 * Integration tests for the {@link PropertyResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class PropertyResourceIT {

    private static final String DEFAULT_ADDRESS = "AAAAAAAAAA";
    private static final String UPDATED_ADDRESS = "BBBBBBBBBB";

    private static final String DEFAULT_REGION_CD = "AAAAAAAAAA";
    private static final String UPDATED_REGION_CD = "BBBBBBBBBB";

    private static final String DEFAULT_LOCAL_NAME = "AAAAAAAAAA";
    private static final String UPDATED_LOCAL_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_STREET = "AAAAAAAAAA";
    private static final String UPDATED_STREET = "BBBBBBBBBB";

    private static final Integer DEFAULT_FLOOR = 1;
    private static final Integer UPDATED_FLOOR = 2;

    private static final String DEFAULT_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_TYPE = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_AREA = new BigDecimal(1);
    private static final BigDecimal UPDATED_AREA = new BigDecimal(2);

    private static final Integer DEFAULT_ROOMS = 1;
    private static final Integer UPDATED_ROOMS = 2;

    private static final Integer DEFAULT_BATHROOMS = 1;
    private static final Integer UPDATED_BATHROOMS = 2;

    private static final Integer DEFAULT_BUILD_YEAR = 1;
    private static final Integer UPDATED_BUILD_YEAR = 2;

    private static final String DEFAULT_PARKING_YN = "A";
    private static final String UPDATED_PARKING_YN = "B";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_UPDATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_UPDATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/properties";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/properties/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private PropertySearchRepository propertySearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Property property;

    private Property insertedProperty;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Property createEntity() {
        return new Property()
            .address(DEFAULT_ADDRESS)
            .regionCd(DEFAULT_REGION_CD)
            .localName(DEFAULT_LOCAL_NAME)
            .street(DEFAULT_STREET)
            .floor(DEFAULT_FLOOR)
            .type(DEFAULT_TYPE)
            .area(DEFAULT_AREA)
            .rooms(DEFAULT_ROOMS)
            .bathrooms(DEFAULT_BATHROOMS)
            .buildYear(DEFAULT_BUILD_YEAR)
            .parkingYn(DEFAULT_PARKING_YN)
            .description(DEFAULT_DESCRIPTION)
            .createdAt(DEFAULT_CREATED_AT)
            .updatedAt(DEFAULT_UPDATED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Property createUpdatedEntity() {
        return new Property()
            .address(UPDATED_ADDRESS)
            .regionCd(UPDATED_REGION_CD)
            .localName(UPDATED_LOCAL_NAME)
            .street(UPDATED_STREET)
            .floor(UPDATED_FLOOR)
            .type(UPDATED_TYPE)
            .area(UPDATED_AREA)
            .rooms(UPDATED_ROOMS)
            .bathrooms(UPDATED_BATHROOMS)
            .buildYear(UPDATED_BUILD_YEAR)
            .parkingYn(UPDATED_PARKING_YN)
            .description(UPDATED_DESCRIPTION)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Property.class).block();
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
        property = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedProperty != null) {
            propertyRepository.delete(insertedProperty).block();
            propertySearchRepository.delete(insertedProperty).block();
            insertedProperty = null;
        }
        deleteEntities(em);
    }

    @Test
    void createProperty() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
        // Create the Property
        var returnedProperty = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(property))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(Property.class)
            .returnResult()
            .getResponseBody();

        // Validate the Property in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertPropertyUpdatableFieldsEquals(returnedProperty, getPersistedProperty(returnedProperty));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });

        insertedProperty = returnedProperty;
    }

    @Test
    void createPropertyWithExistingId() throws Exception {
        // Create the Property with an existing ID
        property.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(property))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Property in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkAddressIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
        // set the field null
        property.setAddress(null);

        // Create the Property, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(property))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
        // set the field null
        property.setType(null);

        // Create the Property, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(property))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkAreaIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
        // set the field null
        property.setArea(null);

        // Create the Property, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(property))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkRoomsIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
        // set the field null
        property.setRooms(null);

        // Create the Property, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(property))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkBathroomsIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
        // set the field null
        property.setBathrooms(null);

        // Create the Property, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(property))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkBuildYearIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
        // set the field null
        property.setBuildYear(null);

        // Create the Property, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(property))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
        // set the field null
        property.setCreatedAt(null);

        // Create the Property, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(property))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void getAllProperties() {
        // Initialize the database
        insertedProperty = propertyRepository.save(property).block();

        // Get all the propertyList
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
            .value(hasItem(property.getId().intValue()))
            .jsonPath("$.[*].address")
            .value(hasItem(DEFAULT_ADDRESS))
            .jsonPath("$.[*].regionCd")
            .value(hasItem(DEFAULT_REGION_CD))
            .jsonPath("$.[*].localName")
            .value(hasItem(DEFAULT_LOCAL_NAME))
            .jsonPath("$.[*].street")
            .value(hasItem(DEFAULT_STREET))
            .jsonPath("$.[*].floor")
            .value(hasItem(DEFAULT_FLOOR))
            .jsonPath("$.[*].type")
            .value(hasItem(DEFAULT_TYPE))
            .jsonPath("$.[*].area")
            .value(hasItem(sameNumber(DEFAULT_AREA)))
            .jsonPath("$.[*].rooms")
            .value(hasItem(DEFAULT_ROOMS))
            .jsonPath("$.[*].bathrooms")
            .value(hasItem(DEFAULT_BATHROOMS))
            .jsonPath("$.[*].buildYear")
            .value(hasItem(DEFAULT_BUILD_YEAR))
            .jsonPath("$.[*].parkingYn")
            .value(hasItem(DEFAULT_PARKING_YN))
            .jsonPath("$.[*].description")
            .value(hasItem(DEFAULT_DESCRIPTION))
            .jsonPath("$.[*].createdAt")
            .value(hasItem(DEFAULT_CREATED_AT.toString()))
            .jsonPath("$.[*].updatedAt")
            .value(hasItem(DEFAULT_UPDATED_AT.toString()));
    }

    @Test
    void getProperty() {
        // Initialize the database
        insertedProperty = propertyRepository.save(property).block();

        // Get the property
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, property.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(property.getId().intValue()))
            .jsonPath("$.address")
            .value(is(DEFAULT_ADDRESS))
            .jsonPath("$.regionCd")
            .value(is(DEFAULT_REGION_CD))
            .jsonPath("$.localName")
            .value(is(DEFAULT_LOCAL_NAME))
            .jsonPath("$.street")
            .value(is(DEFAULT_STREET))
            .jsonPath("$.floor")
            .value(is(DEFAULT_FLOOR))
            .jsonPath("$.type")
            .value(is(DEFAULT_TYPE))
            .jsonPath("$.area")
            .value(is(sameNumber(DEFAULT_AREA)))
            .jsonPath("$.rooms")
            .value(is(DEFAULT_ROOMS))
            .jsonPath("$.bathrooms")
            .value(is(DEFAULT_BATHROOMS))
            .jsonPath("$.buildYear")
            .value(is(DEFAULT_BUILD_YEAR))
            .jsonPath("$.parkingYn")
            .value(is(DEFAULT_PARKING_YN))
            .jsonPath("$.description")
            .value(is(DEFAULT_DESCRIPTION))
            .jsonPath("$.createdAt")
            .value(is(DEFAULT_CREATED_AT.toString()))
            .jsonPath("$.updatedAt")
            .value(is(DEFAULT_UPDATED_AT.toString()));
    }

    @Test
    void getNonExistingProperty() {
        // Get the property
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingProperty() throws Exception {
        // Initialize the database
        insertedProperty = propertyRepository.save(property).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();
        propertySearchRepository.save(property).block();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());

        // Update the property
        Property updatedProperty = propertyRepository.findById(property.getId()).block();
        updatedProperty
            .address(UPDATED_ADDRESS)
            .regionCd(UPDATED_REGION_CD)
            .localName(UPDATED_LOCAL_NAME)
            .street(UPDATED_STREET)
            .floor(UPDATED_FLOOR)
            .type(UPDATED_TYPE)
            .area(UPDATED_AREA)
            .rooms(UPDATED_ROOMS)
            .bathrooms(UPDATED_BATHROOMS)
            .buildYear(UPDATED_BUILD_YEAR)
            .parkingYn(UPDATED_PARKING_YN)
            .description(UPDATED_DESCRIPTION)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedProperty.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(updatedProperty))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Property in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedPropertyToMatchAllProperties(updatedProperty);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Property> propertySearchList = Streamable.of(propertySearchRepository.findAll().collectList().block()).toList();
                Property testPropertySearch = propertySearchList.get(searchDatabaseSizeAfter - 1);

                // Test fails because reactive api returns an empty object instead of null
                // assertPropertyAllPropertiesEquals(testPropertySearch, updatedProperty);
                assertPropertyUpdatableFieldsEquals(testPropertySearch, updatedProperty);
            });
    }

    @Test
    void putNonExistingProperty() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
        property.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, property.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(property))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Property in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithIdMismatchProperty() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
        property.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(property))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Property in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithMissingIdPathParamProperty() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
        property.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(property))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Property in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void partialUpdatePropertyWithPatch() throws Exception {
        // Initialize the database
        insertedProperty = propertyRepository.save(property).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the property using partial update
        Property partialUpdatedProperty = new Property();
        partialUpdatedProperty.setId(property.getId());

        partialUpdatedProperty
            .address(UPDATED_ADDRESS)
            .floor(UPDATED_FLOOR)
            .rooms(UPDATED_ROOMS)
            .bathrooms(UPDATED_BATHROOMS)
            .buildYear(UPDATED_BUILD_YEAR)
            .createdAt(UPDATED_CREATED_AT);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedProperty.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedProperty))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Property in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPropertyUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedProperty, property), getPersistedProperty(property));
    }

    @Test
    void fullUpdatePropertyWithPatch() throws Exception {
        // Initialize the database
        insertedProperty = propertyRepository.save(property).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the property using partial update
        Property partialUpdatedProperty = new Property();
        partialUpdatedProperty.setId(property.getId());

        partialUpdatedProperty
            .address(UPDATED_ADDRESS)
            .regionCd(UPDATED_REGION_CD)
            .localName(UPDATED_LOCAL_NAME)
            .street(UPDATED_STREET)
            .floor(UPDATED_FLOOR)
            .type(UPDATED_TYPE)
            .area(UPDATED_AREA)
            .rooms(UPDATED_ROOMS)
            .bathrooms(UPDATED_BATHROOMS)
            .buildYear(UPDATED_BUILD_YEAR)
            .parkingYn(UPDATED_PARKING_YN)
            .description(UPDATED_DESCRIPTION)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedProperty.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedProperty))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Property in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPropertyUpdatableFieldsEquals(partialUpdatedProperty, getPersistedProperty(partialUpdatedProperty));
    }

    @Test
    void patchNonExistingProperty() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
        property.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, property.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(property))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Property in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithIdMismatchProperty() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
        property.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(property))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Property in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithMissingIdPathParamProperty() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
        property.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(property))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Property in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void deleteProperty() {
        // Initialize the database
        insertedProperty = propertyRepository.save(property).block();
        propertyRepository.save(property).block();
        propertySearchRepository.save(property).block();

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the property
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, property.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    void searchProperty() {
        // Initialize the database
        insertedProperty = propertyRepository.save(property).block();
        propertySearchRepository.save(property).block();

        // Search the property
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + property.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(property.getId().intValue()))
            .jsonPath("$.[*].address")
            .value(hasItem(DEFAULT_ADDRESS))
            .jsonPath("$.[*].regionCd")
            .value(hasItem(DEFAULT_REGION_CD))
            .jsonPath("$.[*].localName")
            .value(hasItem(DEFAULT_LOCAL_NAME))
            .jsonPath("$.[*].street")
            .value(hasItem(DEFAULT_STREET))
            .jsonPath("$.[*].floor")
            .value(hasItem(DEFAULT_FLOOR))
            .jsonPath("$.[*].type")
            .value(hasItem(DEFAULT_TYPE))
            .jsonPath("$.[*].area")
            .value(hasItem(sameNumber(DEFAULT_AREA)))
            .jsonPath("$.[*].rooms")
            .value(hasItem(DEFAULT_ROOMS))
            .jsonPath("$.[*].bathrooms")
            .value(hasItem(DEFAULT_BATHROOMS))
            .jsonPath("$.[*].buildYear")
            .value(hasItem(DEFAULT_BUILD_YEAR))
            .jsonPath("$.[*].parkingYn")
            .value(hasItem(DEFAULT_PARKING_YN))
            .jsonPath("$.[*].description")
            .value(hasItem(DEFAULT_DESCRIPTION))
            .jsonPath("$.[*].createdAt")
            .value(hasItem(DEFAULT_CREATED_AT.toString()))
            .jsonPath("$.[*].updatedAt")
            .value(hasItem(DEFAULT_UPDATED_AT.toString()));
    }

    protected long getRepositoryCount() {
        return propertyRepository.count().block();
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

    protected Property getPersistedProperty(Property property) {
        return propertyRepository.findById(property.getId()).block();
    }

    protected void assertPersistedPropertyToMatchAllProperties(Property expectedProperty) {
        // Test fails because reactive api returns an empty object instead of null
        // assertPropertyAllPropertiesEquals(expectedProperty, getPersistedProperty(expectedProperty));
        assertPropertyUpdatableFieldsEquals(expectedProperty, getPersistedProperty(expectedProperty));
    }

    protected void assertPersistedPropertyToMatchUpdatableProperties(Property expectedProperty) {
        // Test fails because reactive api returns an empty object instead of null
        // assertPropertyAllUpdatablePropertiesEquals(expectedProperty, getPersistedProperty(expectedProperty));
        assertPropertyUpdatableFieldsEquals(expectedProperty, getPersistedProperty(expectedProperty));
    }
}
