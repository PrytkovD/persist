package ru.itis.prytkovd.persist.util;

import java.sql.SQLType;
import java.sql.Types;

public enum SerialSQLType implements SQLType {
    SMALLSERIAL(Types.SMALLINT),
    SERIAL(Types.INTEGER),
    BIGSERIAL(Types.BIGINT);

    private final Integer type;

    SerialSQLType(final Integer type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public String getVendor() {
        return "ru.itis.prytkovd.persist.util";
    }

    @Override
    public Integer getVendorTypeNumber() {
        return type;
    }
}
