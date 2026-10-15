package org.libraryexpress.domain.book.repository;

import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.book.valueobject.Isbn;
import org.libraryexpress.domain.core.dto.InputPaginationDto;

import java.util.Set;

public record BookFilter(
        Isbn ISBN,
        String title,
        String author,
        Integer publishedYear,
        Set<BookStatus> statuses,
        InputPaginationDto paginationDto
) {}
