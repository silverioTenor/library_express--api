package org.libraryexpress.application.book.usecase;

import org.libraryexpress.application.book.dto.response.BookDto;
import org.libraryexpress.application.book.mapper.BookMapper;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.repository.BookRepository;
import org.libraryexpress.domain.core.dto.InputPaginationDto;
import org.libraryexpress.domain.core.dto.OutputPaginationDto;
import org.libraryexpress.domain.core.repository.QueryResult;

import java.util.Set;

public class ListBooks {

    private final BookRepository bookRepository;
    private final BookMapper mapper;

    public ListBooks(BookRepository bookRepository, BookMapper mapper) {
        this.bookRepository = bookRepository;
        this.mapper = mapper;
    }

    public OutputPaginationDto<BookDto> execute(InputPaginationDto paginationDto) {
        QueryResult<Book> result = this.bookRepository.findAll(paginationDto);

        Set<BookDto> booksDto = mapper.toResponseListDto(result.items());

        if  (paginationDto == null || !paginationDto.isPaginated()) {
            return OutputPaginationDto.unpaginated(booksDto);
        }

        int totalPages = Math.toIntExact(result.total() / paginationDto.limit());

        return new OutputPaginationDto<>(
                booksDto,
                paginationDto.page(),
                paginationDto.limit(),
                totalPages,
                result.total()
        );
    }
}
