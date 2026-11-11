package org.libraryexpress.domain.book.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.domain.UnitTest;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@DisplayName("Author value object")
class AuthorTest {

    @Test
    @DisplayName("Trims and parses comma-separated authors")
    void shouldParseManyAuthors_whenInputIsValid() {
        List<Author> authors = Author.parseMany(" Ada Lovelace,  Grace Hopper ");

        assertEquals(List.of(new Author("Ada Lovelace"), new Author("Grace Hopper")), authors);
    }

    @Test
    @DisplayName("Rejects missing author data")
    void shouldRejectMissingAuthors() {
        assertThrows(InvalidArgumentException.class, () -> Author.parseMany((String) null));
        assertThrows(InvalidArgumentException.class, () -> Author.parseMany("   "));
        assertThrows(InvalidArgumentException.class, () -> Author.parseMany(" , "));
        assertThrows(InvalidArgumentException.class, () -> new Author(" "));
        assertThrows(InvalidArgumentException.class, () -> Author.parseMany(new String[]{" ", null}));
    }

    @Test
    @DisplayName("Parses an array and ignores blank entries")
    void shouldParseAuthorArray_whenValuesArePresent() {
        assertEquals(List.of(new Author("Ada Lovelace")), Author.parseMany(new String[]{" Ada Lovelace ", ""}));
    }
}
