package com.dnc.mprs.propservice.repository.rowmapper;

import com.dnc.mprs.propservice.domain.Complex;
import io.r2dbc.spi.Row;
import java.time.Instant;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Complex}, with proper type conversions.
 */
@Service
public class ComplexRowMapper implements BiFunction<Row, String, Complex> {

    private final ColumnConverter converter;

    public ComplexRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Complex} stored in the database.
     */
    @Override
    public Complex apply(Row row, String prefix) {
        Complex entity = new Complex();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setComplexName(converter.fromRow(row, prefix + "_complex_name", String.class));
        entity.setState(converter.fromRow(row, prefix + "_state", String.class));
        entity.setCounty(converter.fromRow(row, prefix + "_county", String.class));
        entity.setCity(converter.fromRow(row, prefix + "_city", String.class));
        entity.setTown(converter.fromRow(row, prefix + "_town", String.class));
        entity.setAddressCode(converter.fromRow(row, prefix + "_address_code", String.class));
        entity.setCreatedAt(converter.fromRow(row, prefix + "_created_at", Instant.class));
        entity.setUpdatedAt(converter.fromRow(row, prefix + "_updated_at", Instant.class));
        return entity;
    }
}
