package ru.itis.prytkovd.persist.statements;

import ru.itis.prytkovd.persist.ColumnInfo;
import ru.itis.prytkovd.persist.Persistor;
import ru.itis.prytkovd.persist.TableInfo;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InsertStatement<T> extends AbstractStatement<T> {
    protected final List<T> entities;
    protected final TableInfo table;
    protected final List<ColumnInfo> columns;
    protected final Set<String> columnNames;
    protected boolean updateOnConflict;
    protected boolean doNothingOnConflict;

    public InsertStatement(Persistor persistor, Class<T> type) {
        super(persistor, type);
        this.entities = new ArrayList<>();
        this.table = persistor.getTableInfo(type);
        this.columns = table.getColumns();
        this.columnNames = new HashSet<>();
        this.updateOnConflict = false;
    }

    public InsertStatement<T> entity(T entity) {
        this.entities.add(entity);
        return this;
    }

    public InsertStatement<T> entities(T... entities) {
        return entities(Arrays.asList(entities));
    }

    private InsertStatement<T> entities(List<T> entities) {
        this.entities.addAll(entities);
        return this;
    }

    @Override
    protected String toSQL() {
        if (columnNames.isEmpty()) {
            columnNames.addAll(columns.stream()
                .filter(column -> !column.isPrimaryKey())
                .map(ColumnInfo::getName)
                .toList());
        }

        List<String> columnNameList = columns.stream()
            .map(ColumnInfo::getName)
            .filter(columnNames::contains)
            .toList();

        StringBuilder sqlBuilder = new StringBuilder();

        String valuesString = '(' +
                              IntStream.range(0, columnNameList.size())
                                  .mapToObj(i -> "?")
                                  .collect(Collectors.joining(", ")) +
                              ')';

        sqlBuilder.append("insert into ")
            .append(table.getName())
            .append('(')
            .append(String.join(", ", columnNameList))
            .append(") values ")
            .append(
                IntStream.range(0, entities.size())
                    .mapToObj(i -> valuesString)
                    .collect(Collectors.joining(", "))
            );

        if (doNothingOnConflict || updateOnConflict) {

            sqlBuilder.append(" on conflict (")
                .append(table.getPrimaryKey().getName())
                .append(") do ");

            if (doNothingOnConflict) {
                sqlBuilder.append("nothing");
            } else {
                sqlBuilder
                    .append("update set ")
                    .append(
                    columnNameList.stream()
                        .map(columnName -> String.format(
                            "%s = excluded.%s",
                            columnName,
                            columnName
                        )).collect(Collectors.joining(", "))
                );
            }
        }

        return sqlBuilder.toString();
    }

    public void execute() {
        String sql = toSQL();
        List<Object> parameters = entities.stream()
            .flatMap(
                value -> columns.stream()
                    .filter(column -> !column.isPrimaryKey())
                    .filter(column -> columnNames.contains(column.getName()))
                    .map(column -> column.getValue(value))
            ).toList();

        List<Map<String, Object>> mapList = persistor.executeUpdate(sql, parameters);

        ColumnInfo primaryKey = table.getPrimaryKey();

        for (int entityIndex = 0; entityIndex < entities.size(); entityIndex++) {
            T entity = entities.get(entityIndex);
            Map<String, Object> map = mapList.get(entityIndex);
            primaryKey.setValue(entity, map.get(primaryKey.getName()));
        }
    }
}
