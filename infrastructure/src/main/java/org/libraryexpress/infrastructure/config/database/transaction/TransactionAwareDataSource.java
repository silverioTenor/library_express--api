package org.libraryexpress.infrastructure.config.database.transaction;

import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;

public class TransactionAwareDataSource implements DataSource {

    private final DataSource targetDataSource;

    public TransactionAwareDataSource(DataSource targetDataSource) {
        this.targetDataSource = targetDataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection activeConnection = TransactionContext.getCurrentConnection();

        if (activeConnection != null) {
            return createNonClosingProxy(activeConnection);
        }
        return targetDataSource.getConnection();
    }

    /**
     * Creates a lightweight proxy that intercepts the close() method to prevent repository try-with-resources blocks
     * from closing the connection prior to commit/rollback.
     */
    private Connection createNonClosingProxy(Connection targetConnection) {
        InvocationHandler handler = (proxy, method, args) -> {
            if ("close".equals(method.getName())) {
                // Ignores individual repository close() calls; TransactionManager handles final closure.
                return null;
            }

            if ("isClosed".equals(method.getName())) {
                return targetConnection.isClosed();
            }

            return method.invoke(targetConnection, args);
        };

        return (Connection)
                Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[] {Connection.class}, handler);
    }

    @Override
    public Connection getConnection(String s, String s1) throws SQLException {
        return getConnection();
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return targetDataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter printWriter) throws SQLException {
        targetDataSource.setLogWriter(printWriter);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        targetDataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return targetDataSource.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return targetDataSource.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> aClass) throws SQLException {
        return targetDataSource.unwrap(aClass);
    }

    @Override
    public boolean isWrapperFor(Class<?> aClass) throws SQLException {
        return targetDataSource.isWrapperFor(aClass);
    }
}
