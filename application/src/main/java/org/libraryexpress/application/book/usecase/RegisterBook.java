package org.libraryexpress.application.book.usecase;

import org.libraryexpress.application.book.dto.request.RegisterBookDTO;
import org.libraryexpress.application.book.dto.response.RegisterBookResponseDTO;
import org.libraryexpress.application.book.mapper.BookMapper;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.book.exception.BookNotFoundException;
import org.libraryexpress.domain.book.exception.UniqueIsbnViolationException;
import org.libraryexpress.domain.book.gateway.BookMetadataGateway;
import org.libraryexpress.domain.book.repository.BookRepository;
import org.libraryexpress.domain.book.valueobject.BookMetadata;
import org.libraryexpress.domain.core.logging.CustomLogger;
import org.libraryexpress.domain.core.logging.CustomLoggerFactory;

import java.util.Optional;

public class RegisterBook {

    private static final CustomLogger log =  CustomLoggerFactory.getLogger(RegisterBook.class);

    private final BookRepository bookRepository;

    private final BookMetadataGateway bookGateway;

    private final BookMapper mapper;

    public RegisterBook(BookRepository bookRepository, BookMetadataGateway bookGateway, BookMapper mapper) {
        this.bookRepository = bookRepository;
        this.bookGateway = bookGateway;
        this.mapper = mapper;
    }

    public RegisterBookResponseDTO execute(RegisterBookDTO registerBookDto) {
        log.info("Starting book registration...");

        Book book = registerBookDto.isManualRegistration()
                ? processManualRegistration(registerBookDto)
                : processExternalRegistration(registerBookDto);

        this.bookRepository.create(book);
        log.info("Book registered.");

        return new RegisterBookResponseDTO(book.getIsbn().value());
    }

    private Book processManualRegistration(RegisterBookDTO dto) {
        var hasRegistered = this.bookRepository.getByIsbn(dto.isbn());

        if (hasRegistered.isPresent()) {
            log.warn("ABORTED: Book already exists. isbn: {}", dto.isbn());
            throw new UniqueIsbnViolationException();
        }

        return dto.toEntity();
    }

    private Book processExternalRegistration(RegisterBookDTO dto) {
        Optional<BookMetadata> bookMetadata = this.bookGateway.fetch(dto.title(), dto.author());

        if (bookMetadata.isEmpty()) {
            log.warn("External book lookup returned empty. Title: [{}], Author: [{}]", dto.title(), dto.author());
            throw new BookNotFoundException(
                    "No book matches the provided title and author in the external registry. Please review the search data or fill in the complete details manually."
            );
        }

        return this.mapper.toEntity(bookMetadata.get());
    }
}
