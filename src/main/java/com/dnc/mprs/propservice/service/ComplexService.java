package com.dnc.mprs.propservice.service;

import com.dnc.mprs.propservice.domain.Complex;
import com.dnc.mprs.propservice.repository.ComplexRepository;
import com.dnc.mprs.propservice.repository.search.ComplexSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link com.dnc.mprs.propservice.domain.Complex}.
 */
@Service
@Transactional
public class ComplexService {

    private static final Logger LOG = LoggerFactory.getLogger(ComplexService.class);

    private final ComplexRepository complexRepository;

    private final ComplexSearchRepository complexSearchRepository;

    public ComplexService(ComplexRepository complexRepository, ComplexSearchRepository complexSearchRepository) {
        this.complexRepository = complexRepository;
        this.complexSearchRepository = complexSearchRepository;
    }

    /**
     * Save a complex.
     *
     * @param complex the entity to save.
     * @return the persisted entity.
     */
    public Mono<Complex> save(Complex complex) {
        LOG.debug("Request to save Complex : {}", complex);
        return complexRepository.save(complex).flatMap(complexSearchRepository::save);
    }

    /**
     * Update a complex.
     *
     * @param complex the entity to save.
     * @return the persisted entity.
     */
    public Mono<Complex> update(Complex complex) {
        LOG.debug("Request to update Complex : {}", complex);
        return complexRepository.save(complex).flatMap(complexSearchRepository::save);
    }

    /**
     * Partially update a complex.
     *
     * @param complex the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<Complex> partialUpdate(Complex complex) {
        LOG.debug("Request to partially update Complex : {}", complex);

        return complexRepository
            .findById(complex.getId())
            .map(existingComplex -> {
                if (complex.getComplexName() != null) {
                    existingComplex.setComplexName(complex.getComplexName());
                }
                if (complex.getState() != null) {
                    existingComplex.setState(complex.getState());
                }
                if (complex.getCounty() != null) {
                    existingComplex.setCounty(complex.getCounty());
                }
                if (complex.getCity() != null) {
                    existingComplex.setCity(complex.getCity());
                }
                if (complex.getTown() != null) {
                    existingComplex.setTown(complex.getTown());
                }
                if (complex.getAddressCode() != null) {
                    existingComplex.setAddressCode(complex.getAddressCode());
                }
                if (complex.getCreatedAt() != null) {
                    existingComplex.setCreatedAt(complex.getCreatedAt());
                }
                if (complex.getUpdatedAt() != null) {
                    existingComplex.setUpdatedAt(complex.getUpdatedAt());
                }

                return existingComplex;
            })
            .flatMap(complexRepository::save)
            .flatMap(savedComplex -> {
                complexSearchRepository.save(savedComplex);
                return Mono.just(savedComplex);
            });
    }

    /**
     * Get all the complexes.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Flux<Complex> findAll(Pageable pageable) {
        LOG.debug("Request to get all Complexes");
        return complexRepository.findAllBy(pageable);
    }

    /**
     * Returns the number of complexes available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return complexRepository.count();
    }

    /**
     * Returns the number of complexes available in search repository.
     *
     */
    public Mono<Long> searchCount() {
        return complexSearchRepository.count();
    }

    /**
     * Get one complex by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Mono<Complex> findOne(Long id) {
        LOG.debug("Request to get Complex : {}", id);
        return complexRepository.findById(id);
    }

    /**
     * Delete the complex by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(Long id) {
        LOG.debug("Request to delete Complex : {}", id);
        return complexRepository.deleteById(id).then(complexSearchRepository.deleteById(id));
    }

    /**
     * Search for the complex corresponding to the query.
     *
     * @param query the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Flux<Complex> search(String query, Pageable pageable) {
        LOG.debug("Request to search for a page of Complexes for query {}", query);
        return complexSearchRepository.search(query, pageable);
    }
}
