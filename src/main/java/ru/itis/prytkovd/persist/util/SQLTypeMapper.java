package ru.itis.prytkovd.persist.util;

import java.sql.SQLType;
import java.util.Map;

import static java.sql.JDBCType.*;
import static ru.itis.prytkovd.persist.util.SerialSQLType.*;

public class SQLTypeMapper {
    public static final Map<Class<?>, SQLType> sqlTypes = Map.of(
        Boolean.class, BOOLEAN,
        Byte.class, TINYINT,
        Short.class, SMALLINT,
        Integer.class, INTEGER,
        Long.class, BIGINT,
        Float.class, FLOAT,
        Double.class, DOUBLE,
        String.class, VARCHAR
    );

    public static final Map<Class<?>, SQLType> serialSqlTypes = Map.of(
        Short.class, SMALLSERIAL,
        Integer.class, SERIAL,
        Long.class, BIGSERIAL
    );

    public static SQLType sqlType(Class<?> javaType) {
        if (!sqlTypes.containsKey(javaType)) {
            throw new IllegalArgumentException("Unknown java type " + javaType);
        }
        return sqlTypes.get(javaType);
    }

    public static SQLType sqlType(Class<?> javaType, boolean isSerial) {
        if (isSerial) {
            if (!serialSqlTypes.containsKey(javaType)) {
                throw new IllegalArgumentException("Unknown java type " + javaType);
            }
            return serialSqlTypes.get(javaType);
        }
        return sqlType(javaType);
    }
}
