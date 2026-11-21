package org.libraryexpress.application.support;

import org.libraryexpress.domain.core.repository.TransactionManager;

import java.util.function.Supplier;

/**
 * Executes the transactional block immediately without opening a real database transaction.
 * Useful for unit tests that only need to verify use-case orchestration.
 */
public final class PassthroughTransactionManager implements TransactionManager {

    @Override
    public <T> T execute(Supplier<T> action) {
        return action.get();
    }
}
