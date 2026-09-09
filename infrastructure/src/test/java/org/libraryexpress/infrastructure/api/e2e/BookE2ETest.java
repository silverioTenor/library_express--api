package org.libraryexpress.infrastructure.api.e2e;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.application.book.dto.response.BookDto;
import org.libraryexpress.infrastructure.E2ETest;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@E2ETest
@DisplayName("Book Aggregate Endpoints - E2E Test")
class BookE2ETest extends E2EBaseConfig {

    @Test
    @DisplayName("Should register successfully a book and receive a 201 status code")
    void shouldRegisterBook_whenExecutingFullHappyPath() {
        String bookPayload = """
            {
                "ISBN": "995-29-66530-22-4",
                "title": "Clean Architecture Modern Java",
                "author": "John Doe",
                "year": 2026,
                "status": "AVAILABLE"
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .header("X-Trace-Id", "7278e119-b087-4528-87bd-a5e8809b9c49")
                .body(bookPayload)
        .when()
                .post("/books")
        .then()
                .statusCode(201);
    }

    @Test
    @DisplayName("Should get a book by ISBN when the data provided is valid")
    void shouldGetBookPreviouslySaved_whenProvideValidIsbn() {
        given()
                .contentType(ContentType.JSON)
        .when()
                .get("/books/995-29-66530-22-4")
        .then()
                .statusCode(200)
                .body("title", equalTo("Clean Architecture Modern Java"))
                .body("author", equalTo("John Doe"))
                .body("year", equalTo(2026))
                .body("status", equalTo("AVAILABLE"));
    }

    @Test
    @DisplayName("Should get a book by ISBN when the data provided is valid")
    void shouldSuccessfullyListBooks_whenCallEndpoint() {
        BookDto bookDto = get("/books/995-29-66530-22-4").as(BookDto.class);

        given()
                .contentType(ContentType.JSON)
        .when()
                .get("/books")
        .then()
                .statusCode(200)
                .body("items",  hasSize(1))
                .body("page", is(0))
                .body("limit", is(1))
                .body("totalPages", is(1))
                .body("total", is(1))
                .body("items[0].ISBN", is(bookDto.ISBN()));

        given()
                .contentType(ContentType.JSON)
                .param("page", 1)
                .param("limit", 5)
        .when()
                .get("/books")
        .then()
                .statusCode(200)
                .body("page", is(1))
                .body("limit", is(5))
                .body("totalPages", is(0))
                .body("total", is(1));
    }

    @Test
    @DisplayName("Should throw an error with status code 409 when trying register a new book with ISBN previous registered")
    void shouldThrowConflictError_whenTryingRegisterNewBookWithISBNPreviousRegistered() {
        String bookPayload = """
            {
                "ISBN": "995-29-66530-22-4",
                "title": "New Book",
                "author": "Another Author",
                "year": 1999,
                "status": "AVAILABLE"
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(bookPayload)
                .when()
                .post("/books")
                .then()
                .statusCode(409)
                .body("message", equalTo("It is not permitted to register a book with an ISBN that is already in use."));
    }

    @Test
    @DisplayName("Should throw an error with status code 404 when provide an invalid ISBN")
    void shouldThrowError_whenProvideAnInvalidIsbn() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/books/995-29-6XL0-22-4")
                .then()
                .statusCode(404)
                .body("message", equalTo("Book not Found!"));
    }
}
