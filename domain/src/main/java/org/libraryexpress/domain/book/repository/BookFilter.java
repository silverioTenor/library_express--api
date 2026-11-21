package org.libraryexpress.domain.book.repository;

import java.util.Set;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.book.valueobject.Isbn;
import org.libraryexpress.domain.core.dto.InputPaginationDto;

public record BookFilter(
        String id,
        Isbn ISBN,
        String title,
        String author,
        Integer publishedYear,
        Set<BookStatus> statuses,
        InputPaginationDto paginationDto) {}
