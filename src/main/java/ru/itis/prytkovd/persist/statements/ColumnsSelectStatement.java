package ru.itis.prytkovd.persist.statements;

import ru.itis.prytkovd.persist.ColumnInfo;
import ru.itis.prytkovd.persist.Persistor;

import java.util.Set;

public class ColumnsSelectStatement<T> extends SelectStatement<T> {
    public ColumnsSelectStatement(Persistor persistor, Class<T> type) {
        super(persistor, type);
    }

    public SelectStatement<T> columns(String... columnNames) {
        return columns(Set.of(columnNames));
    }

    private SelectStatement<T> columns(Set<String> columnNames) {
        for (String name : columnNames) {
            boolean isPresent = false;

            for (ColumnInfo column : columns) {
                if (column.getName().equals(name)) {
                    isPresent = true;
                    break;
                }
            }

            if (!isPresent) {
                throw new IllegalArgumentException("There is no column " + name + " in table " + table.getName());
            }
        }
        this.columnNames.addAll(columnNames);
        return this;
    }
}
