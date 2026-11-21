package org.libraryexpress.infrastructure.repository.jdbc;

import org.libraryexpress.domain.core.repository.QueryResult;
import org.libraryexpress.domain.loan.entity.Loan;
import org.libraryexpress.domain.loan.enums.LoanStatus;
import org.libraryexpress.domain.loan.repository.LoanFilter;
import org.libraryexpress.domain.loan.repository.LoanRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

public class LoanDbRepository implements LoanRepository {

    private final DataSource dataSource;

    public LoanDbRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void create(Loan loan) {
        String query =
                "INSERT INTO loan (id, book_id, customer_id, status, start_date, end_date) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {

            statement.setObject(1, UUID.fromString(loan.getId()));
            statement.setObject(2, UUID.fromString(loan.getBookId()));
            statement.setObject(3, UUID.fromString(loan.getCustomerId()));
            statement.setString(4, loan.getStatus().name());
            statement.setObject(5, loan.getStartDate());
            statement.setObject(6, loan.getEndDate());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Fatal database error during loan record registration: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Loan loan) {
        executeUpdate(loan, null);
    }

    @Override
    public boolean updateIfCurrentStatus(Loan loan, LoanStatus expectedStatus) {
        return executeUpdate(loan, expectedStatus) == 1;
    }

    private int executeUpdate(Loan loan, LoanStatus expectedStatus) {
        StringBuilder queryBuilder = new StringBuilder(
                "UPDATE loan SET book_id = ?, customer_id = ?, status = ?, start_date = ?, end_date = ?"
        );

        if (loan.getClosedAt() != null) {
            queryBuilder.append(", closed_at = ? ");
        }

        queryBuilder.append(" WHERE id::text = ?");

        if (expectedStatus != null) {
            queryBuilder.append(" AND status = ?");
        }

        String query = queryBuilder.append(';').toString();

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {
            int parameterIndex = 1;

            statement.setObject(parameterIndex++, UUID.fromString(loan.getBookId()));
            statement.setObject(parameterIndex++, UUID.fromString(loan.getCustomerId()));
            statement.setString(parameterIndex++, loan.getStatus().name());
            statement.setObject(parameterIndex++, loan.getStartDate());
            statement.setObject(parameterIndex++, loan.getEndDate());

            if (loan.getClosedAt() != null) {
                statement.setObject(parameterIndex++, loan.getClosedAt().atOffset(ZoneOffset.UTC));
            }

            statement.setString(parameterIndex++, loan.getId());

            if (expectedStatus != null) {
                statement.setString(parameterIndex, expectedStatus.name());
            }

            return statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Fatal database error during loan record modification: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Loan> findById(String id) {
        String query = "SELECT l.id, l.book_id, l.customer_id, l.status, l.start_date, l.end_date, l.closed_at " +
                "FROM loan l WHERE l.id::text = ?";

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {

            statement.setString(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRowToLoan(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Fatal database error during unique loan query operation: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    @Override
    public QueryResult<Loan> search(LoanFilter filter) {
        boolean shouldPaginate = filter.paginationDto() != null && filter.paginationDto().isPaginated();

        var query = shouldPaginate
                ? new StringBuilder("SELECT l.id, l.book_id, l.customer_id, l.status, l.start_date, l.end_date, l.closed_at, COUNT(*) OVER() as full_count FROM loan l WHERE 1=1")
                : new StringBuilder("SELECT l.id, l.book_id, l.customer_id, l.status, l.start_date, l.end_date, l.closed_at FROM loan l WHERE 1=1");

        List<Loan> loans = new ArrayList<>();
        long totalElements;

        boolean hasId = filter.id() != null && !filter.id().isBlank();
        boolean hasCustomerId = filter.customerId() != null && !filter.customerId().isBlank();
        boolean hasBookId = filter.bookId() != null && !filter.bookId().isBlank();
        boolean hasStatuses = filter.statuses() != null && !filter.statuses().isEmpty();

        if (hasId) query.append(" AND l.id::text = ?");
        if (hasCustomerId) query.append(" AND l.customer_id::text = ?");
        if (hasBookId) query.append(" AND l.book_id::text = ?");
        if (hasStatuses) query.append(" AND l.status = ANY(?)");

        if (shouldPaginate) query.append(" ORDER BY l.created_at, l.id DESC LIMIT ? OFFSET ?;");
        else query.append(" ORDER BY l.created_at DESC;");

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query.toString())
        ) {
            int parameterIndex = 1;

            if (hasId) statement.setString(parameterIndex++, filter.id());
            if (hasCustomerId) statement.setString(parameterIndex++, filter.customerId());
            if (hasBookId) statement.setString(parameterIndex++, filter.bookId());

            if (hasStatuses) {
                String[] statusesStr = filter.statuses().stream()
                        .map(LoanStatus::name)
                        .toArray(String[]::new);

                Array arrayStatus = connection.createArrayOf("varchar", statusesStr);
                statement.setArray(parameterIndex++, arrayStatus);
            }

            if (shouldPaginate) {
                statement.setInt(parameterIndex++, filter.paginationDto().limit());
                statement.setInt(parameterIndex, filter.paginationDto().offset());
            } else {
                connection.setAutoCommit(false);
                statement.setFetchSize(500);
            }

            boolean firstRow = true;
            long countFromWindow = 0;

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {

                    if (shouldPaginate && firstRow) {
                        countFromWindow = resultSet.getLong("full_count");
                        firstRow = false;
                    }

                    loans.add(mapRowToLoan(resultSet));
                }
            }
            totalElements = shouldPaginate ? countFromWindow : loans.size();

        } catch (SQLException e) {
            throw new RuntimeException("Fatal database error executing multi-criteria loans catalog search: " + e.getMessage(), e);
        }

        return new QueryResult<>(loans, totalElements);
    }

    /**
     * Map a loan tuple back into its respective clean domain model abstraction.
     */
    private Loan mapRowToLoan(ResultSet resultSet) throws SQLException {
        OffsetDateTime closedAtOffset = resultSet.getObject("closed_at", OffsetDateTime.class);
        Instant closedAtInstant = (closedAtOffset != null) ? closedAtOffset.toInstant() : null;

        return Loan.restore(
                resultSet.getString("id"),
                resultSet.getString("book_id"),
                resultSet.getString("customer_id"),
                LoanStatus.valueOf(resultSet.getString("status")),
                resultSet.getObject("start_date", LocalDate.class),
                resultSet.getObject("end_date", LocalDate.class),
                closedAtInstant
        );
    }
}
