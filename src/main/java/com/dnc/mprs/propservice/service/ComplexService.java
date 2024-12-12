package com.dnc.mprs.propservice.service;

import com.dnc.mprs.propservice.domain.Complex;
import com.dnc.mprs.propservice.repository.ComplexRepository;
import com.dnc.mprs.propservice.repository.search.ComplexSearchRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Complex save(Complex complex) {
        LOG.debug("Request to save Complex : {}", complex);
        complex = complexRepository.save(complex);
        complexSearchRepository.index(complex);
        return complex;
    }

    /**
     * Update a complex.
     *
     * @param complex the entity to save.
     * @return the persisted entity.
     */
    public Complex update(Complex complex) {
        LOG.debug("Request to update Complex : {}", complex);
        complex = complexRepository.save(complex);
        complexSearchRepository.index(complex);
        return complex;
    }

    /**
     * Partially update a complex.
     *
     * @param complex the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<Complex> partialUpdate(Complex complex) {
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
            .map(complexRepository::save)
            .map(savedComplex -> {
                complexSearchRepository.index(savedComplex);
                return savedComplex;
            });
    }

    /**
     * Get all the complexes.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<Complex> findAll(Pageable pageable) {
        LOG.debug("Request to get all Complexes");
        return complexRepository.findAll(pageable);
    }

    /**
     * Get one complex by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<Complex> findOne(Long id) {
        LOG.debug("Request to get Complex : {}", id);
        return complexRepository.findById(id);
    }

    /**
     * Delete the complex by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Complex : {}", id);
        complexRepository.deleteById(id);
        complexSearchRepository.deleteFromIndexById(id);
    }

    /**
     * Search for the complex corresponding to the query.
     *
     * @param query the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<Complex> search(String query, Pageable pageable) {
        LOG.debug("Request to search for a page of Complexes for query {}", query);
        return complexSearchRepository.search(query, pageable);
    }
}
