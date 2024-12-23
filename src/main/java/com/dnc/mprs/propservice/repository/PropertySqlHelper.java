package com.dnc.mprs.propservice.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class PropertySqlHelper {

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("address", table, columnPrefix + "_address"));
        columns.add(Column.aliased("region_cd", table, columnPrefix + "_region_cd"));
        columns.add(Column.aliased("local_name", table, columnPrefix + "_local_name"));
        columns.add(Column.aliased("street", table, columnPrefix + "_street"));
        columns.add(Column.aliased("floor", table, columnPrefix + "_floor"));
        columns.add(Column.aliased("type", table, columnPrefix + "_type"));
        columns.add(Column.aliased("area", table, columnPrefix + "_area"));
        columns.add(Column.aliased("rooms", table, columnPrefix + "_rooms"));
        columns.add(Column.aliased("bathrooms", table, columnPrefix + "_bathrooms"));
        columns.add(Column.aliased("build_year", table, columnPrefix + "_build_year"));
        columns.add(Column.aliased("parking_yn", table, columnPrefix + "_parking_yn"));
        columns.add(Column.aliased("description", table, columnPrefix + "_description"));
        columns.add(Column.aliased("created_at", table, columnPrefix + "_created_at"));
        columns.add(Column.aliased("updated_at", table, columnPrefix + "_updated_at"));

        columns.add(Column.aliased("complex_id", table, columnPrefix + "_complex_id"));
        return columns;
    }
}
