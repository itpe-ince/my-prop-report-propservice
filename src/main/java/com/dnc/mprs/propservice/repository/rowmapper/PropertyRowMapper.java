package com.dnc.mprs.propservice.repository.rowmapper;

import com.dnc.mprs.propservice.domain.Property;
import io.r2dbc.spi.Row;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Property}, with proper type conversions.
 */
@Service
public class PropertyRowMapper implements BiFunction<Row, String, Property> {

    private final ColumnConverter converter;

    public PropertyRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Property} stored in the database.
     */
    @Override
    public Property apply(Row row, String prefix) {
        Property entity = new Property();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setAddress(converter.fromRow(row, prefix + "_address", String.class));
        entity.setRegionCd(converter.fromRow(row, prefix + "_region_cd", String.class));
        entity.setLocalName(converter.fromRow(row, prefix + "_local_name", String.class));
        entity.setStreet(converter.fromRow(row, prefix + "_street", String.class));
        entity.setFloor(converter.fromRow(row, prefix + "_floor", Integer.class));
        entity.setType(converter.fromRow(row, prefix + "_type", String.class));
        entity.setArea(converter.fromRow(row, prefix + "_area", BigDecimal.class));
        entity.setRooms(converter.fromRow(row, prefix + "_rooms", Integer.class));
        entity.setBathrooms(converter.fromRow(row, prefix + "_bathrooms", Integer.class));
        entity.setBuildYear(converter.fromRow(row, prefix + "_build_year", Integer.class));
        entity.setParkingYn(converter.fromRow(row, prefix + "_parking_yn", String.class));
        entity.setDescription(converter.fromRow(row, prefix + "_description", String.class));
        entity.setCreatedAt(converter.fromRow(row, prefix + "_created_at", Instant.class));
        entity.setUpdatedAt(converter.fromRow(row, prefix + "_updated_at", Instant.class));
        entity.setComplexId(converter.fromRow(row, prefix + "_complex_id", Long.class));
        return entity;
    }
}
