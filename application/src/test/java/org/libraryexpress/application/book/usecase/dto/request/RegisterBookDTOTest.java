package org.libraryexpress.application.book.usecase.dto.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.application.UnitTest;
import org.libraryexpress.application.book.dto.request.RegisterBookDTO;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@DisplayName("RegisterBookDTO - Unit Test")
class RegisterBookDTOTest {

    private static final String ISBN_STR = "9788535902778";

    @Test
    @DisplayName("Creates a manual-registration entity from valid request data")
    void shouldCreateEntity_whenManualRegistrationDataIsValid() {
        RegisterBookDTO dto = new RegisterBookDTO(
                ISBN_STR, "Clean Code", "Robert C. Martin", 2008, "AVAILABLE"
        );

        Book book = dto.toEntity();

        assertTrue(dto.isManualRegistration());
        assertEquals(ISBN_STR, book.getIsbn().value());
        assertEquals("Clean Code", book.getTitle());
        assertEquals("Robert C. Martin", book.getAuthors().getFirst().name());
        assertEquals(2008, book.getPublicationYear());
    }

    @Test
    @DisplayName("Allows an omitted ISBN for external metadata lookup")
    void shouldIdentifyExternalRegistration_whenIsbnIsMissing() {
        RegisterBookDTO dto = new RegisterBookDTO(null, "Clean Code", "Robert C. Martin", 2008, "AVAILABLE");

        assertFalse(dto.isManualRegistration());
    }

    @Test
    @DisplayName("Rejects missing title and author")
    void shouldThrowInvalidArgumentException_whenTitleOrAuthorIsMissing() {
        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> new RegisterBookDTO(ISBN_STR, " ", null, 2008, "AVAILABLE")
        );

        assertEquals("[Title and author are mandatory]", exception.getMessage());
    }

    @Test
    @DisplayName("Rejects years before zero or in the future")
    void shouldThrowInvalidArgumentException_whenPublicationYearIsOutOfRange() {
        InvalidArgumentException negativeYear = assertThrows(
                InvalidArgumentException.class,
                () -> new RegisterBookDTO(ISBN_STR, "Clean Code", "Robert C. Martin", -1, "AVAILABLE")
        );
        InvalidArgumentException futureYear = assertThrows(
                InvalidArgumentException.class,
                () -> new RegisterBookDTO(ISBN_STR, "Clean Code", "Robert C. Martin", LocalDate.now().getYear() + 1, "AVAILABLE")
        );

        assertEquals("[Publication year is invalid]", negativeYear.getMessage());
        assertEquals("[Publication year is invalid]", futureYear.getMessage());
    }

    @Test
    @DisplayName("Rejects an unknown status")
    void shouldThrowInvalidArgumentException_whenStatusIsInvalid() {
        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> new RegisterBookDTO(ISBN_STR, "Clean Code", "Robert C. Martin", 2008, "MAINTENANCE")
        );

        assertEquals("[Invalid status provided]", exception.getMessage());
    }

    @Test
    @DisplayName("Reports all invalid fields in one validation error")
    void shouldReportAllValidationErrors_whenMultipleFieldsAreInvalid() {
        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> new RegisterBookDTO(null, "", " ", -1, "MAINTENANCE")
        );

        assertEquals("[Title and author are mandatory, Publication year is invalid, Invalid status provided]", exception.getMessage());
    }
}
