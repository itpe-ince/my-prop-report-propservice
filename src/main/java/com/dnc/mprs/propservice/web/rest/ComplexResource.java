package com.dnc.mprs.propservice.web.rest;

import com.dnc.mprs.propservice.domain.Complex;
import com.dnc.mprs.propservice.repository.ComplexRepository;
import com.dnc.mprs.propservice.service.ComplexService;
import com.dnc.mprs.propservice.web.rest.errors.BadRequestAlertException;
import com.dnc.mprs.propservice.web.rest.errors.ElasticsearchExceptionMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.dnc.mprs.propservice.domain.Complex}.
 */
@RestController
@RequestMapping("/api/complexes")
public class ComplexResource {

    private static final Logger LOG = LoggerFactory.getLogger(ComplexResource.class);

    private static final String ENTITY_NAME = "propserviceComplex";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ComplexService complexService;

    private final ComplexRepository complexRepository;

    public ComplexResource(ComplexService complexService, ComplexRepository complexRepository) {
        this.complexService = complexService;
        this.complexRepository = complexRepository;
    }

    /**
     * {@code POST  /complexes} : Create a new complex.
     *
     * @param complex the complex to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new complex, or with status {@code 400 (Bad Request)} if the complex has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Complex> createComplex(@Valid @RequestBody Complex complex) throws URISyntaxException {
        LOG.debug("REST request to save Complex : {}", complex);
        if (complex.getId() != null) {
            throw new BadRequestAlertException("A new complex cannot already have an ID", ENTITY_NAME, "idexists");
        }
        complex = complexService.save(complex);
        return ResponseEntity.created(new URI("/api/complexes/" + complex.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, complex.getId().toString()))
            .body(complex);
    }

    /**
     * {@code PUT  /complexes/:id} : Updates an existing complex.
     *
     * @param id the id of the complex to save.
     * @param complex the complex to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated complex,
     * or with status {@code 400 (Bad Request)} if the complex is not valid,
     * or with status {@code 500 (Internal Server Error)} if the complex couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Complex> updateComplex(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Complex complex
    ) throws URISyntaxException {
        LOG.debug("REST request to update Complex : {}, {}", id, complex);
        if (complex.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, complex.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!complexRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        complex = complexService.update(complex);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, complex.getId().toString()))
            .body(complex);
    }

    /**
     * {@code PATCH  /complexes/:id} : Partial updates given fields of an existing complex, field will ignore if it is null
     *
     * @param id the id of the complex to save.
     * @param complex the complex to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated complex,
     * or with status {@code 400 (Bad Request)} if the complex is not valid,
     * or with status {@code 404 (Not Found)} if the complex is not found,
     * or with status {@code 500 (Internal Server Error)} if the complex couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Complex> partialUpdateComplex(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Complex complex
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Complex partially : {}, {}", id, complex);
        if (complex.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, complex.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!complexRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Complex> result = complexService.partialUpdate(complex);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, complex.getId().toString())
        );
    }

    /**
     * {@code GET  /complexes} : get all the complexes.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of complexes in body.
     */
    @GetMapping("")
    public ResponseEntity<List<Complex>> getAllComplexes(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of Complexes");
        Page<Complex> page = complexService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /complexes/:id} : get the "id" complex.
     *
     * @param id the id of the complex to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the complex, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Complex> getComplex(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Complex : {}", id);
        Optional<Complex> complex = complexService.findOne(id);
        return ResponseUtil.wrapOrNotFound(complex);
    }

    /**
     * {@code DELETE  /complexes/:id} : delete the "id" complex.
     *
     * @param id the id of the complex to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComplex(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Complex : {}", id);
        complexService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /complexes/_search?query=:query} : search for the complex corresponding
     * to the query.
     *
     * @param query the query of the complex search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public ResponseEntity<List<Complex>> searchComplexes(
        @RequestParam("query") String query,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to search for a page of Complexes for query {}", query);
        try {
            Page<Complex> page = complexService.search(query, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            return ResponseEntity.ok().headers(headers).body(page.getContent());
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
