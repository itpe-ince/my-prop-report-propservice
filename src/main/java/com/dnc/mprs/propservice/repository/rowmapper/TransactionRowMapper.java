package com.dnc.mprs.propservice.repository.rowmapper;

import com.dnc.mprs.propservice.domain.Transaction;
import io.r2dbc.spi.Row;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Transaction}, with proper type conversions.
 */
@Service
public class TransactionRowMapper implements BiFunction<Row, String, Transaction> {

    private final ColumnConverter converter;

    public TransactionRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Transaction} stored in the database.
     */
    @Override
    public Transaction apply(Row row, String prefix) {
        Transaction entity = new Transaction();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setPropertyId(converter.fromRow(row, prefix + "_property_id", Long.class));
        entity.setTransactionType(converter.fromRow(row, prefix + "_transaction_type", String.class));
        entity.setPrice(converter.fromRow(row, prefix + "_price", BigDecimal.class));
        entity.setTransactionDate(converter.fromRow(row, prefix + "_transaction_date", Instant.class));
        entity.setBuyer(converter.fromRow(row, prefix + "_buyer", String.class));
        entity.setSeller(converter.fromRow(row, prefix + "_seller", String.class));
        entity.setAgent(converter.fromRow(row, prefix + "_agent", String.class));
        entity.setCreatedAt(converter.fromRow(row, prefix + "_created_at", Instant.class));
        entity.setUpdatedAt(converter.fromRow(row, prefix + "_updated_at", Instant.class));
        return entity;
    }
}
