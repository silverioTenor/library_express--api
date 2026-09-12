package org.libraryexpress.infrastructure.repository.InMemory;

import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.book.repository.BookRepository;
import org.libraryexpress.domain.core.dto.InputPaginationDto;
import org.libraryexpress.domain.core.repository.QueryResult;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryBookRepository implements BookRepository {

    private final Map<String, Book> group = new ConcurrentHashMap<>();

    @Override
    public void create(Book book) {
        group.put(book.getISBN().value(), book);
    }

    @Override
    public void update(Book bookToUpdate) {
        Optional.ofNullable(group.get(bookToUpdate.getISBN().value()))
                .ifPresent(book -> book.changeStatus(bookToUpdate.getStatus()));
    }

    @Override
    public Set<Book> search(String ISBN, Set<BookStatus> statuses) {

        Collection<Book> books = (Objects.nonNull(ISBN) && !ISBN.isBlank())
                ? Optional.ofNullable(group.get(ISBN)).map(Set::of).orElse(Set.of())
                : group.values();

        if (Objects.nonNull(statuses) && !statuses.isEmpty()) {
            return books.stream()
                    .filter(book -> statuses.contains(book.getStatus()))
                    .collect(Collectors.toUnmodifiableSet());
        }

        return Set.copyOf(books);
    }

    @Override
    public Optional<Book> getByIsbn(String ISBN) {
        return Optional.ofNullable(group.get(ISBN));
    }

    @Override
    public QueryResult<Book> findAll(InputPaginationDto paginationDto) {
        var books = Set.copyOf(group.values());
        return new QueryResult<>(books, books.size());
    }
}
