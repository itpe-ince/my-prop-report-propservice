package com.dnc.mprs.propservice.web.rest;

import com.dnc.mprs.propservice.domain.Complex;
import com.dnc.mprs.propservice.repository.ComplexRepository;
import com.dnc.mprs.propservice.service.ComplexService;
import com.dnc.mprs.propservice.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.ForwardedHeaderUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

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
    public Mono<ResponseEntity<Complex>> createComplex(@Valid @RequestBody Complex complex) throws URISyntaxException {
        LOG.debug("REST request to save Complex : {}", complex);
        if (complex.getId() != null) {
            throw new BadRequestAlertException("A new complex cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return complexService
            .save(complex)
            .map(result -> {
                try {
                    return ResponseEntity.created(new URI("/api/complexes/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
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
    public Mono<ResponseEntity<Complex>> updateComplex(
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

        return complexRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return complexService
                    .update(complex)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity.ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                            .body(result)
                    );
            });
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
    public Mono<ResponseEntity<Complex>> partialUpdateComplex(
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

        return complexRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<Complex> result = complexService.partialUpdate(complex);

                return result
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(res ->
                        ResponseEntity.ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId().toString()))
                            .body(res)
                    );
            });
    }

    /**
     * {@code GET  /complexes} : get all the complexes.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of complexes in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<Complex>>> getAllComplexes(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        LOG.debug("REST request to get a page of Complexes");
        return complexService
            .countAll()
            .zipWith(complexService.findAll(pageable).collectList())
            .map(countWithEntities ->
                ResponseEntity.ok()
                    .headers(
                        PaginationUtil.generatePaginationHttpHeaders(
                            ForwardedHeaderUtils.adaptFromForwardedHeaders(request.getURI(), request.getHeaders()),
                            new PageImpl<>(countWithEntities.getT2(), pageable, countWithEntities.getT1())
                        )
                    )
                    .body(countWithEntities.getT2())
            );
    }

    /**
     * {@code GET  /complexes/:id} : get the "id" complex.
     *
     * @param id the id of the complex to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the complex, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Complex>> getComplex(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Complex : {}", id);
        Mono<Complex> complex = complexService.findOne(id);
        return ResponseUtil.wrapOrNotFound(complex);
    }

    /**
     * {@code DELETE  /complexes/:id} : delete the "id" complex.
     *
     * @param id the id of the complex to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteComplex(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Complex : {}", id);
        return complexService
            .delete(id)
            .then(
                Mono.just(
                    ResponseEntity.noContent()
                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                        .build()
                )
            );
    }

    /**
     * {@code SEARCH  /complexes/_search?query=:query} : search for the complex corresponding
     * to the query.
     *
     * @param query the query of the complex search.
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public Mono<ResponseEntity<Flux<Complex>>> searchComplexes(
        @RequestParam("query") String query,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        LOG.debug("REST request to search for a page of Complexes for query {}", query);
        return complexService
            .searchCount()
            .map(total -> new PageImpl<>(new ArrayList<>(), pageable, total))
            .map(page ->
                PaginationUtil.generatePaginationHttpHeaders(
                    ForwardedHeaderUtils.adaptFromForwardedHeaders(request.getURI(), request.getHeaders()),
                    page
                )
            )
            .map(headers -> ResponseEntity.ok().headers(headers).body(complexService.search(query, pageable)));
    }
}
