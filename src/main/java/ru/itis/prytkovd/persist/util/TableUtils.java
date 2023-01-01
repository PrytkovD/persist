package ru.itis.prytkovd.persist.util;

import ru.itis.prytkovd.persist.ColumnInfo;
import ru.itis.prytkovd.persist.annotations.Column;
import ru.itis.prytkovd.persist.annotations.ForeignKey;
import ru.itis.prytkovd.persist.annotations.PrimaryKey;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public final class TableUtils {
    public static boolean isPrimaryKey(Field field) {
        return field.isAnnotationPresent(PrimaryKey.class);
    }

    public static boolean isForeignKey(Field field) {
        return field.isAnnotationPresent(ForeignKey.class);
    }

    public static boolean isRegularColumn(Field field) {
        return field.isAnnotationPresent(Column.class);
    }

    public static boolean isColumn(Field field) {
        return isRegularColumn(field) ||
               isPrimaryKey(field) ||
               isForeignKey(field);
    }

    public static List<ColumnInfo> columnsIn(Field[] fields) {
        return Arrays.stream(fields)
            .filter(TableUtils::isColumn)
            .map(ColumnInfo::new)
            .toList();
    }
}
