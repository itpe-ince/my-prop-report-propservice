package com.dnc.mprs.propservice.service;

import com.dnc.mprs.propservice.domain.Transaction;
import com.dnc.mprs.propservice.repository.TransactionRepository;
import com.dnc.mprs.propservice.repository.search.TransactionSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link com.dnc.mprs.propservice.domain.Transaction}.
 */
@Service
@Transactional
public class TransactionService {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;

    private final TransactionSearchRepository transactionSearchRepository;

    public TransactionService(TransactionRepository transactionRepository, TransactionSearchRepository transactionSearchRepository) {
        this.transactionRepository = transactionRepository;
        this.transactionSearchRepository = transactionSearchRepository;
    }

    /**
     * Save a transaction.
     *
     * @param transaction the entity to save.
     * @return the persisted entity.
     */
    public Mono<Transaction> save(Transaction transaction) {
        LOG.debug("Request to save Transaction : {}", transaction);
        return transactionRepository.save(transaction).flatMap(transactionSearchRepository::save);
    }

    /**
     * Update a transaction.
     *
     * @param transaction the entity to save.
     * @return the persisted entity.
     */
    public Mono<Transaction> update(Transaction transaction) {
        LOG.debug("Request to update Transaction : {}", transaction);
        return transactionRepository.save(transaction).flatMap(transactionSearchRepository::save);
    }

    /**
     * Partially update a transaction.
     *
     * @param transaction the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<Transaction> partialUpdate(Transaction transaction) {
        LOG.debug("Request to partially update Transaction : {}", transaction);

        return transactionRepository
            .findById(transaction.getId())
            .map(existingTransaction -> {
                if (transaction.getPropertyId() != null) {
                    existingTransaction.setPropertyId(transaction.getPropertyId());
                }
                if (transaction.getTransactionType() != null) {
                    existingTransaction.setTransactionType(transaction.getTransactionType());
                }
                if (transaction.getPrice() != null) {
                    existingTransaction.setPrice(transaction.getPrice());
                }
                if (transaction.getTransactionDate() != null) {
                    existingTransaction.setTransactionDate(transaction.getTransactionDate());
                }
                if (transaction.getBuyer() != null) {
                    existingTransaction.setBuyer(transaction.getBuyer());
                }
                if (transaction.getSeller() != null) {
                    existingTransaction.setSeller(transaction.getSeller());
                }
                if (transaction.getAgent() != null) {
                    existingTransaction.setAgent(transaction.getAgent());
                }
                if (transaction.getCreatedAt() != null) {
                    existingTransaction.setCreatedAt(transaction.getCreatedAt());
                }
                if (transaction.getUpdatedAt() != null) {
                    existingTransaction.setUpdatedAt(transaction.getUpdatedAt());
                }

                return existingTransaction;
            })
            .flatMap(transactionRepository::save)
            .flatMap(savedTransaction -> {
                transactionSearchRepository.save(savedTransaction);
                return Mono.just(savedTransaction);
            });
    }

    /**
     * Get all the transactions.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Flux<Transaction> findAll(Pageable pageable) {
        LOG.debug("Request to get all Transactions");
        return transactionRepository.findAllBy(pageable);
    }

    /**
     * Returns the number of transactions available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return transactionRepository.count();
    }

    /**
     * Returns the number of transactions available in search repository.
     *
     */
    public Mono<Long> searchCount() {
        return transactionSearchRepository.count();
    }

    /**
     * Get one transaction by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Mono<Transaction> findOne(Long id) {
        LOG.debug("Request to get Transaction : {}", id);
        return transactionRepository.findById(id);
    }

    /**
     * Delete the transaction by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(Long id) {
        LOG.debug("Request to delete Transaction : {}", id);
        return transactionRepository.deleteById(id).then(transactionSearchRepository.deleteById(id));
    }

    /**
     * Search for the transaction corresponding to the query.
     *
     * @param query the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Flux<Transaction> search(String query, Pageable pageable) {
        LOG.debug("Request to search for a page of Transactions for query {}", query);
        return transactionSearchRepository.search(query, pageable);
    }
}
