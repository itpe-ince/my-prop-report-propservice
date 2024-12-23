package com.dnc.mprs.propservice.repository;

import com.dnc.mprs.propservice.domain.Complex;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Complex entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ComplexRepository extends ReactiveCrudRepository<Complex, Long>, ComplexRepositoryInternal {
    Flux<Complex> findAllBy(Pageable pageable);

    @Override
    <S extends Complex> Mono<S> save(S entity);

    @Override
    Flux<Complex> findAll();

    @Override
    Mono<Complex> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface ComplexRepositoryInternal {
    <S extends Complex> Mono<S> save(S entity);

    Flux<Complex> findAllBy(Pageable pageable);

    Flux<Complex> findAll();

    Mono<Complex> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Complex> findAllBy(Pageable pageable, Criteria criteria);
}
