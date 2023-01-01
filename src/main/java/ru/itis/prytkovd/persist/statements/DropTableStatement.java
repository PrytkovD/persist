package ru.itis.prytkovd.persist.statements;

import ru.itis.prytkovd.persist.Persistor;

public class DropTableStatement<T> extends AbstractStatement<T> {
    public DropTableStatement(Persistor persistor, Class<T> type) {
        super(persistor, type);
    }

    @Override
    protected String toSQL() {
        return "drop table if exists " + persistor.getTableInfo(type).getName() + " cascade";
    }

    public void execute() {
        persistor.execute(toSQL());
    }
}
