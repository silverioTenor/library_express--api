package org.libraryexpress.infrastructure.repository.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.infrastructure.IntegrationTest;
import org.libraryexpress.infrastructure.config.database.PostgresTestContainerConfig;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@IntegrationTest
@DisplayName("Book Repository - Core Operations Integration Test")
class BookDbRepositoryIntegrationTest extends PostgresTestContainerConfig {

    private BookDbRepository bookRepository;

    @BeforeEach
    void setUp() {
        this.bookRepository = new BookDbRepository(dataSource);
    }

    @Test
    @DisplayName("Should successfully persist a new book and retrieve it back by its ISBN identity descriptor")
    void shouldPersistAndRetrieveBook_whenPayloadIsValid() {
        // Arrange (Testing create and getByIsbn)
        String isbn = "978-16-14841-20-0";
        Book newBook = createSampleBook(isbn, "Continuous Delivery", BookStatus.AVAILABLE);

        // Act
        bookRepository.create(newBook);
        Optional<Book> retrieved = bookRepository.getByIsbn(isbn);

        // Assert
        assertTrue(retrieved.isPresent(), "Persisted book must be found by identity inside the container");
        assertEquals("Continuous Delivery", retrieved.get().getTitle());
        assertEquals(BookStatus.AVAILABLE, retrieved.get().getStatus());
    }

    @Test
    @DisplayName("Should throw RuntimeException when trying to insert an identical ISBN business key")
    void shouldThrowRuntimeException_whenIsbnConstraintIsViolatedAtDatabaseLevel() {
        // Arrange (Testing validation exception translation mapping)
        String duplicatedIsbn = "978-13-23590-88-4";
        Book baseBook = createSampleBook(duplicatedIsbn, "Clean Code Core", BookStatus.AVAILABLE);
        bookRepository.create(baseBook);

        Book collidingBook = createSampleBook(duplicatedIsbn, "Clean Code Duplicate Entry Attempt", BookStatus.AVAILABLE);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> bookRepository.create(collidingBook),
                "Repository layer must translate low-level SQL constraint violations into rich domain exception mappings"
        );
    }

    @Test
    @DisplayName("Should successfully modify book fields and reflect state changes inside database during record updates")
    void shouldUpdateBookState_whenModifyingExistingRecord() {
        // Arrange (Testing update)
        String isbn = "978-13-44941-06-6";
        Book book = createSampleBook(isbn, "Clean Architecture Early Draft", BookStatus.AVAILABLE);
        bookRepository.create(book);

        // Modifying entity state via domain patterns (representing an internal update cycle)
        Book updatedBook = new Book.Builder()
                .setISBN(isbn)
                .setTitle("Clean Architecture - Final Release")
                .setAuthor("Robert C. Martin")
                .setYear(2017)
                .setStatus(BookStatus.BORROWED) // Triggering status change translation
                .build();

        // Act
        bookRepository.update(updatedBook);
        Optional<Book> retrieved = bookRepository.getByIsbn(isbn);

        // Assert
        assertTrue(retrieved.isPresent());
        assertEquals("Clean Architecture - Final Release", retrieved.get().getTitle());
        assertEquals(BookStatus.BORROWED, retrieved.get().getStatus());
    }

    @Test
    @DisplayName("Should return a multi-criteria matched dataset when searching with high-performance native ANY clauses")
    void shouldReturnFilteredSet_whenSearchingByCriteriaUsingNativeArrayFilters() {
        // Arrange (Testing search)
        Book book1 = createSampleBook("978-11-11111-01-1", "Design Patterns Base", BookStatus.AVAILABLE);
        Book book2 = createSampleBook("978-22-22222-20-2", "Refactoring Core", BookStatus.BORROWED);
        Book book3 = createSampleBook("978-33-33333-30-3", "Enterprise Integration Patterns", BookStatus.AVAILABLE);

        bookRepository.create(book1);
        bookRepository.create(book2);
        bookRepository.create(book3);

        // Act - Searching using your awesome high-performance ANY arrays filter implementation
        Set<Book> foundBooks = bookRepository.search(null, Set.of(BookStatus.AVAILABLE));

        // Assert
        assertNotNull(foundBooks);
        assertEquals(2, foundBooks.size(), "Should find exactly 2 AVAILABLE books, discarding the BORROWED entry");
        assertTrue(foundBooks.stream().anyMatch(b -> b.getTitle().equals("Design Patterns Base")));
        assertTrue(foundBooks.stream().anyMatch(b -> b.getTitle().equals("Enterprise Integration Patterns")));
    }

    @Test
    @DisplayName("Should successfully scan and retrieve the entire catalog collection payload without criteria constraints")
    void shouldReturnFindAllPersistedBooks_whenTriggeringUnrestrictedCatalogScan() {
        // Arrange (Testing findAll)
        Book book1 = createSampleBook("978-44-44444-40-4", "Domain-Driven Design", BookStatus.AVAILABLE);
        Book book2 = createSampleBook("978-55-55555-50-5", "Implementing Domain-Driven Design", BookStatus.AVAILABLE);

        bookRepository.create(book1);
        bookRepository.create(book2);

        // Act
        Set<Book> allBooks = bookRepository.findAll(null).items();

        // Assert
        assertNotNull(allBooks);
        assertTrue(allBooks.size() >= 2, "Catalog set must retrieve findAll entries currently matching isolation scopes");
    }

    /**
     * Clean helper method to encapsulate recurring builder initializations inside the test suite matrix.
     */
    private Book createSampleBook(String isbn, String title, BookStatus status) {
        return new Book.Builder()
                .setISBN(isbn)
                .setTitle(title)
                .setAuthor("Test Author Reference")
                .setYear(2020)
                .setStatus(status)
                .build();
    }
}
