package com.dnc.mprs.propservice.web.rest;

import static com.dnc.mprs.propservice.domain.TransactionAsserts.*;
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
import com.dnc.mprs.propservice.domain.Transaction;
import com.dnc.mprs.propservice.repository.TransactionRepository;
import com.dnc.mprs.propservice.repository.search.TransactionSearchRepository;
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
 * Integration tests for the {@link TransactionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class TransactionResourceIT {

    private static final Long DEFAULT_PROPERTY_ID = 1L;
    private static final Long UPDATED_PROPERTY_ID = 2L;

    private static final String DEFAULT_TRANSACTION_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_TRANSACTION_TYPE = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_PRICE = new BigDecimal(1);
    private static final BigDecimal UPDATED_PRICE = new BigDecimal(2);

    private static final Instant DEFAULT_TRANSACTION_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_TRANSACTION_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_BUYER = "AAAAAAAAAA";
    private static final String UPDATED_BUYER = "BBBBBBBBBB";

    private static final String DEFAULT_SELLER = "AAAAAAAAAA";
    private static final String UPDATED_SELLER = "BBBBBBBBBB";

    private static final String DEFAULT_AGENT = "AAAAAAAAAA";
    private static final String UPDATED_AGENT = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_UPDATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_UPDATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/transactions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/transactions/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionSearchRepository transactionSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTransactionMockMvc;

    private Transaction transaction;

    private Transaction insertedTransaction;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Transaction createEntity() {
        return new Transaction()
            .propertyId(DEFAULT_PROPERTY_ID)
            .transactionType(DEFAULT_TRANSACTION_TYPE)
            .price(DEFAULT_PRICE)
            .transactionDate(DEFAULT_TRANSACTION_DATE)
            .buyer(DEFAULT_BUYER)
            .seller(DEFAULT_SELLER)
            .agent(DEFAULT_AGENT)
            .createdAt(DEFAULT_CREATED_AT)
            .updatedAt(DEFAULT_UPDATED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Transaction createUpdatedEntity() {
        return new Transaction()
            .propertyId(UPDATED_PROPERTY_ID)
            .transactionType(UPDATED_TRANSACTION_TYPE)
            .price(UPDATED_PRICE)
            .transactionDate(UPDATED_TRANSACTION_DATE)
            .buyer(UPDATED_BUYER)
            .seller(UPDATED_SELLER)
            .agent(UPDATED_AGENT)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
    }

    @BeforeEach
    public void initTest() {
        transaction = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedTransaction != null) {
            transactionRepository.delete(insertedTransaction);
            transactionSearchRepository.delete(insertedTransaction);
            insertedTransaction = null;
        }
    }

    @Test
    @Transactional
    void createTransaction() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionSearchRepository.findAll());
        // Create the Transaction
        var returnedTransaction = om.readValue(
            restTransactionMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(transaction))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Transaction.class
        );

        // Validate the Transaction in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertTransactionUpdatableFieldsEquals(returnedTransaction, getPersistedTransaction(returnedTransaction));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });

        insertedTransaction = returnedTransaction;
    }

    @Test
    @Transactional
    void createTransactionWithExistingId() throws Exception {
        // Create the Transaction with an existing ID
        transaction.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restTransactionMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(transaction)))
            .andExpect(status().isBadRequest());

        // Validate the Transaction in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkPropertyIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionSearchRepository.findAll());
        // set the field null
        transaction.setPropertyId(null);

        // Create the Transaction, which fails.

        restTransactionMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(transaction)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkTransactionTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionSearchRepository.findAll());
        // set the field null
        transaction.setTransactionType(null);

        // Create the Transaction, which fails.

        restTransactionMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(transaction)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkPriceIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionSearchRepository.findAll());
        // set the field null
        transaction.setPrice(null);

        // Create the Transaction, which fails.

        restTransactionMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(transaction)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkTransactionDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionSearchRepository.findAll());
        // set the field null
        transaction.setTransactionDate(null);

        // Create the Transaction, which fails.

        restTransactionMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(transaction)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionSearchRepository.findAll());
        // set the field null
        transaction.setCreatedAt(null);

        // Create the Transaction, which fails.

        restTransactionMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(transaction)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllTransactions() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList
        restTransactionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(transaction.getId().intValue())))
            .andExpect(jsonPath("$.[*].propertyId").value(hasItem(DEFAULT_PROPERTY_ID.intValue())))
            .andExpect(jsonPath("$.[*].transactionType").value(hasItem(DEFAULT_TRANSACTION_TYPE)))
            .andExpect(jsonPath("$.[*].price").value(hasItem(sameNumber(DEFAULT_PRICE))))
            .andExpect(jsonPath("$.[*].transactionDate").value(hasItem(DEFAULT_TRANSACTION_DATE.toString())))
            .andExpect(jsonPath("$.[*].buyer").value(hasItem(DEFAULT_BUYER)))
            .andExpect(jsonPath("$.[*].seller").value(hasItem(DEFAULT_SELLER)))
            .andExpect(jsonPath("$.[*].agent").value(hasItem(DEFAULT_AGENT)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));
    }

    @Test
    @Transactional
    void getTransaction() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);

        // Get the transaction
        restTransactionMockMvc
            .perform(get(ENTITY_API_URL_ID, transaction.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(transaction.getId().intValue()))
            .andExpect(jsonPath("$.propertyId").value(DEFAULT_PROPERTY_ID.intValue()))
            .andExpect(jsonPath("$.transactionType").value(DEFAULT_TRANSACTION_TYPE))
            .andExpect(jsonPath("$.price").value(sameNumber(DEFAULT_PRICE)))
            .andExpect(jsonPath("$.transactionDate").value(DEFAULT_TRANSACTION_DATE.toString()))
            .andExpect(jsonPath("$.buyer").value(DEFAULT_BUYER))
            .andExpect(jsonPath("$.seller").value(DEFAULT_SELLER))
            .andExpect(jsonPath("$.agent").value(DEFAULT_AGENT))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()))
            .andExpect(jsonPath("$.updatedAt").value(DEFAULT_UPDATED_AT.toString()));
    }

    @Test
    @Transactional
    void getNonExistingTransaction() throws Exception {
        // Get the transaction
        restTransactionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingTransaction() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        transactionSearchRepository.save(transaction);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionSearchRepository.findAll());

        // Update the transaction
        Transaction updatedTransaction = transactionRepository.findById(transaction.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedTransaction are not directly saved in db
        em.detach(updatedTransaction);
        updatedTransaction
            .propertyId(UPDATED_PROPERTY_ID)
            .transactionType(UPDATED_TRANSACTION_TYPE)
            .price(UPDATED_PRICE)
            .transactionDate(UPDATED_TRANSACTION_DATE)
            .buyer(UPDATED_BUYER)
            .seller(UPDATED_SELLER)
            .agent(UPDATED_AGENT)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        restTransactionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedTransaction.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedTransaction))
            )
            .andExpect(status().isOk());

        // Validate the Transaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedTransactionToMatchAllProperties(updatedTransaction);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Transaction> transactionSearchList = Streamable.of(transactionSearchRepository.findAll()).toList();
                Transaction testTransactionSearch = transactionSearchList.get(searchDatabaseSizeAfter - 1);

                assertTransactionAllPropertiesEquals(testTransactionSearch, updatedTransaction);
            });
    }

    @Test
    @Transactional
    void putNonExistingTransaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionSearchRepository.findAll());
        transaction.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTransactionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, transaction.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(transaction))
            )
            .andExpect(status().isBadRequest());

        // Validate the Transaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchTransaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionSearchRepository.findAll());
        transaction.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTransactionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(transaction))
            )
            .andExpect(status().isBadRequest());

        // Validate the Transaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTransaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionSearchRepository.findAll());
        transaction.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTransactionMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(transaction)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Transaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateTransactionWithPatch() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the transaction using partial update
        Transaction partialUpdatedTransaction = new Transaction();
        partialUpdatedTransaction.setId(transaction.getId());

        partialUpdatedTransaction.price(UPDATED_PRICE).transactionDate(UPDATED_TRANSACTION_DATE).createdAt(UPDATED_CREATED_AT);

        restTransactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTransaction.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTransaction))
            )
            .andExpect(status().isOk());

        // Validate the Transaction in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTransactionUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedTransaction, transaction),
            getPersistedTransaction(transaction)
        );
    }

    @Test
    @Transactional
    void fullUpdateTransactionWithPatch() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the transaction using partial update
        Transaction partialUpdatedTransaction = new Transaction();
        partialUpdatedTransaction.setId(transaction.getId());

        partialUpdatedTransaction
            .propertyId(UPDATED_PROPERTY_ID)
            .transactionType(UPDATED_TRANSACTION_TYPE)
            .price(UPDATED_PRICE)
            .transactionDate(UPDATED_TRANSACTION_DATE)
            .buyer(UPDATED_BUYER)
            .seller(UPDATED_SELLER)
            .agent(UPDATED_AGENT)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        restTransactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTransaction.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTransaction))
            )
            .andExpect(status().isOk());

        // Validate the Transaction in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTransactionUpdatableFieldsEquals(partialUpdatedTransaction, getPersistedTransaction(partialUpdatedTransaction));
    }

    @Test
    @Transactional
    void patchNonExistingTransaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionSearchRepository.findAll());
        transaction.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTransactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, transaction.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(transaction))
            )
            .andExpect(status().isBadRequest());

        // Validate the Transaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTransaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionSearchRepository.findAll());
        transaction.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTransactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(transaction))
            )
            .andExpect(status().isBadRequest());

        // Validate the Transaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTransaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionSearchRepository.findAll());
        transaction.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTransactionMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(transaction))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Transaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteTransaction() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);
        transactionRepository.save(transaction);
        transactionSearchRepository.save(transaction);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the transaction
        restTransactionMockMvc
            .perform(delete(ENTITY_API_URL_ID, transaction.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchTransaction() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);
        transactionSearchRepository.save(transaction);

        // Search the transaction
        restTransactionMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + transaction.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(transaction.getId().intValue())))
            .andExpect(jsonPath("$.[*].propertyId").value(hasItem(DEFAULT_PROPERTY_ID.intValue())))
            .andExpect(jsonPath("$.[*].transactionType").value(hasItem(DEFAULT_TRANSACTION_TYPE)))
            .andExpect(jsonPath("$.[*].price").value(hasItem(sameNumber(DEFAULT_PRICE))))
            .andExpect(jsonPath("$.[*].transactionDate").value(hasItem(DEFAULT_TRANSACTION_DATE.toString())))
            .andExpect(jsonPath("$.[*].buyer").value(hasItem(DEFAULT_BUYER)))
            .andExpect(jsonPath("$.[*].seller").value(hasItem(DEFAULT_SELLER)))
            .andExpect(jsonPath("$.[*].agent").value(hasItem(DEFAULT_AGENT)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));
    }

    protected long getRepositoryCount() {
        return transactionRepository.count();
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

    protected Transaction getPersistedTransaction(Transaction transaction) {
        return transactionRepository.findById(transaction.getId()).orElseThrow();
    }

    protected void assertPersistedTransactionToMatchAllProperties(Transaction expectedTransaction) {
        assertTransactionAllPropertiesEquals(expectedTransaction, getPersistedTransaction(expectedTransaction));
    }

    protected void assertPersistedTransactionToMatchUpdatableProperties(Transaction expectedTransaction) {
        assertTransactionAllUpdatablePropertiesEquals(expectedTransaction, getPersistedTransaction(expectedTransaction));
    }
}
