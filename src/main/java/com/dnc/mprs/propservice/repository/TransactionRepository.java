package com.dnc.mprs.propservice.repository;

import com.dnc.mprs.propservice.domain.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Transaction entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TransactionRepository extends ReactiveCrudRepository<Transaction, Long>, TransactionRepositoryInternal {
    Flux<Transaction> findAllBy(Pageable pageable);

    @Override
    <S extends Transaction> Mono<S> save(S entity);

    @Override
    Flux<Transaction> findAll();

    @Override
    Mono<Transaction> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface TransactionRepositoryInternal {
    <S extends Transaction> Mono<S> save(S entity);

    Flux<Transaction> findAllBy(Pageable pageable);

    Flux<Transaction> findAll();

    Mono<Transaction> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Transaction> findAllBy(Pageable pageable, Criteria criteria);
}
