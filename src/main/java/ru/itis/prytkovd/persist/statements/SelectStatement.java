package ru.itis.prytkovd.persist.statements;

import ru.itis.prytkovd.persist.ColumnInfo;
import ru.itis.prytkovd.persist.Persistor;
import ru.itis.prytkovd.persist.TableInfo;
import ru.itis.prytkovd.persist.exceptions.PersistenceException;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SelectStatement<T> extends AbstractStatement<T> {
    protected final TableInfo table;
    protected final List<ColumnInfo> columns;
    protected final List<String> whereClauses;
    protected final List<Object> whereParams;
    protected final List<String> groupByColumns;
    protected final List<String> orderByClauses;
    protected final Set<String> columnNames;

    public SelectStatement(Persistor persistor, Class<T> type) {
        super(persistor, type);
        this.table = persistor.getTableInfo(type);
        this.columns = table.getColumns();
        this.whereClauses = new ArrayList<>();
        this.whereParams = new ArrayList<>();
        this.groupByColumns = new ArrayList<>();
        this.orderByClauses = new ArrayList<>();
        this.columnNames = new HashSet<>();
    }

    public SelectStatement<T> where(String expression, Object... parameters) {
        long countOfParameters = expression.chars().filter(c -> c == '?').count();

        if (parameters.length != countOfParameters) {
            throw new IllegalArgumentException("Provided " + parameters.length + " parameters but expected " + countOfParameters);
        }

        whereClauses.add(expression);
        whereParams.addAll(Arrays.asList(parameters));
        return this;
    }

    public SelectStatement<T> groupBy(String columnName) {
        if (!columns.stream()
            .map(ColumnInfo::getName)
            .collect(Collectors.toSet())
            .contains(columnName)) {
            throw new IllegalArgumentException("There is no column " + columnName + " in table " + table.getName());
        }
        groupByColumns.add(columnName);
        return this;
    }

    public SelectStatement<T> orderBy(String sql) {
        orderByClauses.add(sql);
        return this;
    }

    @Override
    protected String toSQL() {
        StringBuilder sqlBuilder = new StringBuilder();

        sqlBuilder.append("select ");

        if (columnNames.isEmpty()) {
            sqlBuilder.append('*');
        } else {
            List<String> columnNameList = columns.stream()
                .map(ColumnInfo::getName)
                .filter(columnNames::contains)
                .toList();
            sqlBuilder.append(String.join(", ", columnNameList));
        }

        sqlBuilder.append(" from ")
            .append(table.getName());

        if (!whereClauses.isEmpty()) {
            sqlBuilder.append(" where ")
                .append(
                    whereClauses.stream()
                        .map(expr -> '(' + expr + ')')
                        .collect(Collectors.joining(" and "))
                );
        }

        if (!groupByColumns.isEmpty()) {
            sqlBuilder.append(" group by ")
                .append(String.join(", ", groupByColumns));
        }

        if (!orderByClauses.isEmpty()) {
            sqlBuilder.append(" order by ")
                .append(String.join(", ", orderByClauses));
        }

        return sqlBuilder.toString();
    }

    public List<Map<String, Object>> queryForMapList() {
        return persistor.executeQuery(toSQL(), whereParams);
    }

    public Optional<Map<String, Object>> queryForMap() {
        List<Map<String, Object>> mapList = queryForMapList();

        if (mapList.size() > 1) {
            throw new PersistenceException("Queried for single map but got multiple rows");
        }

        return mapList.isEmpty() ? Optional.empty() : Optional.of(mapList.get(0));
    }

    public List<T> queryForList(Supplier<T> supplier) {
        return queryForMapList().stream()
            .map(map -> mapToObject(map, supplier))
            .toList();
    }

    public Optional<T> queryForObject(Supplier<T> supplier) {
        Optional<Map<String, Object>> map = queryForMap();
        if (map.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mapToObject(map.get(), supplier));
    }

    private T mapToObject(Map<String, Object> row, Supplier<T> supplier) {
        T object = supplier.get();
        columns.forEach(columnInfo -> {
            columnInfo.setValue(object, row.get(columnInfo.getName().toLowerCase()));
        });
        return object;
    }
}
