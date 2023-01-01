package ru.itis.prytkovd.persist.statements;

import ru.itis.prytkovd.persist.ColumnInfo;
import ru.itis.prytkovd.persist.Persistor;

import java.util.Set;

public class ColumnsInsertStatement<T> extends InsertStatement<T> {
    public ColumnsInsertStatement(Persistor persistor, Class<T> type) {
        super(persistor, type);
    }

    public InsertStatement<T> columns(String... columnNames) {
        return columns(Set.of(columnNames));
    }

    private InsertStatement<T> columns(Set<String> columnNames) {
        for (String name : columnNames) {
            boolean isPresent = false;

            for (ColumnInfo column : columns) {
                if (column.getName().equals(name)) {
                    if (column.isPrimaryKey()) {
                        throw new IllegalArgumentException("Cant assign column " + name + "which is primary key of " +
                                                           "table " + table.getName());
                    }
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
