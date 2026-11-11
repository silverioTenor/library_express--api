package org.libraryexpress.infrastructure.gateway.googlebooks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.domain.book.valueobject.BookMetadata;
import org.libraryexpress.infrastructure.UnitTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@DisplayName("Google Books metadata parser")
class GoogleBookMetadataParserTest {

    @Test
    @DisplayName("Maps the first result and prefers the ISBN-13 identifier")
    void shouldParseMetadata_whenResponseHasBookResults() {
        String json = """
                {
                  "items": [{
                    "volumeInfo": {
                      "industryIdentifiers": [
                        {"type": "ISBN_10", "identifier": "0132350882"},
                        {"type": "ISBN_13", "identifier": "9780132350884"}
                      ],
                      "title": "Clean Code",
                      "authors": ["Robert C. Martin", "Michael Feathers"],
                      "publishedDate": "2008-08-01",
                      "categories": ["Computers", "Software Engineering"],
                      "description": "A book description"
                    }
                  }]
                }
                """;

        Optional<BookMetadata> parsed = GoogleBookMetadataParser.parse(json);

        assertTrue(parsed.isPresent());
        assertEquals(new BookMetadata(
                "9780132350884",
                "Clean Code",
                "Robert C. Martin, Michael Feathers",
                2008,
                "Computers, Software Engineering",
                "A book description"
        ), parsed.get());
    }

    @Test
    @DisplayName("Falls back to ISBN-10 and supports partial metadata")
    void shouldParsePartialMetadata_whenOptionalFieldsAreAbsent() {
        Optional<BookMetadata> parsed = GoogleBookMetadataParser.parse("""
                {"items":[{"volumeInfo":{
                  "industryIdentifiers":[{"type":"ISBN_10","identifier":"0132350882"}],
                  "title":"Clean Code",
                  "publishedDate":"unknown"
                }}]}
                """);

        assertEquals(Optional.of(new BookMetadata("0132350882", "Clean Code", "", 0, "", "")), parsed);
    }

    @Test
    @DisplayName("Truncates descriptions to the database field limit")
    void shouldTruncateDescription_whenDescriptionExceedsLimit() {
        String description = "x".repeat(501);
        String json = """
                {"items":[{"volumeInfo":{"description":"%s"}}]}
                """.formatted(description);

        String parsedDescription = GoogleBookMetadataParser.parse(json).orElseThrow().description();

        assertEquals(500, parsedDescription.length());
        assertTrue(parsedDescription.endsWith("..."));
    }

    @Test
    @DisplayName("Returns no metadata for empty, malformed, or result-free responses")
    void shouldReturnEmpty_whenResponseCannotBeParsedOrHasNoItems() {
        assertEquals(Optional.empty(), GoogleBookMetadataParser.parse(null));
        assertEquals(Optional.empty(), GoogleBookMetadataParser.parse(" "));
        assertEquals(Optional.empty(), GoogleBookMetadataParser.parse("not-json"));
        assertEquals(Optional.empty(), GoogleBookMetadataParser.parse("{\"items\":[]}"));
        assertEquals(Optional.empty(), GoogleBookMetadataParser.parse("{\"items\":{}}"));
    }
}
