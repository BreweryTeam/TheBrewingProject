package dev.jsinco.brewery.database.sql;

import dev.jsinco.brewery.database.*;

import java.sql.Connection;

public interface SqlStoredData {

    interface Findable<T, U> extends FindableStoredData<T, U, Connection> {
    }

    interface Insertable<T> extends InsertableStoredData<T, Connection> {
    }

    interface Removable<T> extends RemovableStoredData<T, Connection> {
    }

    interface Retrievable<T> extends RetrievableStoredData<T, Connection> {
    }

    interface Singleton<T> extends SingletonStoredData<T, Connection> {
    }

    interface Updateable<T> extends UpdateableStoredData<T, Connection> {
    }
}
