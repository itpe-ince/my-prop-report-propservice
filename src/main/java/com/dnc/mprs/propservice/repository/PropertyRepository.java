package com.dnc.mprs.propservice.repository;

import com.dnc.mprs.propservice.domain.Property;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Property entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PropertyRepository extends ReactiveCrudRepository<Property, Long>, PropertyRepositoryInternal {
    Flux<Property> findAllBy(Pageable pageable);

    @Query("SELECT * FROM property entity WHERE entity.complex_id = :id")
    Flux<Property> findByComplex(Long id);

    @Query("SELECT * FROM property entity WHERE entity.complex_id IS NULL")
    Flux<Property> findAllWhereComplexIsNull();

    @Override
    <S extends Property> Mono<S> save(S entity);

    @Override
    Flux<Property> findAll();

    @Override
    Mono<Property> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface PropertyRepositoryInternal {
    <S extends Property> Mono<S> save(S entity);

    Flux<Property> findAllBy(Pageable pageable);

    Flux<Property> findAll();

    Mono<Property> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Property> findAllBy(Pageable pageable, Criteria criteria);
}
