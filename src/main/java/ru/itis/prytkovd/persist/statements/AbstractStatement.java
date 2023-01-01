package ru.itis.prytkovd.persist.statements;

import ru.itis.prytkovd.persist.Persistor;

abstract class AbstractStatement<T> {
    protected final Persistor persistor;
    protected final Class<T> type;

    protected AbstractStatement(Persistor persistor, Class<T> type) {
        this.persistor = persistor;
        this.type = type;
    }

    protected abstract String toSQL();
}
