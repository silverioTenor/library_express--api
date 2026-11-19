package org.libraryexpress.infrastructure.config.database.transaction;

import java.sql.Connection;

public final class TransactionContext {

    private static final ThreadLocal<Connection> CURRENT_CONNECTION = new ThreadLocal<>();

    private TransactionContext() {}

    public static void bind(Connection connection) {
        CURRENT_CONNECTION.set(connection);
    }

    public static Connection getCurrentConnection() {
        return CURRENT_CONNECTION.get();
    }

    public static boolean isTransactionActive() {
        return CURRENT_CONNECTION.get() != null;
    }

    public static void clear() {
        CURRENT_CONNECTION.remove();
    }
}
