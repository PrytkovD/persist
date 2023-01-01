package ru.itis.prytkovd.persist.util;

import ru.itis.prytkovd.persist.ColumnInfo;

import java.util.List;
import java.util.Optional;

public final class ColumnFilter {
    public static Optional<ColumnInfo> primaryKeyIn(List<ColumnInfo> columns) {
        List<ColumnInfo> primaryKeys = columns.stream()
            .filter(ColumnInfo::isPrimaryKey)
            .toList();

        if (primaryKeys.size() > 1) {
            throw new IllegalArgumentException("Multiple primary keys found");
        }

        return primaryKeys.isEmpty() ? Optional.empty() : Optional.of(primaryKeys.get(0));
    }

    public static List<ColumnInfo> foreignKeysIn(List<ColumnInfo> columns) {
        return columns.stream()
            .filter(ColumnInfo::isForeignKey)
            .toList();
    }

    public static List<ColumnInfo> regularColumnsIn(List<ColumnInfo> columns) {
        return columns.stream()
            .filter(column -> !column.isPrimaryKey() && !column.isForeignKey())
            .toList();
    }
}
