package org.libraryexpress.application.book.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.libraryexpress.application.UnitTest;
import org.libraryexpress.application.book.dto.response.BookDTO;
import org.libraryexpress.application.book.mapper.BookMapper;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.book.exception.BookNotFoundException;
import org.libraryexpress.domain.book.repository.BookRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@UnitTest
@ExtendWith(MockitoExtension.class)
@DisplayName("FindBook UseCase - Unit Test")
class FindBookTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper mapper;

    @InjectMocks
    private FindBook findBook;

    private static final String ISBN_STR = "9780140283292";

    @Test
    @DisplayName("Should successfully return the mapped DTO when the book is found by ISBN")
    void shouldReturnBookDto_whenBookExists() {
        // Arrange
        Book foundBook = new Book.Builder()
                .setISBN(ISBN_STR)
                .setTitle("Clean Code")
                .setStatus(BookStatus.AVAILABLE)
                .build();

        BookDTO expectedDto = new BookDTO(ISBN_STR, "Clean Code", "Robert C. Martin", 2008, BookStatus.AVAILABLE);

        // Stubbing repository query and mapper layer conversion
        when(bookRepository.getByIsbn(ISBN_STR)).thenReturn(Optional.of(foundBook));
        when(mapper.toResponseDto(foundBook)).thenReturn(expectedDto);

        // Act
        BookDTO result = findBook.execute(ISBN_STR);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDto, result);

        // Style guide compliance: verify only critical business queries
        verify(bookRepository).getByIsbn(ISBN_STR);
    }

    @Test
    @DisplayName("Should throw BookNotFoundException and never activate mapper when book ID does not exist")
    void shouldThrowBookNotFoundException_whenBookIsNotFound() {
        // Arrange
        when(bookRepository.getByIsbn(ISBN_STR)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BookNotFoundException.class, () -> findBook.execute(ISBN_STR));

        // Orchestration guard: ensure no conversion mappings are triggered if the base entity is missing
        verifyNoInteractions(mapper);
    }
}
