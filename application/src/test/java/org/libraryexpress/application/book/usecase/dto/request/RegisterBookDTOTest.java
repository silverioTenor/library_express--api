package org.libraryexpress.application.book.usecase.dto.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.application.UnitTest;
import org.libraryexpress.application.book.dto.request.RegisterBookDTO;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@DisplayName("RegisterBookDTOTest - Unit Test")
public class RegisterBookDTOTest {

    private static final String ISBN_STR = "9788535902778";

    @Test
    @DisplayName("Should create a new instance of RegisterBookDTO when provide a valid data")
    void shouldCreateNewInstanceOfRegisterBookDTO_whenProvideValidData() {
        Book book = Book.create(ISBN_STR, "Clean Code", "Robert C. Martin", 2008, "AVAILABLE");

        RegisterBookDTO registerBookDTO = new RegisterBookDTO(
                ISBN_STR,
                "Clean Code",
                "Robert C. Martin",
                2008,
                "AVAILABLE"
        );

        Book bookEntity = registerBookDTO.toEntity();

        assertNotNull(registerBookDTO);
        assertNotNull(bookEntity);

        assertEquals(book.getIsbn(), bookEntity.getIsbn());
        assertEquals(book.getTitle(), bookEntity.getTitle());
        assertEquals(book.getAuthor(), bookEntity.getAuthor());
        assertEquals(book.getPublicationYear(), bookEntity.getPublicationYear());
    }

    @Test
    @DisplayName("Should throw InvalidArgumentException when one or more fields are missing or Invalid")
    void shouldThrowInvalidArgumentException_whenOneOrMoreFieldsAreMissingOrInvalid() {
//        TEST 1
        var error1 = assertThrows(InvalidArgumentException.class, () -> {
            new RegisterBookDTO(null, "Clean Code","Robert C. Martin",2008, "AVAILABLE");
        });
        assertEquals("[Isbn, title and author are mandatory]", error1.getMessage());

//        TEST 2
        var error2 = assertThrows(InvalidArgumentException.class, () -> {
            new RegisterBookDTO(ISBN_STR, "","Robert C. Martin",2008, "AVAILABLE");
        });
        assertEquals("[Isbn, title and author are mandatory]", error2.getMessage());

//        TEST 3
        var error3 = assertThrows(InvalidArgumentException.class, () -> {
            new RegisterBookDTO(ISBN_STR,"Clean Code","",2008, "AVAILABLE");
        });
        assertEquals("[Isbn, title and author are mandatory]", error3.getMessage());

//        TEST 4
        var error4 = assertThrows(InvalidArgumentException.class, () -> {
            new RegisterBookDTO(ISBN_STR, "Clean Code", "Robert C. Martin", -10, "AVAILABLE");
        });
        assertEquals("[Publication year is invalid]", error4.getMessage());

//        TEST 4
        var error5 = assertThrows(InvalidArgumentException.class, () -> {
            new RegisterBookDTO(ISBN_STR, "Clean Code", "Robert C. Martin", 1990, "MAINTENANCE");
        });
        assertEquals("[Invalid status provided]", error5.getMessage());

//        TEST 6
        var error6 = assertThrows(InvalidArgumentException.class, () -> {
            new RegisterBookDTO(ISBN_STR, "", "Robert C. Martin", -100, "AVAILABLE");
        });
        assertEquals("[Isbn, title and author are mandatory, Publication year is invalid]", error6.getMessage());
    }
}
