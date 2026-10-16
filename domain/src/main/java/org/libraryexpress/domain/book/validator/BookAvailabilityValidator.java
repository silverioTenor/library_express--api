package org.libraryexpress.domain.book.validator;

import org.libraryexpress.domain.book.exception.BookNotFoundException;
import org.libraryexpress.domain.book.exception.BookUnavailableException;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.book.repository.BookRepository;
import org.libraryexpress.domain.book.valueobject.Isbn;
import org.libraryexpress.domain.core.repository.QueryResult;
import org.libraryexpress.domain.book.repository.BookFilter;

public class BookAvailabilityValidator {

    private final BookRepository bookRepository;

    public BookAvailabilityValidator(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book validate(String ISBN) {

        QueryResult<Book> result = this.bookRepository.search(
                new BookFilter(new Isbn(ISBN), null, null, null, null, null)
        );
        var books = result.items();

        Book book = books.stream()
                .findFirst()
                .orElseThrow(BookNotFoundException::new);

        if (book.getStatus() != BookStatus.AVAILABLE) {
            throw new BookUnavailableException();
        }

        return book;
    }
}
