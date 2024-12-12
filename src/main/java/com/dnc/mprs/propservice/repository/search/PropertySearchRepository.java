package com.dnc.mprs.propservice.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.dnc.mprs.propservice.domain.Property;
import com.dnc.mprs.propservice.repository.PropertyRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Property} entity.
 */
public interface PropertySearchRepository extends ElasticsearchRepository<Property, Long>, PropertySearchRepositoryInternal {}

interface PropertySearchRepositoryInternal {
    Page<Property> search(String query, Pageable pageable);

    Page<Property> search(Query query);

    @Async
    void index(Property entity);

    @Async
    void deleteFromIndexById(Long id);
}

class PropertySearchRepositoryInternalImpl implements PropertySearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final PropertyRepository repository;

    PropertySearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, PropertyRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Page<Property> search(String query, Pageable pageable) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery.setPageable(pageable));
    }

    @Override
    public Page<Property> search(Query query) {
        SearchHits<Property> searchHits = elasticsearchTemplate.search(query, Property.class);
        List<Property> hits = searchHits.map(SearchHit::getContent).stream().toList();
        return new PageImpl<>(hits, query.getPageable(), searchHits.getTotalHits());
    }

    @Override
    public void index(Property entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Property.class);
    }
}
