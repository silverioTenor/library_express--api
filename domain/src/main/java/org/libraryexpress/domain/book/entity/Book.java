package org.libraryexpress.domain.book.entity;

import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.book.valueobject.Author;
import org.libraryexpress.domain.book.valueobject.Category;
import org.libraryexpress.domain.book.valueobject.Isbn;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;
import org.libraryexpress.domain.core.util.RandomGenerator;

import java.util.*;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;

public class Book implements Comparable<Book> {

    private final UUID id;

    private final Isbn isbn;

    private final String title;

    private final List<Author> authors;

    private final int publicationYear;

    private final List<Category> categories;

    private BookStatus status;

    private final String description;

    private Book(
            UUID id,
            Isbn isbn,
            String title,
            List<Author> authors,
            int publicationYear,
            BookStatus status,
            List<Category> categories,
            String description
    ) {
        this.id = id;
        this.isbn = isbn;
        this.title = title;
        this.authors = authors;
        this.publicationYear = publicationYear;
        this.categories = categories;
        this.status = status;
        this.description = description;
    }

    public static Book create(
            String isbn,
            String title,
            String strAuthors,
            int publicationYear,
            String strStatus,
            String strCategories,
            String description
    ) {
        return new Book(
                UUID.randomUUID(),
                new Isbn(isbn),
                title,
                Author.parseMany(strAuthors),
                publicationYear,
                BookStatus.from(strStatus),
                Category.parseMany(strCategories),
                description
        );
    }

    public static Book restore(
            String id,
            String isbn,
            String title,
            String[] authorsArray,
            int publicationYear,
            String strStatus,
            String[] categoryArray,
            String description
    ) {
        return new Book(
                UUID.fromString(id),
                new Isbn(isbn),
                title,
                Author.parseMany(authorsArray),
                publicationYear,
                BookStatus.from(strStatus),
                Category.parseMany(categoryArray),
                description
        );
    }

    public void changeStatus(BookStatus newStatus) {
        if (this.status == newStatus) return;

        this.status = newStatus;
    }

    public UUID getId() {
        return id;
    }

    public Isbn getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public String[] getAuthorsAsArray() {
        return this.authors.stream()
                .map(Author::name)
                .toArray(String[]::new);
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public String[] getCategoriesAsArray() {
        return this.categories.stream()
                .map(Category::name)
                .toArray(String[]::new);
    }

    public BookStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Book book)) return false;
        return Objects.equals(isbn, book.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(isbn);
    }

    @Override
    public int compareTo(Book otherBook) {
        return Objects.compare(this.title, otherBook.getTitle(), String::compareTo);
    }
}
