package org.libraryexpress.application.book.mapper;

import org.libraryexpress.application.book.dto.response.BookDTO;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.book.valueobject.Author;
import org.libraryexpress.domain.book.valueobject.BookMetadata;
import org.libraryexpress.domain.book.valueobject.Category;
import org.libraryexpress.domain.book.valueobject.Isbn;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(unmappedTargetPolicy = ReportingPolicy.WARN)
public interface BookMapper {

    BookMapper INSTANCE = Mappers.getMapper(BookMapper.class);

//    @Mapping(target = "authors", source = "author")
//    @Mapping(target = "categories", source = "category")
    BookDTO toResponseDto(Book book);

    List<BookDTO> toResponseListDto(List<Book> books);

    default String mapAuthors(List<Author> authors) {
        return authors == null
                ? null
                : authors.stream().map(Author::name).collect(Collectors.joining(", "));
    }

    default Book toEntity(BookMetadata bookMetadata, String status) {
        return Book.create(
                bookMetadata.isbn(),
                bookMetadata.title(),
                bookMetadata.author(),
                bookMetadata.publicationYear(),
                status,
                bookMetadata.category(),
                bookMetadata.description()
        );
    }

    @Named("stringToIsbn")
    default Isbn mapStringToIsbn(String value) {
        return value != null ? new Isbn(value) : null;
    }

    default String mapIsbnToString(Isbn ISBN) {
        return ISBN != null ? ISBN.value() : null;
    }

    default String mapAuthorToString(Author author) {
        return author != null ? author.name() : null;
    }

    default String mapCategoryToString(Category category) {
        return category != null ? category.name() : null;
    }
}
