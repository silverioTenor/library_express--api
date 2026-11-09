package org.libraryexpress.domain.book.entity;

import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.book.valueobject.Isbn;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;

import java.util.Objects;

public class Book implements Comparable<Book> {

    private final Isbn isbn;

    private final String title;

    private final String author;

    private final int publicationYear;

    private BookStatus status;

    private Book(Isbn isbn, String title, String author, int publicationYear, BookStatus status) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publicationYear = publicationYear;
        this.status = status;
    }

    public static Book create(String isbn, String title, String author, int publicationYear, String strStatus) {
        BookStatus status = (strStatus != null && !strStatus.isBlank())
                ? BookStatus.valueOf(strStatus)
                : BookStatus.AVAILABLE;

        return new Book(new Isbn(isbn), title, author, publicationYear, status);
    }

    public void changeStatus(BookStatus newStatus) {
        if (this.status == newStatus) return;

        this.status = newStatus;
    }

    public Isbn getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public BookStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "{\n" +
                " isbn: " + isbn + ",\n" +
                " title: " + title + ",\n" +
                " author: " + author + ",\n" +
                " publicationYear: " + publicationYear + ",\n" +
                " statuses: " + status + ",\n" +
                "}";
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
