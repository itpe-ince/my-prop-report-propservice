package com.dnc.mprs.propservice.web.rest;

import static com.dnc.mprs.propservice.domain.PropertyAsserts.*;
import static com.dnc.mprs.propservice.web.rest.TestUtil.createUpdateProxyForBean;
import static com.dnc.mprs.propservice.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.dnc.mprs.propservice.IntegrationTest;
import com.dnc.mprs.propservice.domain.Property;
import com.dnc.mprs.propservice.repository.PropertyRepository;
import com.dnc.mprs.propservice.repository.search.PropertySearchRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.util.Streamable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link PropertyResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class PropertyResourceIT {

    private static final Long DEFAULT_COMPLEX_ID = 1L;
    private static final Long UPDATED_COMPLEX_ID = 2L;

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
    private MockMvc restPropertyMockMvc;

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
            .complexId(DEFAULT_COMPLEX_ID)
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
            .complexId(UPDATED_COMPLEX_ID)
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

    @BeforeEach
    public void initTest() {
        property = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedProperty != null) {
            propertyRepository.delete(insertedProperty);
            propertySearchRepository.delete(insertedProperty);
            insertedProperty = null;
        }
    }

    @Test
    @Transactional
    void createProperty() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll());
        // Create the Property
        var returnedProperty = om.readValue(
            restPropertyMockMvc
                .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(property)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Property.class
        );

        // Validate the Property in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertPropertyUpdatableFieldsEquals(returnedProperty, getPersistedProperty(returnedProperty));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });

        insertedProperty = returnedProperty;
    }

    @Test
    @Transactional
    void createPropertyWithExistingId() throws Exception {
        // Create the Property with an existing ID
        property.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restPropertyMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(property)))
            .andExpect(status().isBadRequest());

        // Validate the Property in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkComplexIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll());
        // set the field null
        property.setComplexId(null);

        // Create the Property, which fails.

        restPropertyMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(property)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkAddressIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll());
        // set the field null
        property.setAddress(null);

        // Create the Property, which fails.

        restPropertyMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(property)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll());
        // set the field null
        property.setType(null);

        // Create the Property, which fails.

        restPropertyMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(property)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkAreaIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll());
        // set the field null
        property.setArea(null);

        // Create the Property, which fails.

        restPropertyMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(property)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkRoomsIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll());
        // set the field null
        property.setRooms(null);

        // Create the Property, which fails.

        restPropertyMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(property)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkBathroomsIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll());
        // set the field null
        property.setBathrooms(null);

        // Create the Property, which fails.

        restPropertyMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(property)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkBuildYearIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll());
        // set the field null
        property.setBuildYear(null);

        // Create the Property, which fails.

        restPropertyMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(property)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll());
        // set the field null
        property.setCreatedAt(null);

        // Create the Property, which fails.

        restPropertyMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(property)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllProperties() throws Exception {
        // Initialize the database
        insertedProperty = propertyRepository.saveAndFlush(property);

        // Get all the propertyList
        restPropertyMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(property.getId().intValue())))
            .andExpect(jsonPath("$.[*].complexId").value(hasItem(DEFAULT_COMPLEX_ID.intValue())))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)))
            .andExpect(jsonPath("$.[*].regionCd").value(hasItem(DEFAULT_REGION_CD)))
            .andExpect(jsonPath("$.[*].localName").value(hasItem(DEFAULT_LOCAL_NAME)))
            .andExpect(jsonPath("$.[*].street").value(hasItem(DEFAULT_STREET)))
            .andExpect(jsonPath("$.[*].floor").value(hasItem(DEFAULT_FLOOR)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE)))
            .andExpect(jsonPath("$.[*].area").value(hasItem(sameNumber(DEFAULT_AREA))))
            .andExpect(jsonPath("$.[*].rooms").value(hasItem(DEFAULT_ROOMS)))
            .andExpect(jsonPath("$.[*].bathrooms").value(hasItem(DEFAULT_BATHROOMS)))
            .andExpect(jsonPath("$.[*].buildYear").value(hasItem(DEFAULT_BUILD_YEAR)))
            .andExpect(jsonPath("$.[*].parkingYn").value(hasItem(DEFAULT_PARKING_YN)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));
    }

    @Test
    @Transactional
    void getProperty() throws Exception {
        // Initialize the database
        insertedProperty = propertyRepository.saveAndFlush(property);

        // Get the property
        restPropertyMockMvc
            .perform(get(ENTITY_API_URL_ID, property.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(property.getId().intValue()))
            .andExpect(jsonPath("$.complexId").value(DEFAULT_COMPLEX_ID.intValue()))
            .andExpect(jsonPath("$.address").value(DEFAULT_ADDRESS))
            .andExpect(jsonPath("$.regionCd").value(DEFAULT_REGION_CD))
            .andExpect(jsonPath("$.localName").value(DEFAULT_LOCAL_NAME))
            .andExpect(jsonPath("$.street").value(DEFAULT_STREET))
            .andExpect(jsonPath("$.floor").value(DEFAULT_FLOOR))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE))
            .andExpect(jsonPath("$.area").value(sameNumber(DEFAULT_AREA)))
            .andExpect(jsonPath("$.rooms").value(DEFAULT_ROOMS))
            .andExpect(jsonPath("$.bathrooms").value(DEFAULT_BATHROOMS))
            .andExpect(jsonPath("$.buildYear").value(DEFAULT_BUILD_YEAR))
            .andExpect(jsonPath("$.parkingYn").value(DEFAULT_PARKING_YN))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()))
            .andExpect(jsonPath("$.updatedAt").value(DEFAULT_UPDATED_AT.toString()));
    }

    @Test
    @Transactional
    void getNonExistingProperty() throws Exception {
        // Get the property
        restPropertyMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingProperty() throws Exception {
        // Initialize the database
        insertedProperty = propertyRepository.saveAndFlush(property);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        propertySearchRepository.save(property);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll());

        // Update the property
        Property updatedProperty = propertyRepository.findById(property.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedProperty are not directly saved in db
        em.detach(updatedProperty);
        updatedProperty
            .complexId(UPDATED_COMPLEX_ID)
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

        restPropertyMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedProperty.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedProperty))
            )
            .andExpect(status().isOk());

        // Validate the Property in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedPropertyToMatchAllProperties(updatedProperty);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Property> propertySearchList = Streamable.of(propertySearchRepository.findAll()).toList();
                Property testPropertySearch = propertySearchList.get(searchDatabaseSizeAfter - 1);

                assertPropertyAllPropertiesEquals(testPropertySearch, updatedProperty);
            });
    }

    @Test
    @Transactional
    void putNonExistingProperty() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll());
        property.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPropertyMockMvc
            .perform(
                put(ENTITY_API_URL_ID, property.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(property))
            )
            .andExpect(status().isBadRequest());

        // Validate the Property in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchProperty() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll());
        property.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPropertyMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(property))
            )
            .andExpect(status().isBadRequest());

        // Validate the Property in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamProperty() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll());
        property.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPropertyMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(property)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Property in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdatePropertyWithPatch() throws Exception {
        // Initialize the database
        insertedProperty = propertyRepository.saveAndFlush(property);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the property using partial update
        Property partialUpdatedProperty = new Property();
        partialUpdatedProperty.setId(property.getId());

        partialUpdatedProperty
            .complexId(UPDATED_COMPLEX_ID)
            .street(UPDATED_STREET)
            .area(UPDATED_AREA)
            .rooms(UPDATED_ROOMS)
            .bathrooms(UPDATED_BATHROOMS)
            .description(UPDATED_DESCRIPTION)
            .updatedAt(UPDATED_UPDATED_AT);

        restPropertyMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProperty.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedProperty))
            )
            .andExpect(status().isOk());

        // Validate the Property in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPropertyUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedProperty, property), getPersistedProperty(property));
    }

    @Test
    @Transactional
    void fullUpdatePropertyWithPatch() throws Exception {
        // Initialize the database
        insertedProperty = propertyRepository.saveAndFlush(property);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the property using partial update
        Property partialUpdatedProperty = new Property();
        partialUpdatedProperty.setId(property.getId());

        partialUpdatedProperty
            .complexId(UPDATED_COMPLEX_ID)
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

        restPropertyMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProperty.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedProperty))
            )
            .andExpect(status().isOk());

        // Validate the Property in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPropertyUpdatableFieldsEquals(partialUpdatedProperty, getPersistedProperty(partialUpdatedProperty));
    }

    @Test
    @Transactional
    void patchNonExistingProperty() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll());
        property.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPropertyMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, property.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(property))
            )
            .andExpect(status().isBadRequest());

        // Validate the Property in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchProperty() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll());
        property.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPropertyMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(property))
            )
            .andExpect(status().isBadRequest());

        // Validate the Property in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamProperty() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll());
        property.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPropertyMockMvc
            .perform(patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(property)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Property in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteProperty() throws Exception {
        // Initialize the database
        insertedProperty = propertyRepository.saveAndFlush(property);
        propertyRepository.save(property);
        propertySearchRepository.save(property);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(propertySearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the property
        restPropertyMockMvc
            .perform(delete(ENTITY_API_URL_ID, property.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(propertySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchProperty() throws Exception {
        // Initialize the database
        insertedProperty = propertyRepository.saveAndFlush(property);
        propertySearchRepository.save(property);

        // Search the property
        restPropertyMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + property.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(property.getId().intValue())))
            .andExpect(jsonPath("$.[*].complexId").value(hasItem(DEFAULT_COMPLEX_ID.intValue())))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)))
            .andExpect(jsonPath("$.[*].regionCd").value(hasItem(DEFAULT_REGION_CD)))
            .andExpect(jsonPath("$.[*].localName").value(hasItem(DEFAULT_LOCAL_NAME)))
            .andExpect(jsonPath("$.[*].street").value(hasItem(DEFAULT_STREET)))
            .andExpect(jsonPath("$.[*].floor").value(hasItem(DEFAULT_FLOOR)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE)))
            .andExpect(jsonPath("$.[*].area").value(hasItem(sameNumber(DEFAULT_AREA))))
            .andExpect(jsonPath("$.[*].rooms").value(hasItem(DEFAULT_ROOMS)))
            .andExpect(jsonPath("$.[*].bathrooms").value(hasItem(DEFAULT_BATHROOMS)))
            .andExpect(jsonPath("$.[*].buildYear").value(hasItem(DEFAULT_BUILD_YEAR)))
            .andExpect(jsonPath("$.[*].parkingYn").value(hasItem(DEFAULT_PARKING_YN)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));
    }

    protected long getRepositoryCount() {
        return propertyRepository.count();
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
        return propertyRepository.findById(property.getId()).orElseThrow();
    }

    protected void assertPersistedPropertyToMatchAllProperties(Property expectedProperty) {
        assertPropertyAllPropertiesEquals(expectedProperty, getPersistedProperty(expectedProperty));
    }

    protected void assertPersistedPropertyToMatchUpdatableProperties(Property expectedProperty) {
        assertPropertyAllUpdatablePropertiesEquals(expectedProperty, getPersistedProperty(expectedProperty));
    }
}
