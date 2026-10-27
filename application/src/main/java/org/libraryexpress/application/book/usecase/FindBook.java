package org.libraryexpress.application.book.usecase;

import org.libraryexpress.application.book.dto.response.BookDTO;
import org.libraryexpress.domain.book.exception.BookNotFoundException;
import org.libraryexpress.application.book.mapper.BookMapper;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.repository.BookRepository;
import org.libraryexpress.domain.book.valueobject.Isbn;

import java.util.Optional;

public class FindBook {

    private final BookRepository bookRepository;
    private final BookMapper mapper;

    public FindBook(BookRepository bookRepository, BookMapper mapper) {
        this.bookRepository = bookRepository;
        this.mapper = mapper;
    }

    public BookDTO execute(String ISBN) {
        Isbn isbn = new Isbn(ISBN);

        Optional<Book> book = this.bookRepository.getByIsbn(isbn.value());

        return book
                .map(mapper::toResponseDto)
                .orElseThrow(BookNotFoundException::new);
    }
}
