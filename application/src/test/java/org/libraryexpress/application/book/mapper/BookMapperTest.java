package org.libraryexpress.application.book.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.application.UnitTest;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.valueobject.BookMetadata;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@DisplayName("Book mapper")
class BookMapperTest {

    @Test
    @DisplayName("Maps author value objects to the response's comma-separated author field")
    void shouldMapAuthors_whenCreatingBookResponse() {
        Book book = Book.create("9780132350884", "Clean Code", "Robert C. Martin, Michael Feathers", 2008, "AVAILABLE", "", "");

        var response = BookMapper.INSTANCE.toResponseDto(book);

        assertEquals("Robert C. Martin, Michael Feathers", response.author());
    }

    @Test
    @DisplayName("Maps external book metadata including categories and description")
    void shouldMapMetadata_whenCreatingBookEntity() {
        BookMetadata metadata = new BookMetadata(
                "9780132350884",
                "Clean Code",
                "Robert C. Martin",
                2008,
                "Software",
                "A programming guide"
        );

        Book book = BookMapper.INSTANCE.toEntity(metadata, "AVAILABLE");

        assertEquals(metadata.isbn(), book.getIsbn().value());
        assertEquals(metadata.category(), book.getCategories().getFirst().name());
        assertEquals(metadata.description(), book.getDescription());
    }
}
