package ru.itis.prytkovd.persist.statements;

import ru.itis.prytkovd.persist.ColumnInfo;
import ru.itis.prytkovd.persist.Persistor;
import ru.itis.prytkovd.persist.TableInfo;

import java.util.*;
import java.util.stream.Collectors;

public class UpdateStatement<T> extends AbstractStatement<T> {
    private final TableInfo table;
    private final List<ColumnInfo> columns;
    private final Map<String, Object> setClauses;
    private final List<String> whereClauses;
    private final List<Object> whereParams;
    private T entity;

    public UpdateStatement(Persistor persistor, Class<T> type) {
        super(persistor, type);
        this.table = persistor.getTableInfo(type);
        this.columns = table.getColumns();
        this.setClauses = new HashMap<>();
        this.whereClauses = new ArrayList<>();
        this.whereParams = new ArrayList<>();
    }

    public UpdateStatement<T> set(String columnName, Object value) {
        if (!columns.stream()
            .map(ColumnInfo::getName)
            .collect(Collectors.toSet())
            .contains(columnName)) {
            throw new IllegalArgumentException("There is no column " + columnName + " in table " + table.getName());
        }
        if (columns.stream()
            .filter(column -> column.getName().equals(columnName))
            .findFirst()
            .get()
            .isPrimaryKey()
        ) {
            throw new IllegalArgumentException("Cant assign column " + columnName + "which is primary key of " +
                                               "table " + table.getName());
        }
        setClauses.put(columnName, value);
        return this;
    }

    public UpdateStatement<T> where(String expression, Object... parameters) {
        long countOfParameters = expression.chars().filter(c -> c == '?').count();

        if (parameters.length != countOfParameters) {
            throw new IllegalArgumentException("Provided " + parameters.length + " parameters but expected " + countOfParameters);
        }

        whereClauses.add(expression);
        whereParams.addAll(Arrays.asList(parameters));
        return this;
    }

    public UpdateStatement<T> entity(T entity) {
        this.entity = entity;
        return this;
    }

    @Override
    protected String toSQL() {
        if (entity != null && (!whereClauses.isEmpty() || !setClauses.isEmpty())) {
            throw new IllegalStateException("Cant have specified entity and where clauses or columns to set at the " +
                                            "same time");
        }

        if (entity == null && whereClauses.isEmpty()) {
            throw new IllegalStateException("Entity is not specified so where clauses must be specified");
        }

        if (entity == null && setClauses.isEmpty()) {
            throw new IllegalStateException("Where clauses are specified so columns to be set and their new values " +
                                            "should be specified");
        }

        StringBuilder sqlBuilder = new StringBuilder();

        sqlBuilder.append("update ")
            .append(table.getName());

        sqlBuilder.append(" set ");

        if (setClauses.isEmpty()) {
            sqlBuilder.append(
                columns.stream()
                    .filter(column -> !column.isPrimaryKey())
                    .map(column -> column.getName() + " = ?")
                    .collect(Collectors.joining(", "))
            );
        } else {
            sqlBuilder.append(
                setClauses.entrySet().stream()
                    .map(entry -> entry.getKey() + " = ?")
                    .collect(Collectors.joining(", "))
            );
        }

        sqlBuilder.append(" where ");

        if (whereClauses.isEmpty()) {
            sqlBuilder.append(table.getPrimaryKey().getName())
                .append(" = ?");
        } else {
            sqlBuilder.append(
                whereClauses.stream()
                    .map(expr -> '(' + expr + ')')
                    .collect(Collectors.joining(" and "))
            );
        }

        return sqlBuilder.toString();
    }

    public void execute() {
        String sql = toSQL();
        List<Object> parameters = new ArrayList<>();

        if (entity == null) {
            parameters.addAll(
                setClauses.entrySet().stream()
                    .map(Map.Entry::getValue)
                    .toList()
            );

            parameters.addAll(whereParams);
        } else {
            parameters.addAll(
                columns.stream()
                    .filter(column -> !column.isPrimaryKey())
                    .map(column -> column.getValue(entity))
                    .toList()
            );

            ColumnInfo primaryKey = table.getPrimaryKey();

            parameters.add(primaryKey.getValue(entity));
        }

        persistor.executeUpdate(sql, parameters);
    }
}
