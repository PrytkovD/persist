package ru.itis.prytkovd.persist;

import ru.itis.prytkovd.persist.annotations.Table;
import ru.itis.prytkovd.persist.util.ColumnFilter;
import ru.itis.prytkovd.persist.util.TableUtils;

import java.util.List;

public class TableInfo {
    private final Class<?> type;
    private final String name;
    private final List<ColumnInfo> columns;
    private final List<ColumnInfo> foreignKeys;
    private final ColumnInfo primaryKey;

    public TableInfo(Class<?> type) {
        if (!type.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException(type + " is not a table");
        }

        this.type = type;

        String annotationName = type.getAnnotation(Table.class).name();

        if (annotationName.isEmpty()) {
            this.name = type.getSimpleName();
        } else {
            this.name = annotationName;
        }

        List<ColumnInfo> columnInfo = TableUtils.columnsIn(type.getDeclaredFields());

        this.columns = columnInfo;
        this.foreignKeys = ColumnFilter.foreignKeysIn(columnInfo);
        this.primaryKey = ColumnFilter.primaryKeyIn(columnInfo).orElseThrow(
            () -> new IllegalArgumentException("No primary key")
        );
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public List<ColumnInfo> getColumns() {
        return columns;
    }

    public ColumnInfo getPrimaryKey() {
        return primaryKey;
    }

    public List<ColumnInfo> getForeignKeys() {
        return foreignKeys;
    }
}
