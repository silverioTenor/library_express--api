package org.libraryexpress.application.book.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.libraryexpress.application.UnitTest;
import org.libraryexpress.application.book.dto.request.RegisterBookDTO;
import org.libraryexpress.application.book.dto.response.RegisterBookResponseDTO;
import org.libraryexpress.application.book.mapper.BookMapper;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.book.exception.BookNotFoundException;
import org.libraryexpress.domain.book.exception.UniqueIsbnViolationException;
import org.libraryexpress.domain.book.gateway.BookMetadataGateway;
import org.libraryexpress.domain.book.repository.BookRepository;
import org.libraryexpress.domain.book.valueobject.BookMetadata;
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
    private BookMetadataGateway bookGateway;

    @Mock
    private BookMapper mapper;

    @InjectMocks
    private RegisterBook registerBook;

    private static final String ISBN_STR = "9788535902778";

    @Test
    @DisplayName("Registers a manual book when its ISBN is unique")
    void shouldRegisterBook_whenIsbnIsUnique() {
        RegisterBookDTO requestDto = new RegisterBookDTO(ISBN_STR, "Clean Code", "Robert C. Martin", 2008, "AVAILABLE");
        Book expectedBook = Book.create(ISBN_STR, "Clean Code", "Robert C. Martin", 2008, "AVAILABLE", "", "");
        when(bookRepository.getByIsbn(ISBN_STR)).thenReturn(Optional.empty());

        RegisterBookResponseDTO response = registerBook.execute(requestDto);

        assertEquals(ISBN_STR, response.isbn());
        verify(bookRepository).create(expectedBook);
        verifyNoInteractions(bookGateway, mapper);
    }

    @Test
    @DisplayName("Rejects a manual book when its ISBN already exists")
    void shouldThrowUniqueIsbnViolationException_whenIsbnAlreadyExists() {
        RegisterBookDTO requestDto = new RegisterBookDTO(ISBN_STR, "Clean Code", "Robert C. Martin", 2008, "AVAILABLE");
        Book existingBook = Book.create(ISBN_STR, "Duplicate Book Entry", "Another Author", 2008, "AVAILABLE", "", "");
        when(bookRepository.getByIsbn(ISBN_STR)).thenReturn(Optional.of(existingBook));

        assertThrows(UniqueIsbnViolationException.class, () -> registerBook.execute(requestDto));

        verify(bookRepository, never()).create(any(Book.class));
        verifyNoInteractions(bookGateway, mapper);
    }

    @Test
    @DisplayName("Registers an externally looked-up book when its metadata is found")
    void shouldRegisterBook_whenExternalMetadataIsFound() {
        RegisterBookDTO requestDto = new RegisterBookDTO(null, "Clean Code", "Robert C. Martin", 2008, "AVAILABLE");
        BookMetadata metadata = new BookMetadata(ISBN_STR, "Clean Code", "Robert C. Martin", 2008, "Software", "A guide");
        Book expectedBook = Book.create(ISBN_STR, "Clean Code", "Robert C. Martin", 2008, "AVAILABLE", "Software", "A guide");
        when(bookGateway.fetch(requestDto.title(), requestDto.author())).thenReturn(Optional.of(metadata));
        when(mapper.toEntity(metadata, requestDto.status())).thenReturn(expectedBook);

        RegisterBookResponseDTO response = registerBook.execute(requestDto);

        assertEquals(ISBN_STR, response.isbn());
        verify(mapper).toEntity(metadata, requestDto.status());
        verify(bookRepository).create(expectedBook);
        verify(bookRepository).getByIsbn(ISBN_STR);
    }

    @Test
    @DisplayName("Rejects external registration when metadata cannot be found")
    void shouldThrowBookNotFoundException_whenExternalMetadataIsMissing() {
        RegisterBookDTO requestDto = new RegisterBookDTO(null, "Unknown title", "Unknown author", 2008, "AVAILABLE");
        when(bookGateway.fetch(requestDto.title(), requestDto.author())).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> registerBook.execute(requestDto));

        verifyNoInteractions(mapper);
        verify(bookRepository, never()).create(any(Book.class));
        verify(bookRepository, never()).getByIsbn(anyString());
    }

    @Test
    @DisplayName("Rejects an externally found book when its ISBN already exists")
    void shouldThrowUniqueIsbnViolationException_whenExternalMetadataIsAlreadyRegistered() {
        RegisterBookDTO requestDto = new RegisterBookDTO(null, "Clean Code", "Robert C. Martin", 2008, "AVAILABLE");
        BookMetadata metadata = new BookMetadata(ISBN_STR, "Clean Code", "Robert C. Martin", 2008, "", "");
        Book existingBook = Book.create(ISBN_STR, "Clean Code", "Robert C. Martin", 2008, "AVAILABLE", "", "");
        Book mappedBook = Book.create(ISBN_STR, "Clean Code", "Robert C. Martin", 2008, "AVAILABLE", "", "");
        when(bookGateway.fetch(requestDto.title(), requestDto.author())).thenReturn(Optional.of(metadata));
        when(mapper.toEntity(metadata, requestDto.status())).thenReturn(mappedBook);
        when(bookRepository.getByIsbn(ISBN_STR)).thenReturn(Optional.of(existingBook));

        assertThrows(UniqueIsbnViolationException.class, () -> registerBook.execute(requestDto));

        verify(bookRepository, never()).create(any(Book.class));
    }
}
