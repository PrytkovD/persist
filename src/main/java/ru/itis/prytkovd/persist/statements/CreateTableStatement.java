package ru.itis.prytkovd.persist.statements;

import ru.itis.prytkovd.persist.ColumnInfo;
import ru.itis.prytkovd.persist.Persistor;
import ru.itis.prytkovd.persist.TableInfo;

import java.util.stream.Collectors;

public class CreateTableStatement<T> extends AbstractStatement<T> {
    public CreateTableStatement(Persistor persistor, Class<T> type) {
        super(persistor, type);
    }

    @Override
    protected String toSQL() {
        TableInfo table = persistor.getTableInfo(type);

        StringBuilder sqlBuilder = new StringBuilder();

        sqlBuilder.append("create table if not exists ")
            .append(table.getName())
            .append('(');

        if (table.getColumns().size() > 0) {
            sqlBuilder.append(
                table.getColumns()
                    .stream()
                    .map(ColumnInfo::toSQL)
                    .collect(Collectors.joining(", "))
            );
        }

        sqlBuilder.append(", ")
            .append("primary key (")
            .append(table.getPrimaryKey().getName())
            .append(")");

        if (table.getForeignKeys().size() > 0) {
            sqlBuilder.append(", ")
                .append(
                    table.getForeignKeys()
                        .stream()
                        .map(key -> {
                            TableInfo otherTable = persistor.getTableInfo(key.references());
                            ColumnInfo otherPrimaryKey = otherTable.getPrimaryKey();

                            if (!key.getType().equals(otherPrimaryKey.getType())) {
                                throw new IllegalArgumentException("Foreign key does match type of primary key");
                            }

                            return "foreign key (" +
                                   key.getName() +
                                   ") references " +
                                   otherTable.getName() +
                                   '(' + otherPrimaryKey.getName() + ')';
                        })
                        .collect(Collectors.joining(", "))
                );
        }

        sqlBuilder.append(')');

        return sqlBuilder.toString();
    }

    public void execute() {
        persistor.execute(toSQL());
    }
}
