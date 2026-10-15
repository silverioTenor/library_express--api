package org.libraryexpress.application.book.usecase;

import org.libraryexpress.application.book.dto.request.BookFilterDTO;
import org.libraryexpress.application.book.dto.response.BookDto;
import org.libraryexpress.application.book.mapper.BookMapper;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.repository.BookRepository;
import org.libraryexpress.domain.core.dto.OutputPaginationDto;
import org.libraryexpress.domain.core.repository.QueryResult;

import java.util.List;

public class SearchBooks {

    private final BookRepository bookRepository;
    private final BookMapper mapper;

    public SearchBooks(BookRepository bookRepository, BookMapper mapper) {
        this.bookRepository = bookRepository;
        this.mapper = mapper;
    }

    public OutputPaginationDto<BookDto> execute(BookFilterDTO bookFilterDto) {
        QueryResult<Book> result = this.bookRepository.search(bookFilterDto.toBookFilter());

        List<BookDto> booksDto = mapper.toResponseListDto(result.items());

        if  (bookFilterDto.paginationDto() == null || !bookFilterDto.paginationDto().isPaginated()) {
            return OutputPaginationDto.unpaginated(booksDto);
        }

        int totalPages = Math.toIntExact(result.total() / bookFilterDto.paginationDto().limit());

        return new OutputPaginationDto<>(
                booksDto,
                bookFilterDto.paginationDto().page(),
                bookFilterDto.paginationDto().limit(),
                totalPages,
                result.total()
        );
    }
}
