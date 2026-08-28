package org.libraryexpress.domain.book;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.domain.UnitTest;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.book.valueobject.Isbn;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@DisplayName("Book Entity - Unit Test")
public class BookUnitTest {

    private static final String VALID_ISBN_1 = "978-43-12345-67-8";
    private static final String VALID_ISBN_2 = "111-22-33333-44-5";

    @Test
    @DisplayName("Should build a book instance successfully with findAll provided attributes")
    void shouldBuildBook_whenAllAttributesAreProvided() {
        // Arrange & Act
        Book book = new Book.Builder()
                .setISBN(VALID_ISBN_1)
                .setTitle("Book Test")
                .setAuthor("John Doe")
                .setYear(2008)
                .setStatus(BookStatus.AVAILABLE)
                .build();

        // Assert
        assertNotNull(book);
        assertEquals(new Isbn(VALID_ISBN_1), book.getISBN());
        assertEquals("Book Test", book.getTitle());
        assertEquals("John Doe", book.getAuthor());
        assertEquals(2008, book.getYear());
        assertEquals(BookStatus.AVAILABLE, book.getStatus());
    }

    @Test
    @DisplayName("Should change book status when a different status is provided")
    void shouldChangeStatus_whenNewStatusIsDifferent() {
        // Arrange
        Book book = new Book.Builder()
                .setISBN(VALID_ISBN_1)
                .setTitle("Book Test")
                .setStatus(BookStatus.AVAILABLE)
                .build();

        // Act
        book.changeStatus(BookStatus.BORROWED);

        // Assert
        assertEquals(BookStatus.BORROWED, book.getStatus());
    }

    @Test
    @DisplayName("Should ignore status change when the new status is identical to the current one")
    void shouldIgnoreStatusChange_whenNewStatusIsTheSame() {
        // Arrange
        Book book = new Book.Builder()
                .setISBN(VALID_ISBN_1)
                .setTitle("Book Test")
                .setStatus(BookStatus.AVAILABLE)
                .build();

        // Act
        book.changeStatus(BookStatus.AVAILABLE);

        // Assert
        assertEquals(BookStatus.AVAILABLE, book.getStatus());
    }

    @Test
    @DisplayName("Should consider two books equal when they share the same ISBN, regardless of other fields")
    void shouldConsiderTwoBooksEqual_whenTheyHaveTheSameIsbn() {
        // Arrange
        Book firstBook = new Book.Builder()
                .setISBN(VALID_ISBN_1)
                .setTitle("Book Test")
                .setYear(2008)
                .build();

        Book secondBook = new Book.Builder()
                .setISBN(VALID_ISBN_1)
                .setTitle("Design Patterns")
                .setYear(1994)
                .build();

        // Assert (DDD Identity Rule)
        assertEquals(firstBook, secondBook, "Books with identical ISBN must be equal");
        assertEquals(firstBook.hashCode(), secondBook.hashCode());
    }

    @Test
    @DisplayName("Should sort books alphabetically by title when compared")
    void shouldSortBooksByTitle_whenCompared() {
        // Arrange
        Book bookA = new Book.Builder().setISBN(VALID_ISBN_1).setTitle("Architecture Patterns").build();
        Book bookB = new Book.Builder().setISBN(VALID_ISBN_2).setTitle("Book Test").build();

        // Act & Assert
        assertTrue(bookA.compareTo(bookB) < 0, "'Architecture' should come before 'Book Test'");
        assertTrue(bookB.compareTo(bookA) > 0, "'Book Test' should come after 'Architecture'");
        assertEquals(0, bookA.compareTo(bookA), "Comparing a book to itself must return zero");
    }
}
