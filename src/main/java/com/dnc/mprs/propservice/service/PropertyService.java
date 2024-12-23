package com.dnc.mprs.propservice.service;

import com.dnc.mprs.propservice.domain.Property;
import com.dnc.mprs.propservice.repository.PropertyRepository;
import com.dnc.mprs.propservice.repository.search.PropertySearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link com.dnc.mprs.propservice.domain.Property}.
 */
@Service
@Transactional
public class PropertyService {

    private static final Logger LOG = LoggerFactory.getLogger(PropertyService.class);

    private final PropertyRepository propertyRepository;

    private final PropertySearchRepository propertySearchRepository;

    public PropertyService(PropertyRepository propertyRepository, PropertySearchRepository propertySearchRepository) {
        this.propertyRepository = propertyRepository;
        this.propertySearchRepository = propertySearchRepository;
    }

    /**
     * Save a property.
     *
     * @param property the entity to save.
     * @return the persisted entity.
     */
    public Mono<Property> save(Property property) {
        LOG.debug("Request to save Property : {}", property);
        return propertyRepository.save(property).flatMap(propertySearchRepository::save);
    }

    /**
     * Update a property.
     *
     * @param property the entity to save.
     * @return the persisted entity.
     */
    public Mono<Property> update(Property property) {
        LOG.debug("Request to update Property : {}", property);
        return propertyRepository.save(property).flatMap(propertySearchRepository::save);
    }

    /**
     * Partially update a property.
     *
     * @param property the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<Property> partialUpdate(Property property) {
        LOG.debug("Request to partially update Property : {}", property);

        return propertyRepository
            .findById(property.getId())
            .map(existingProperty -> {
                if (property.getAddress() != null) {
                    existingProperty.setAddress(property.getAddress());
                }
                if (property.getRegionCd() != null) {
                    existingProperty.setRegionCd(property.getRegionCd());
                }
                if (property.getLocalName() != null) {
                    existingProperty.setLocalName(property.getLocalName());
                }
                if (property.getStreet() != null) {
                    existingProperty.setStreet(property.getStreet());
                }
                if (property.getFloor() != null) {
                    existingProperty.setFloor(property.getFloor());
                }
                if (property.getType() != null) {
                    existingProperty.setType(property.getType());
                }
                if (property.getArea() != null) {
                    existingProperty.setArea(property.getArea());
                }
                if (property.getRooms() != null) {
                    existingProperty.setRooms(property.getRooms());
                }
                if (property.getBathrooms() != null) {
                    existingProperty.setBathrooms(property.getBathrooms());
                }
                if (property.getBuildYear() != null) {
                    existingProperty.setBuildYear(property.getBuildYear());
                }
                if (property.getParkingYn() != null) {
                    existingProperty.setParkingYn(property.getParkingYn());
                }
                if (property.getDescription() != null) {
                    existingProperty.setDescription(property.getDescription());
                }
                if (property.getCreatedAt() != null) {
                    existingProperty.setCreatedAt(property.getCreatedAt());
                }
                if (property.getUpdatedAt() != null) {
                    existingProperty.setUpdatedAt(property.getUpdatedAt());
                }

                return existingProperty;
            })
            .flatMap(propertyRepository::save)
            .flatMap(savedProperty -> {
                propertySearchRepository.save(savedProperty);
                return Mono.just(savedProperty);
            });
    }

    /**
     * Get all the properties.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Flux<Property> findAll(Pageable pageable) {
        LOG.debug("Request to get all Properties");
        return propertyRepository.findAllBy(pageable);
    }

    /**
     * Returns the number of properties available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return propertyRepository.count();
    }

    /**
     * Returns the number of properties available in search repository.
     *
     */
    public Mono<Long> searchCount() {
        return propertySearchRepository.count();
    }

    /**
     * Get one property by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Mono<Property> findOne(Long id) {
        LOG.debug("Request to get Property : {}", id);
        return propertyRepository.findById(id);
    }

    /**
     * Delete the property by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(Long id) {
        LOG.debug("Request to delete Property : {}", id);
        return propertyRepository.deleteById(id).then(propertySearchRepository.deleteById(id));
    }

    /**
     * Search for the property corresponding to the query.
     *
     * @param query the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Flux<Property> search(String query, Pageable pageable) {
        LOG.debug("Request to search for a page of Properties for query {}", query);
        return propertySearchRepository.search(query, pageable);
    }
}
