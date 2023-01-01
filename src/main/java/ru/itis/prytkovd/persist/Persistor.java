package ru.itis.prytkovd.persist;

import ru.itis.prytkovd.persist.exceptions.PersistenceException;
import ru.itis.prytkovd.persist.statements.*;
import ru.itis.prytkovd.persist.util.ResultSets;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Persistor {
    private final DataSource dataSource;
    private final Map<Class<?>, TableInfo> tables;

    public Persistor(DataSource dataSource) {
        this.dataSource = dataSource;
        this.tables = new HashMap<>();
    }

    public <T> void createTable(Class<T> type) {
        new CreateTableStatement<>(this, type).execute();
    }

    public <T> void dropTable(Class<T> type) {
        new DropTableStatement<>(this, type).execute();
    }

    public <T> ColumnsSelectStatement<T> select(Class<T> type) {
        return new ColumnsSelectStatement<>(this, type);
    }

    public <T> OnConflictColumnsInsertStatement<T> insert(Class<T> type) {
        return new OnConflictColumnsInsertStatement<>(this, type);
    }

    public <T> UpdateStatement<T> update(Class<T> type) {
        return new UpdateStatement<>(this, type);
    }

    public <T> DeleteStatement<T> delete(Class<T> type) {
        return new DeleteStatement<>(this, type);
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new PersistenceException("Unable to establish connection", e);
        }
    }

    public TableInfo getTableInfo(Class<?> type) {
        return tables.computeIfAbsent(type, TableInfo::new);
    }

    public void execute(String sql) {
        System.out.println(sql);
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    public List<Map<String, Object>> executeUpdate(String sql, List<Object> parameters) {
        System.out.println(sql + parameters);
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                List<Map<String, Object>> mapList = ResultSets.asMapList(generatedKeys);
                System.out.println(mapList);
                return mapList;
            }
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    public List<Map<String, Object>> executeQuery(String sql, List<Object> parameters) {
        System.out.println(sql + parameters);
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> mapList = ResultSets.asMapList(resultSet);
                System.out.println(mapList);
                return mapList;
            }
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }
}
