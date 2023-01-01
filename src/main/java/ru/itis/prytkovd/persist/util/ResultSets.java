package ru.itis.prytkovd.persist.util;

import ru.itis.prytkovd.persist.exceptions.PersistenceException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultSets {
    public static List<Map<String, Object>> asMapList(ResultSet resultSet) {
        try {
            List<Map<String, Object>> mapList = new ArrayList<>();

            ResultSetMetaData metaData = resultSet.getMetaData();

            while (resultSet.next()) {
                Map<String, Object> map = new HashMap<>();
                for (int columnIndex = 1; columnIndex <= metaData.getColumnCount(); columnIndex++) {
                    String columnName = metaData.getColumnName(columnIndex);
                    Object columnValue = resultSet.getObject(columnIndex);
                    map.put(columnName, columnValue);
                }
                mapList.add(map);
            }

            return mapList;
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }
}
