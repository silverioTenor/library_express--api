package org.libraryexpress.application.book.mapper;

import org.libraryexpress.application.book.dto.response.BookDTO;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.book.valueobject.BookMetadata;
import org.libraryexpress.domain.book.valueobject.Isbn;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.WARN)
public interface BookMapper {

    BookMapper INSTANCE = Mappers.getMapper(BookMapper.class);

    BookDTO toResponseDto(Book book);

    List<BookDTO> toResponseListDto(List<Book> books);

    default Book toEntity(BookMetadata bookMetadata) {
        return Book.create(
                bookMetadata.isbn(),
                bookMetadata.title(),
                bookMetadata.author(),
                bookMetadata.publicationYear(),
                null
        );
    }

    @Named("stringToIsbn")
    default Isbn mapStringToIsbn(String value) {
        return value != null ? new Isbn(value) : null;
    }

    default String mapIsbnToString(Isbn ISBN) {
        return ISBN != null ? ISBN.value() : null;
    }
}
