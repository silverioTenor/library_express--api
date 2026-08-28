package org.libraryexpress.application.book.usecase;

import org.libraryexpress.application.book.dto.response.BookDto;
import org.libraryexpress.application.book.mapper.BookMapper;
import org.libraryexpress.domain.book.repository.BookRepository;
import org.libraryexpress.application.core.dto.InputPaginationDto;
import org.libraryexpress.application.core.dto.OutputPaginationDto;

import java.util.Set;

public class ListBooks {

    private final BookRepository bookRepository;
    private final BookMapper mapper;

    public ListBooks(BookRepository bookRepository, BookMapper mapper) {
        this.bookRepository = bookRepository;
        this.mapper = mapper;
    }

    public OutputPaginationDto<BookDto> execute(InputPaginationDto paginationDto) {
        var books = this.bookRepository.all(paginationDto);

        Set<BookDto> booksDto = mapper.toResponseListDto(books);

        if  (paginationDto == null || !paginationDto.isPaginated()) {
            return OutputPaginationDto.unpaginated(booksDto);
        }

        return new OutputPaginationDto<>(booksDto, paginationDto.page(), paginationDto.page(), booksDto.size());
    }
}
