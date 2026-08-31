package org.libraryexpress.infrastructure.repository.jdbc;

import org.libraryexpress.domain.core.dto.InputPaginationDto;
import org.libraryexpress.domain.core.repository.QueryResult;
import org.libraryexpress.domain.loan.entity.Loan;
import org.libraryexpress.domain.loan.enums.LoanStatus;
import org.libraryexpress.domain.loan.repository.LoanRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class LoanDbRepository implements LoanRepository {

    private final DataSource dataSource;

    public LoanDbRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void create(Loan loan) {
        String query =
                "INSERT INTO tb_loan (id, isbn, customer_id, status, start_date, end_date) VALUES (?, ?, ?, ?, ?, ?)";

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {

            statement.setString(1, loan.getId());
            statement.setString(2, loan.getISBN().value());
            statement.setString(3, loan.getCustomerId());
            statement.setString(4, loan.getStatus().name());
            statement.setDate(5, Date.valueOf(loan.getStartDate()));
            statement.setDate(6, Date.valueOf(loan.getEndDate()));

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Fatal database error during loan record registration: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Loan loan) {
        String query =
                "UPDATE tb_loan SET isbn = ?, customer_id = ?, status = ?, start_date = ?, end_date = ? WHERE id = ?";

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {

            statement.setString(1, loan.getISBN().value());
            statement.setString(2, loan.getCustomerId());
            statement.setString(3, loan.getStatus().name());
            statement.setDate(4, Date.valueOf(loan.getStartDate()));
            statement.setDate(5, Date.valueOf(loan.getEndDate()));
            statement.setString(6, loan.getId());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Fatal database error during loan record modification: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Loan> findById(String id) {
        String query = "SELECT id, isbn, customer_id, status, start_date, end_date FROM tb_loan WHERE id = ?";

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
    public QueryResult<Loan> search(
            String customerId,
            String ISBN,
            Set<LoanStatus> statuses,
            InputPaginationDto paginationDto
    ) {
        boolean shouldPaginate = paginationDto != null && paginationDto.isPaginated();

        var query = shouldPaginate
                ? new StringBuilder("SELECT *, COUNT(*) OVER() as full_count FROM tb_loan LIMIT ? OFFSET ? WHERE 1=1")
                : new StringBuilder("SELECT * FROM tb_loan WHERE 1=1");

        Set<Loan> loans = new HashSet<>();
        long totalElements;

        boolean hasCustomerIdFilter = customerId != null && !customerId.isEmpty();
        boolean hasISBNFilter = ISBN != null && !ISBN.isEmpty();
        boolean hasStatusesFilter = statuses != null && !statuses.isEmpty();

        if (hasCustomerIdFilter) query.append(" AND customer_id = ?");
        if (hasISBNFilter) query.append(" AND isbn = ?");
        if (hasStatusesFilter) query.append(" AND status = ANY(?)");

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query.toString())
        ) {
            int parameterIndex = 1;

            if (shouldPaginate) {
                statement.setInt(parameterIndex++, paginationDto.limit());
                statement.setInt(parameterIndex++, paginationDto.offset());
            } else {
                connection.setAutoCommit(false);
                statement.setFetchSize(500);
            }

            if (hasCustomerIdFilter) statement.setString(parameterIndex++, customerId);
            if (hasISBNFilter) statement.setString(parameterIndex++, ISBN);

            if (hasStatusesFilter) {
                String[] statusesStr = statuses.stream()
                        .map(LoanStatus::name)
                        .toArray(String[]::new);

                Array arrayStatus = connection.createArrayOf("varchar", statusesStr);
                statement.setArray(parameterIndex, arrayStatus);
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
        return new Loan.Builder()
                .setId(resultSet.getString("id"))
                .setISBN(resultSet.getString("isbn"))
                .setCustomerId(resultSet.getString("customer_id"))
                .setStatus(LoanStatus.valueOf(resultSet.getString("status")))
                .setStartDate(resultSet.getDate("start_date").toLocalDate())
                .setEndDate(resultSet.getDate("end_date").toLocalDate())
                .build();
    }
}
