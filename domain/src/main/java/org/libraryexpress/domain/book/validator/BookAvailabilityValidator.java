package org.libraryexpress.domain.book.validator;

import org.libraryexpress.domain.book.exception.BookNotFoundException;
import org.libraryexpress.domain.book.exception.BookUnavailableException;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.book.repository.BookRepository;
public class BookAvailabilityValidator {

    private final BookRepository bookRepository;

    public BookAvailabilityValidator(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book validate(String bookId) {
        Book book = this.bookRepository.getById(bookId)
                .orElseThrow(BookNotFoundException::new);

        if (book.getStatus() != BookStatus.AVAILABLE) {
            throw new BookUnavailableException();
        }

        return book;
    }
}
