package org.libraryexpress.infrastructure.repository.jdbc;

import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.book.repository.BookRepository;
import org.libraryexpress.domain.core.repository.QueryResult;
import org.libraryexpress.domain.book.repository.BookFilter;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class BookDbRepository implements BookRepository {

    private final DataSource dataSource;

    public BookDbRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    @Override
    public void create(Book book) {
        String query = "INSERT INTO book (id, isbn, title, publication_year, status, description, author, category) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {
            Array authorSqlArray = connection.createArrayOf("text", book.getAuthorsAsArray());
            Array categorySqlArray = connection.createArrayOf("text", book.getCategoriesAsArray());

            int parameterIndex = 1;

            statement.setObject(parameterIndex++, book.getId());
            statement.setString(parameterIndex++, book.getIsbn().value());
            statement.setString(parameterIndex++, book.getTitle());
            statement.setInt(parameterIndex++, book.getPublicationYear());
            statement.setString(parameterIndex++, book.getStatus().name());
            statement.setString(parameterIndex++, book.getDescription());
            statement.setArray(parameterIndex++, authorSqlArray);
            statement.setArray(parameterIndex, categorySqlArray);

            statement.execute();

        } catch (SQLException e) {
            throw new RuntimeException("Fatal database error during book record insertion: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Book book) {
        String query = "UPDATE book SET title = ?, publication_year = ?, status = ?, description = ?, author = ?, category = ? WHERE id = ?";

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {
            Array authorSqlArray = connection.createArrayOf("text", book.getAuthorsAsArray());
            Array categorySqlArray = connection.createArrayOf("text", book.getCategoriesAsArray());

            int parameterIndex = 1;

            statement.setString(parameterIndex++, book.getTitle());
            statement.setInt(parameterIndex++, book.getPublicationYear());
            statement.setString(parameterIndex++, book.getStatus().name());
            statement.setString(parameterIndex++, book.getDescription());
            statement.setArray(parameterIndex++, authorSqlArray);
            statement.setArray(parameterIndex++, categorySqlArray);
            statement.setObject(parameterIndex, book.getId());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Fatal database error during book record modifications: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Book> getByIsbn(String ISBN) {
        String query = "SELECT id, isbn, title, author, publication_year, status, category, description FROM book WHERE isbn = ?";

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {

            statement.setString(1, ISBN);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRowToBook(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Fatal database error executing book payload query by isbn: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    @Override
    public QueryResult<Book> search(BookFilter filter) {
        boolean shouldPaginate = filter.paginationDto() != null && filter.paginationDto().isPaginated();

        var query = shouldPaginate
                ? new StringBuilder("SELECT *, COUNT(*) OVER() as full_count FROM book WHERE 1=1")
                : new StringBuilder("SELECT * FROM book WHERE 1=1");

        List<Book> books = new ArrayList<>();
        long totalElements;

        boolean hasIsbnFilter = filter.ISBN() != null && !filter.ISBN().value().isBlank();
        boolean hasStatusFilter = filter.statuses() != null && !filter.statuses().isEmpty();

        if (hasIsbnFilter) query.append(" AND isbn = ?");

        // Dynamically building the IN (?, ?, ...) clause to match the Set collections size safely
        if (hasStatusFilter) query.append(" AND status = ANY(?)");

        if (shouldPaginate) query.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?;");
        else query.append(" ORDER BY created_at DESC;");

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query.toString());
        ) {
            int parameterIndex = 1;

            if (hasIsbnFilter) {
                statement.setString(parameterIndex++, filter.ISBN().value());
            }

            if (hasStatusFilter) {
                String[] statusStr = filter.statuses().stream()
                        .map(BookStatus::name)
                        .toArray(String[]::new);

                Array statusArray = connection.createArrayOf("varchar", statusStr);
                statement.setArray(parameterIndex++, statusArray);
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

                    books.add(mapRowToBook(resultSet));
                }
            }
            totalElements = shouldPaginate ? countFromWindow : books.size();

        } catch (SQLException e) {
            throw new RuntimeException("Fatal database error executing unrestricted books catalog query: " + e.getMessage(), e);
        }

        return new QueryResult<Book>(books, totalElements);
    }

    /**
     * Map a book tuple back into its respective clean domain model abstraction.
     */
    private Book mapRowToBook(ResultSet resultSet) throws SQLException {
        Array authorArray = resultSet.getArray("author");
        String[] authors = (authorArray != null) ? (String[]) authorArray.getArray() : new String[0];

        Array categoryArray = resultSet.getArray("category");
        String[] categories = (categoryArray != null) ? (String[]) categoryArray.getArray() : new String[0];

        return Book.restore(
                resultSet.getString("id"),
                resultSet.getString("isbn"),
                resultSet.getString("title"),
                authors,
                resultSet.getInt("publication_year"),
                resultSet.getString("status"),
                categories,
                resultSet.getString("description")
        );
    }
}
