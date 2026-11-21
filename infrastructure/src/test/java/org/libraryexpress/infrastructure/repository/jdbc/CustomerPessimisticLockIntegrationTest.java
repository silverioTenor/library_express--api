package org.libraryexpress.infrastructure.repository.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.domain.customer.entity.Customer;
import org.libraryexpress.infrastructure.IntegrationTest;
import org.libraryexpress.infrastructure.config.database.PostgresTestContainerConfig;
import org.libraryexpress.infrastructure.config.database.transaction.TransactionAwareDataSource;
import org.libraryexpress.infrastructure.config.database.transaction.TransactionContext;
import org.libraryexpress.infrastructure.exception.LockAcquisitionException;

@IntegrationTest
@DisplayName("CustomerDbRepository - Pessimistic Lock Integration Test")
class CustomerPessimisticLockIntegrationTest extends PostgresTestContainerConfig {

    private static final String CUSTOMER_ID = "10000000-0000-4000-8000-000000000101";

    private CustomerDbRepository customerRepository;

    @BeforeEach
    void setUp() {
        customerRepository = new CustomerDbRepository(new TransactionAwareDataSource(dataSource));
        customerRepository.create(Customer.restore(CUSTOMER_ID, "Lock Tester", "lock.tester@libraryexpress.com"));
    }

    @Test
    @DisplayName("Should retrieve the customer while acquiring its row lock in a transaction")
    void shouldLockCustomerRow_whenTransactionIsActive() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            TransactionContext.bind(connection);

            try {
                Customer customer = customerRepository.getById(CUSTOMER_ID).orElseThrow();

                assertEquals(CUSTOMER_ID, customer.getId());
                assertTrue(TransactionContext.isTransactionActive());
            } finally {
                TransactionContext.clear();
                connection.rollback();
            }
        }
    }

    @Test
    @DisplayName("Should fail clearly when another transaction holds the customer row lock")
    void shouldThrowLockAcquisitionException_whenCustomerRowIsAlreadyLocked() throws Exception {
        try (Connection holder = dataSource.getConnection()) {
            holder.setAutoCommit(false);
            setLockTimeout(holder);

            try (PreparedStatement lockStatement =
                            holder.prepareStatement("SELECT id FROM customer WHERE id::text = ? FOR UPDATE");
                    ResultSet ignored = lockCustomer(lockStatement)) {
                assertTrue(ignored.next());
                assertCustomerLockTimesOut();
            } finally {
                holder.rollback();
            }
        }
    }

    @Test
    @DisplayName("Should retrieve the customer without a transaction")
    void shouldRetrieveCustomer_whenNoTransactionIsActive() {
        assertFalse(TransactionContext.isTransactionActive());

        Customer customer = customerRepository.getById(CUSTOMER_ID).orElseThrow();

        assertEquals(CUSTOMER_ID, customer.getId());
    }

    private static void setLockTimeout(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute("SET LOCAL lock_timeout = '500ms'");
        }
    }

    private ResultSet lockCustomer(PreparedStatement statement) throws Exception {
        statement.setString(1, CUSTOMER_ID);
        return statement.executeQuery();
    }

    private void assertCustomerLockTimesOut() throws Exception {
        try (Connection contender = dataSource.getConnection()) {
            contender.setAutoCommit(false);
            setLockTimeout(contender);
            TransactionContext.bind(contender);

            try {
                LockAcquisitionException exception =
                        assertThrows(LockAcquisitionException.class, () -> customerRepository.getById(CUSTOMER_ID));

                assertTrue(exception.getMessage().contains(CUSTOMER_ID));
            } finally {
                TransactionContext.clear();
                contender.rollback();
            }
        }
    }
}
