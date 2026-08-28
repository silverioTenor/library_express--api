package org.libraryexpress.domain.book.repository;

import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.core.dto.InputPaginationDto;

import java.util.Optional;
import java.util.Set;

public interface BookRepository {
    void create(Book book);
    void update(Book bookToUpdate);
    Set<Book> search(String ISBN, Set<BookStatus> statuses);
    Optional<Book> getByIsbn(String isbn);
    Set<Book> all(InputPaginationDto paginationDto);
}
