package org.libraryexpress.application.book.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.libraryexpress.application.UnitTest;
import org.libraryexpress.application.book.dto.request.RegisterBookDTO;
import org.libraryexpress.application.book.mapper.BookMapper;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.book.exception.UniqueIsbnViolationException;
import org.libraryexpress.domain.book.repository.BookRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@UnitTest
@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterBook UseCase - Unit Test")
class RegisterBookTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper mapper;

    @InjectMocks
    private RegisterBook registerBook;

    private static final String ISBN_STR = "978-43-12345-67-8";

    @Test
    @DisplayName("Should successfully register a book when the ISBN does not exist in database")
    void shouldRegisterBook_whenIsbnIsUnique() {
        // Arrange
        RegisterBookDTO requestDto = new RegisterBookDTO(ISBN_STR, "Clean Code", "Robert C. Martin", 2008, BookStatus.AVAILABLE);

        // Setting up a Book.Builder mock since mapper returns a Builder instance chain
        Book.Builder mockBuilder = mock(Book.Builder.class);
        Book targetBook = new Book.Builder()
                .setISBN(ISBN_STR)
                .setTitle("Clean Code")
                .setStatus(BookStatus.AVAILABLE)
                .build();

        // Stubbing repository to simulate an empty database slot and configuring builder fluid chain
        when(bookRepository.getByIsbn(ISBN_STR)).thenReturn(Optional.empty());
        when(mapper.toEntity(requestDto)).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(targetBook);

        // Act
        registerBook.execute(requestDto);

        // Assert & Orchestration verification (style guide compliant: avoiding redundant counters)
        verify(bookRepository).create(targetBook);
    }

    @Test
    @DisplayName("Should throw UniqueIsbnViolationException and abort persistence when ISBN already exists")
    void shouldThrowUniqueIsbnViolationException_whenIsbnAlreadyExists() {
        // Arrange
        RegisterBookDTO requestDto = new RegisterBookDTO(ISBN_STR, "Clean Code", "Robert C. Martin", 2008, BookStatus.AVAILABLE);

        Book existingBook = new Book.Builder()
                .setISBN(ISBN_STR)
                .setTitle("Duplicate Book Entry")
                .build();

        // Stubbing repository query to simulate an existing identifier conflict
        when(bookRepository.getByIsbn(ISBN_STR)).thenReturn(Optional.of(existingBook));

        // Act & Assert
        assertThrows(UniqueIsbnViolationException.class, () -> registerBook.execute(requestDto));

        // Orchestration guard verification: data persistence must be completely bypassed on collision
        verify(bookRepository, never()).create(any(Book.class));
        verifyNoInteractions(mapper);
    }
}
