package com.dnc.mprs.propservice.repository;

import com.dnc.mprs.propservice.domain.Property;
import com.dnc.mprs.propservice.repository.rowmapper.ComplexRowMapper;
import com.dnc.mprs.propservice.repository.rowmapper.PropertyRowMapper;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.SimpleR2dbcRepository;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Comparison;
import org.springframework.data.relational.core.sql.Condition;
import org.springframework.data.relational.core.sql.Conditions;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoinCondition;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.data.relational.repository.support.MappingRelationalEntityInformation;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC custom repository implementation for the Property entity.
 */
@SuppressWarnings("unused")
class PropertyRepositoryInternalImpl extends SimpleR2dbcRepository<Property, Long> implements PropertyRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final ComplexRowMapper complexMapper;
    private final PropertyRowMapper propertyMapper;

    private static final Table entityTable = Table.aliased("property", EntityManager.ENTITY_ALIAS);
    private static final Table complexTable = Table.aliased("complex", "complex");

    public PropertyRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        ComplexRowMapper complexMapper,
        PropertyRowMapper propertyMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(Property.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.complexMapper = complexMapper;
        this.propertyMapper = propertyMapper;
    }

    @Override
    public Flux<Property> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<Property> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = PropertySqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(ComplexSqlHelper.getColumns(complexTable, "complex"));
        SelectFromAndJoinCondition selectFrom = Select.builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(complexTable)
            .on(Column.create("complex_id", entityTable))
            .equals(Column.create("id", complexTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, Property.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<Property> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<Property> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    private Property process(Row row, RowMetadata metadata) {
        Property entity = propertyMapper.apply(row, "e");
        entity.setComplex(complexMapper.apply(row, "complex"));
        return entity;
    }

    @Override
    public <S extends Property> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
