package org.libraryexpress.domain.book.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.domain.UnitTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@DisplayName("Category value object")
class CategoryTest {

    @Test
    @DisplayName("Trims and parses comma-separated categories")
    void shouldParseManyCategories_whenInputIsValid() {
        assertEquals(
                List.of(new Category("Science"), new Category("Technology")),
                Category.parseMany(" Science, Technology ")
        );
    }

    @Test
    @DisplayName("Treats missing and blank categories as an empty list")
    void shouldReturnNoCategories_whenInputIsMissing() {
        assertEquals(List.of(), Category.parseMany((String) null));
        assertEquals(List.of(), Category.parseMany(" "));
        assertEquals(List.of(), Category.parseMany(new String[0]));
    }
}
