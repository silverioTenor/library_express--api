package org.libraryexpress.domain.book;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.libraryexpress.domain.UnitTest;
import org.libraryexpress.domain.book.valueobject.Isbn;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@DisplayName("ISBN Value Object - Unit Test")
class IsbnTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "0-596-52068-9",
            "0-262-13472-1",
            "0-19-852663-6",
            "0-471-95869-7"
    })
    @DisplayName("Should create an ISBN-13 instance without hyphens when provided an ISBN-10 with hyphens")
    void shouldCreateAnIsbn13InstanceWithoutHyphens_whenProvidedAnIsbn10WithHyphens(String isbn) {
        assertEquals(13, new Isbn(isbn).value().length());
        assertFalse(new Isbn(isbn).value().contains("-"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "978-85-359-0277-8",
            "978-85-7522-458-8",
            "978-85-01-06544-5",
            "978-65-86015-01-0",
            "978-85-209-2325-2"
    })
    @DisplayName("Should create an ISBN-13 instance without hyphens when provided an ISBN-13 with hyphens")
    void shouldCreateAnIsbn13InstanceWithoutHyphens_whenProvidedAnIsbn13WithHyphens(String isbn) {
        assertEquals(isbn.replaceAll("[\\s-]", "").toUpperCase(), new Isbn(isbn).value());
    }

    @Test
    @DisplayName("Should create an ISBN-13 instance without hyphens when provided an ISBN-13 without hyphens")
    void shouldCreateAnIsbn13InstanceWithoutHyphens_whenProvidedAnIsbn13WithoutHyphens() {
        String validIsbnStr = "9784312345678";
        Isbn isbn = new Isbn(validIsbnStr);

        assertEquals("9784312345678", isbn.value());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "123-45-67890-12",
            "978-4-12345-67-8",
            "978-43-1234-67-8",
            "978-43-12345-678-9",
            "abc-de-fghij-kl-m"
    })
    @DisplayName("Should throw an exception when provided an invalid ISBN format or null")
    void shouldThrowAnExceptionWhenProvidedAnInvalidISBNFormatOrNull(String invalidISBNFormat) {
        assertThrows(InvalidArgumentException.class, () -> new Isbn(invalidISBNFormat));
    }
}
