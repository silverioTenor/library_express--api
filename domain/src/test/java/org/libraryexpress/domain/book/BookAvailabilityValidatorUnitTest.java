package org.libraryexpress.domain.book;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.libraryexpress.domain.UnitTest;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.book.exception.BookNotFoundException;
import org.libraryexpress.domain.book.exception.BookUnavailableException;
import org.libraryexpress.domain.book.repository.BookRepository;
import org.libraryexpress.domain.book.validator.BookAvailabilityValidator;
import org.libraryexpress.domain.book.valueobject.Isbn;
import org.libraryexpress.domain.core.repository.QueryResult;
import org.libraryexpress.domain.book.repository.BookFilter;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@UnitTest
@ExtendWith(MockitoExtension.class)
@DisplayName("Book Availability Validator - Unit Test")
public class BookAvailabilityValidatorUnitTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookAvailabilityValidator bookAvailabilityValidator;

    @Test
    @DisplayName("Should return the book when its found and status is available")
    void shouldReturnBook_whenBookIsFoundAndAvailable() {
        // Arrange
        String isbnStr = Isbn.generate().value();

        Book book = new Book.Builder()
                .setISBN(isbnStr)
                .setTitle("Book Test")
                .setAuthor("John Doe")
                .setYear(2008)
                .setStatus(BookStatus.AVAILABLE)
                .build();

        BookFilter filter = new BookFilter(new Isbn(isbnStr), null, null, null, null, null);
        QueryResult<Book> result = new QueryResult<>(List.of(book), 1);

        when(bookRepository.search(filter)).thenReturn(result);

        // Act
        Book availableBook = bookAvailabilityValidator.validate(isbnStr);

        // Assert
        assertNotNull(availableBook);
        assertEquals(isbnStr, availableBook.getISBN().value());
        assertEquals("Book Test", availableBook.getTitle());
        assertEquals("John Doe", availableBook.getAuthor());
        assertEquals(2008, availableBook.getYear());
        assertEquals(BookStatus.AVAILABLE, availableBook.getStatus());

        verify(bookRepository, times(1)).search(filter);
    }

    @Test
    @DisplayName("Should throw BookNotFoundException when the repository returns an empty set")
    void shouldThrowBookNotFoundException_whenBookIsNotFound() {
        // Arrange
        String isbnStr = "481-18-70121-40-3";
        BookFilter filter = new BookFilter(new Isbn(isbnStr), null, null, null, null, null);
        QueryResult<Book> result = new QueryResult<>(List.of(), 0);

        when(bookRepository.search(filter)).thenReturn(result);

        // Act & Assert
        assertThrows(BookNotFoundException.class, () -> bookAvailabilityValidator.validate(isbnStr));
        verify(bookRepository, times(1)).search(filter);
    }

    @Test
    @DisplayName("Should throw BookUnavailableException when the book is found but its status is not available")
    void shouldThrowBookUnavailableException_whenBookIsFoundButNotAvailable() {
        // Arrange
        String isbnStr = Isbn.generate().value();

        Book borrowedBook = new Book.Builder()
                .setISBN(isbnStr)
                .setTitle("Book Test")
                .setAuthor("John Doe")
                .setYear(2008)
                .setStatus(BookStatus.BORROWED)
                .build();

        BookFilter filter = new BookFilter(new Isbn(isbnStr), null, null, null, null, null);
        QueryResult<Book> result = new QueryResult<>(List.of(borrowedBook), 0);
        when(bookRepository.search(filter)).thenReturn(result);

        // Act & Assert
        assertThrows(BookUnavailableException.class, () -> bookAvailabilityValidator.validate(isbnStr));
        verify(bookRepository, times(1)).search(filter);
    }
}
