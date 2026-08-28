package org.libraryexpress.infrastructure.repository.jdbc;

import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.book.repository.BookRepository;
import org.libraryexpress.domain.core.dto.InputPaginationDto;
import org.libraryexpress.domain.core.repository.QueryResult;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class BookDbRepository implements BookRepository {

    private final DataSource dataSource;

    public BookDbRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    @Override
    public void create(Book book) {
        String query = "INSERT INTO tb_book (isbn, title, author, year_published, status) VALUES (?, ?, ?, ?, ?)";

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {
            statement.setString(1, book.getISBN().value());
            statement.setString(2, book.getTitle());
            statement.setString(3, book.getAuthor());
            statement.setInt(4, book.getYear());
            statement.setString(5, book.getStatus().name());
            statement.execute();

        } catch (SQLException e) {
            throw new RuntimeException("Fatal database error during book record insertion: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Book book) {
        String query = "UPDATE tb_book SET title = ?, author = ?, year_published = ?, status = ? WHERE isbn = ?";

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {
            statement.setString(1, book.getTitle());
            statement.setString(2, book.getAuthor());
            statement.setInt(3, book.getYear());
            statement.setString(4, book.getStatus().name());
            statement.setString(5, book.getISBN().value());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Fatal database error during book record modifications: " + e.getMessage(), e);
        }
    }

    @Override
    public Set<Book> search(String ISBN, Set<BookStatus> statuses) {
        // Dynamic search query assembly based on parameter presence flags
        StringBuilder query = new StringBuilder(
                "SELECT isbn, title, author, year_published, status FROM tb_book WHERE 1=1"
        );

        boolean hasIsbnFilter = ISBN != null && !ISBN.isBlank();
        boolean hasStatusFilter = statuses != null && !statuses.isEmpty();

        if (hasIsbnFilter) query.append(" AND isbn = ?");

        // Dynamically building the IN (?, ?, ...) clause to match the Set collections limit safely
        if (hasStatusFilter) query.append(" AND status = ANY(?)");

        Set<Book> books = new HashSet<>();

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query.toString())
        ) {
            int parameterIndex = 1;

            if (hasIsbnFilter) {
                statement.setString(parameterIndex++, ISBN);
            }

            if (hasStatusFilter) {
                String[] statusStr = statuses.stream()
                        .map(BookStatus::name)
                        .toArray(String[]::new);

                Array statusArray = connection.createArrayOf("varchar", statusStr);
                statement.setArray(parameterIndex++, statusArray);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    books.add(mapRowToBook(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fatal database error executing multi-criteria books catalog search: " + e.getMessage(), e);
        }

        return books;
    }

    @Override
    public Optional<Book> getByIsbn(String ISBN) {
        String query = "SELECT isbn, title, author, year_published, status FROM tb_book WHERE isbn = ?";

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
            throw new RuntimeException("Fatal database error executing book payload query by ISBN: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    @Override
    public QueryResult<Book> findAll(InputPaginationDto paginationDto) {
        boolean shouldPaginate = paginationDto != null && paginationDto.isPaginated();

        String query = shouldPaginate
                ? "SELECT *, COUNT(*) OVER() as full_count FROM tb_book LIMIT ? OFFSET ?"
                : "SELECT * FROM books";

        Set<Book> books = new HashSet<>();
        long totalElements;

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
        ) {
            if (shouldPaginate) {
                statement.setInt(1, paginationDto.limit());
                statement.setInt(2, paginationDto.offset());
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
        return new Book.Builder()
                .setISBN(resultSet.getString("isbn"))
                .setTitle(resultSet.getString("title"))
                .setAuthor(resultSet.getString("author"))
                .setYear(resultSet.getInt("year_published"))
                .setStatus(BookStatus.valueOf(resultSet.getString("status"))) // Parsing plain database VARCHAR back to rich enum
                .build();
    }
}
