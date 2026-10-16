package org.libraryexpress.application.book.mapper;

import org.libraryexpress.application.book.dto.request.RegisterBookDTO;
import org.libraryexpress.application.book.dto.response.BookDTO;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.valueobject.Isbn;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.WARN)
public interface BookMapper {

    BookMapper INSTANCE = Mappers.getMapper(BookMapper.class);

    BookDTO toResponseDto(Book book);

    List<BookDTO> toResponseListDto(List<Book> books);

    @Mapping(target = "ISBN", source = "ISBN", qualifiedByName = "stringToIsbn")
    Book.Builder toEntity(RegisterBookDTO dto);

    @Named("stringToIsbn")
    default Isbn mapStringToIsbn(String value) {
        return value != null ? new Isbn(value) : null;
    }

    default String mapIsbnToString(Isbn ISBN) {
        return ISBN != null ? ISBN.value() : null;
    }
}
