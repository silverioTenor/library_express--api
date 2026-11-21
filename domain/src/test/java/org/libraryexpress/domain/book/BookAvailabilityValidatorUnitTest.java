package org.libraryexpress.domain.book;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        String isbnStr = "978-85-209-2325-2";
        Book book = Book.create(isbnStr, "Book Test", "John Doe", 2008, "AVAILABLE", "", "");
        String bookId = book.getId().toString();

        when(bookRepository.getById(bookId)).thenReturn(Optional.of(book));

        // Act
        Book availableBook = bookAvailabilityValidator.validate(bookId);

        // Assert
        assertNotNull(availableBook);
        assertEquals(book.getIsbn().value(), availableBook.getIsbn().value());
        assertEquals("Book Test", availableBook.getTitle());
        assertEquals("John Doe", availableBook.getAuthors().getFirst().name());
        assertEquals(2008, availableBook.getPublicationYear());
        assertEquals(BookStatus.AVAILABLE, availableBook.getStatus());

        verify(bookRepository, times(1)).getById(bookId);
    }

    @Test
    @DisplayName("Should throw BookNotFoundException when the repository returns an empty set")
    void shouldThrowBookNotFoundException_whenBookIsNotFound() {
        // Arrange
        String bookId = "481-18-70121-40-3";
        when(bookRepository.getById(bookId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BookNotFoundException.class, () -> bookAvailabilityValidator.validate(bookId));
        verify(bookRepository, times(1)).getById(bookId);
    }

    @Test
    @DisplayName("Should throw BookUnavailableException when the book is found but its status is not available")
    void shouldThrowBookUnavailableException_whenBookIsFoundButNotAvailable() {
        // Arrange
        String isbnStr = "978-65-86015-01-0";
        Book borrowedBook = Book.create(isbnStr, "Book Test", "John Doe", 2008, "BORROWED", "", "");
        String bookId = borrowedBook.getId().toString();

        when(bookRepository.getById(bookId)).thenReturn(Optional.of(borrowedBook));

        // Act & Assert
        assertThrows(BookUnavailableException.class, () -> bookAvailabilityValidator.validate(bookId));
        verify(bookRepository, times(1)).getById(bookId);
    }
}
