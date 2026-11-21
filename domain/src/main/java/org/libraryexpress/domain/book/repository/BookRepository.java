package org.libraryexpress.domain.book.repository;

import java.util.Optional;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.core.repository.QueryResult;

public interface BookRepository {
    void create(Book book);

    void update(Book bookToUpdate);

    Optional<Book> getById(String id);

    Optional<Book> getByIsbn(String isbn);

    QueryResult<Book> search(BookFilter filter);
}
