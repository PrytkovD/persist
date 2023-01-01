package ru.itis.prytkovd.persist.statements;

import ru.itis.prytkovd.persist.Persistor;
import ru.itis.prytkovd.persist.TableInfo;
import ru.itis.prytkovd.persist.exceptions.PersistenceException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DeleteStatement<T> extends AbstractStatement<T> {
    private final List<String> whereClauses;
    private final List<Object> whereParams;
    private T entity;
    private TableInfo table;

    public DeleteStatement(Persistor persistor, Class<T> type) {
        super(persistor, type);
        this.table = persistor.getTableInfo(type);
        this.whereClauses = new ArrayList<>();
        this.whereParams = new ArrayList<>();
    }

    public DeleteStatement<T> where(String expression, Object... parameters) {
        long countOfParameters = expression.chars().filter(c -> c == '?').count();

        if (parameters.length != countOfParameters) {
            throw new IllegalArgumentException("Provided " + parameters.length + " parameters but expected " + countOfParameters);
        }

        whereClauses.add(expression);
        whereParams.addAll(Arrays.asList(parameters));
        return this;
    }

    public DeleteStatement<T> entity(T entity) {
        if (this.entity != null) {
            throw new IllegalStateException("Entity is already set");
        }
        this.entity = entity;
        return this;
    }

    @Override
    protected String toSQL() {
        if (entity == null && whereClauses.isEmpty()) {
            throw new IllegalStateException("Nothing is set");
        }

        if (entity != null && !whereClauses.isEmpty()) {
            throw new IllegalStateException("Entity and where clauses cant be set at the same time");
        }

        StringBuilder sqlBuilder = new StringBuilder();

        sqlBuilder.append("delete from ")
            .append(table.getName());

        if (entity == null) {
            sqlBuilder.append(" where ")
                .append(
                    whereClauses.stream()
                        .map(expr -> '(' + expr + ')')
                        .collect(Collectors.joining(" and "))
                );
        } else {
            sqlBuilder.append(" where ")
                .append(table.getPrimaryKey().getName())
                .append(" = ?");
        }

        return sqlBuilder.toString();
    }

    public void execute() {
        if (whereClauses.isEmpty()) {
            whereParams.add(table.getPrimaryKey().getValue(entity));
        }
        persistor.executeUpdate(toSQL(), whereParams);
    }
}
