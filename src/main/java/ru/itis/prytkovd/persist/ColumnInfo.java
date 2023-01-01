package ru.itis.prytkovd.persist;

import ru.itis.prytkovd.persist.annotations.Column;
import ru.itis.prytkovd.persist.annotations.ForeignKey;
import ru.itis.prytkovd.persist.annotations.PrimaryKey;
import ru.itis.prytkovd.persist.exceptions.PersistenceException;
import ru.itis.prytkovd.persist.util.SQLTypeMapper;
import ru.itis.prytkovd.persist.util.TableUtils;

import java.lang.reflect.Field;
import java.sql.JDBCType;
import java.sql.SQLType;

public class ColumnInfo {
    private final Field field;
    private final String name;

    public ColumnInfo(Field field) {
        if (!TableUtils.isColumn(field)) {
            throw new IllegalArgumentException(field + " is not a column");
        }

        field.setAccessible(true);

        this.field = field;

        String annotationName;

        if (isPrimaryKey()) {
            annotationName = field.getAnnotation(PrimaryKey.class).name();
        } else if (isForeignKey()) {
            annotationName = field.getAnnotation(ForeignKey.class).name();
        } else {
            annotationName = field.getAnnotation(Column.class).name();
        }

        if (annotationName.isEmpty()) {
            this.name = field.getName();
        } else {
            this.name = annotationName;
        }
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return field.getType();
    }

    public Object getValue(Object object) {
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            throw new PersistenceException(e);
        }
    }

    public void setValue(Object object, Object value) {
        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            throw new PersistenceException(e);
        }
    }

    public Class<?> references() {
        if (!isForeignKey()) {
            throw new IllegalStateException("Not a foreign key");
        }
        return field.getAnnotation(ForeignKey.class).references();
    }

    public boolean isPrimaryKey() {
        return TableUtils.isPrimaryKey(field);
    }

    public boolean isForeignKey() {
        return TableUtils.isForeignKey(field);
    }

    public String toSQL() {
        SQLType sqlType = SQLTypeMapper.sqlType(getType(), isPrimaryKey());

        String type = sqlType.toString();

        if (sqlType.equals(JDBCType.VARCHAR)) {
            type += "(" + 1000 + ")";
        }

        return name + ' ' + type;
    }
}
