package org.libraryexpress.domain.book.repository;

import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.core.repository.QueryResult;

import java.util.Optional;

public interface BookRepository {
    void create(Book book);
    void update(Book bookToUpdate);
    Optional<Book> getByIsbn(String isbn);
    QueryResult<Book> search(BookFilter filter);
}