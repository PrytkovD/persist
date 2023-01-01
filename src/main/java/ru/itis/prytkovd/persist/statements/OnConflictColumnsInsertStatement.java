package ru.itis.prytkovd.persist.statements;

import ru.itis.prytkovd.persist.Persistor;

public class OnConflictColumnsInsertStatement<T> extends ColumnsInsertStatement<T> {
    public OnConflictColumnsInsertStatement(Persistor persistor, Class<T> type) {
        super(persistor, type);
    }

    public ColumnsInsertStatement<T> doNothingOnConflict() {
        doNothingOnConflict = true;
        return this;
    }

    public ColumnsInsertStatement<T> updateOnConflict() {
        updateOnConflict = true;
        return this;
    }
}
