package org.libraryexpress.infrastructure.repository.jdbc;

import org.libraryexpress.domain.core.logging.CustomLogger;
import org.libraryexpress.domain.core.logging.CustomLoggerFactory;
import org.libraryexpress.domain.core.repository.TransactionManager;
import org.libraryexpress.infrastructure.config.database.transaction.TransactionContext;
import org.libraryexpress.infrastructure.exception.InfrastructureException;
import org.libraryexpress.infrastructure.exception.TransactionException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

public class TransactionDb implements TransactionManager {

    private static final CustomLogger LOG = CustomLoggerFactory.getLogger(TransactionDb.class);

    private  final DataSource dataSource;

    public TransactionDb(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public <T> T execute(Supplier<T> action) {
        if (TransactionContext.isTransactionActive()) {
            return action.get();
        }

        Connection connection = null;
        boolean originalAutoCommit = true;

        try {
            connection = dataSource.getConnection();
            originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            TransactionContext.bind(connection);

            T result = action.get();

            connection.commit();
            return result;

        } catch (Throwable e) {
            if (connection != null) {
                try {
                    LOG.warn("Rolling back transaction due to exception: {}", e.getMessage());
                    connection.rollback();
                } catch (SQLException ex) {
                    e.addSuppressed(ex);
                    LOG.error("Failed to rollback transaction", ex);
                }
            }

            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new TransactionException("Unexpected error during transaction execution: " + e.getMessage());
        } finally {
            TransactionContext.clear();

            if (connection != null) {
                try {
                    connection.setAutoCommit(originalAutoCommit);
                } catch (SQLException e) {
                    LOG.error("Failed to reset autoCommit or close connection", e);
                }
            }
        }
    }
}
