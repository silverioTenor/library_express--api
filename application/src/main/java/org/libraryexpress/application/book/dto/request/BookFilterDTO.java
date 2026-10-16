package org.libraryexpress.application.book.dto.request;

import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.book.valueobject.Isbn;
import org.libraryexpress.domain.core.dto.InputPaginationDto;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;
import org.libraryexpress.domain.book.repository.BookFilter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public record BookFilterDTO(
        Isbn ISBN,
        String title,
        String author,
        Integer publishedYear,
        Set<BookStatus> statuses,
        InputPaginationDto paginationDto
) {
    public BookFilterDTO(
            String ISBN,
            String title,
            String author,
            String strStatuses,
            String publishedYear,
            int page,
            int size
    ) {
        this(
            parseIsbn(ISBN),
            title,
            author,
            parsePublishedYear(publishedYear),
            parseStatuses(strStatuses),
            new InputPaginationDto(page, size)
        );
    }

    public BookFilter toBookFilter() {
        return new BookFilter(
                ISBN(),
                title(),
                author(),
                publishedYear(),
                statuses(),
                paginationDto()
        );
    }

    private static Isbn parseIsbn(String ISBN) {
        if (ISBN == null || ISBN.isBlank()) return null;

        return new Isbn(ISBN);
    }

    private static Integer parsePublishedYear(String publishedYear) {
        if (publishedYear == null || publishedYear.isBlank()) return null;

        return Integer.parseInt(publishedYear);
    }

    private static Set<BookStatus> parseStatuses(String strStatuses) {
        if (strStatuses == null || strStatuses.isBlank()) {
            return Set.of();
        }

        try {
            return Arrays.stream(strStatuses.split(","))
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .map(BookStatus::valueOf)
                    .collect(Collectors.toUnmodifiableSet());
        } catch (IllegalArgumentException e) {
            throw new InvalidArgumentException("Invalid status values provided in query params.");
        }
    }
}
