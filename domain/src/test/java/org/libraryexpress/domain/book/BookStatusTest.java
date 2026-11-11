package org.libraryexpress.domain.book;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.domain.UnitTest;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@UnitTest
@DisplayName("Book status")
class BookStatusTest {

    @Test
    @DisplayName("Defaults absent status to available and parses valid values case-insensitively")
    void shouldParseStatus_whenStatusIsMissingOrValid() {
        assertEquals(BookStatus.AVAILABLE, BookStatus.from(null));
        assertEquals(BookStatus.AVAILABLE, BookStatus.from(" "));
        assertEquals(BookStatus.BORROWED, BookStatus.from(" borrowed "));
        assertEquals(BookStatus.UNAVAILABLE, BookStatus.from("UNAVAILABLE"));
    }

    @Test
    @DisplayName("Rejects an unknown status")
    void shouldRejectStatus_whenValueIsUnknown() {
        assertThrows(InvalidArgumentException.class, () -> BookStatus.from("MAINTENANCE"));
    }
}
