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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Set;

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
        String IsbnStr = Isbn.generate().value();

        Book book = new Book.Builder()
                .setISBN(IsbnStr)
                .setTitle("Book Test")
                .setAuthor("John Doe")
                .setYear(2008)
                .setStatus(BookStatus.AVAILABLE)
                .build();

        when(bookRepository.search(IsbnStr, null)).thenReturn(List.of(book));

        // Act
        Book availableBook = bookAvailabilityValidator.validate(IsbnStr);

        // Assert
        assertNotNull(availableBook);
        assertEquals(IsbnStr, availableBook.getISBN().value());
        assertEquals("Book Test", availableBook.getTitle());
        assertEquals("John Doe", availableBook.getAuthor());
        assertEquals(2008, availableBook.getYear());
        assertEquals(BookStatus.AVAILABLE, availableBook.getStatus());

        verify(bookRepository, times(1)).search(IsbnStr, null);
    }

    @Test
    @DisplayName("Should throw BookNotFoundException when the repository returns an empty set")
    void shouldThrowBookNotFoundException_whenBookIsNotFound() {
        // Arrange
        String isbnStr = "9780132350884";
        when(bookRepository.search(isbnStr, null)).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(BookNotFoundException.class, () -> bookAvailabilityValidator.validate(isbnStr));
        verify(bookRepository, times(1)).search(isbnStr, null);
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

        when(bookRepository.search(isbnStr, null)).thenReturn(List.of(borrowedBook));

        // Act & Assert
        assertThrows(BookUnavailableException.class, () -> bookAvailabilityValidator.validate(isbnStr));
        verify(bookRepository, times(1)).search(isbnStr, null);
    }
}
