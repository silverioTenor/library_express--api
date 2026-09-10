package org.libraryexpress.infrastructure.api.e2e;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.libraryexpress.application.book.dto.response.BookDto;
import org.libraryexpress.application.customer.dto.response.CustomerDto;
import org.libraryexpress.application.loan.dto.response.LoanDto;
import org.libraryexpress.domain.core.dto.OutputPaginationDto;
import org.libraryexpress.infrastructure.E2ETest;
import org.libraryexpress.infrastructure.config.ConfigRegistry;

import javax.sql.DataSource;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@E2ETest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Loan Aggregate endpoint - E2E Test")
public class LoanE2ETest extends E2EBaseConfig {

    @Test
    @Order(1)
    @DisplayName("Should create a new loan when all data provided is valid")
    void shouldCreateLoanSuccessfully_whenProvideValidData() {
        TestDataFactory.createCustomer("j.doe@test.com");
        TestDataFactory.createBook("408-36-74383-60-6");

        var customer = TestDataFactory.getCustomer("j.doe@test.com");
        var book = TestDataFactory.getBook("408-36-74383-60-6");

        String payload = """
                {
                    "customerId": "%s",
                    "ISBN": "%s"
                }
                """.formatted(customer.id(), book.ISBN());

        given()
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .post("/loans")
        .then()
            .statusCode(201);
    }

    @Test
    @Order(2)
    @DisplayName("Should search loans when provide a valid parameters")
    void shouldSearchLoansSuccessfully_whenProvideValidParameters() {
        var customer = TestDataFactory.getCustomer("j.doe@test.com");
        var book = TestDataFactory.getBook("408-36-74383-60-6");

        given()
            .contentType(ContentType.JSON)
            .param("customerId", customer.id())
        .when()
            .get("/loans")
        .then()
            .statusCode(200);

        given()
            .contentType(ContentType.JSON)
            .param("ISBN", book.ISBN())
        .when()
            .get("/loans")
        .then()
            .statusCode(200);

        given()
            .contentType(ContentType.JSON)
            .param("statuses", "ACTIVE")
        .when()
            .get("/loans")
        .then()
            .statusCode(200);
    }

    @Test
    @Order(3)
    @DisplayName("Should list all loans when no provide parameters")
    void shouldListAllLoansSuccessfully_whenNoProvideParameters() {
        TestDataFactory.createBook("498-15-66626-60-5");

        var customer = TestDataFactory.getCustomer("j.doe@test.com");
        var book = TestDataFactory.getBook("498-15-66626-60-5");

        String payload = """
                {
                    "customerId": "%s",
                    "ISBN": "%s"
                }
                """.formatted(customer.id(), book.ISBN());

        // Create new loan
        given()
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .post("/loans")
        .then()
            .statusCode(201);

        // List all loans
        given()
            .contentType(ContentType.JSON)
            .queryParam("page", 1)
            .queryParam("limit", 1)
        .when()
            .get("/loans")
        .then()
            .statusCode(200)
            .body("items.size()", is(1))
            .body("page", is(1))
            .body("limit", is(1))
            .body("totalPages", is(2))
            .body("total", is(2));
    }

    @Test
    @Order(4)
    @DisplayName("Should close a loan successfully when the status is overdue")
    void shouldCloseLoanSuccessfully_whenStatusIsOverdue() {
        var customer = TestDataFactory.getCustomer("j.doe@test.com");
        var book = TestDataFactory.getBook("408-36-74383-60-6");
        TestDataFactory.getPagedLoans(book.ISBN(), customer.id());

        String loanId = UUID.randomUUID().toString();
        TestDataFactory.createOverdueLoan(loanId, customer.id(), book.ISBN());

        given()
            .contentType(ContentType.JSON)
            .header("X-Trace-Id", "08004816-4be3-4d5f-96b8-119c78db1e52")
            .param("loanId", loanId)
        .when()
            .patch("/loans/" + loanId + "/close-overdue")
        .then()
            .statusCode(204);

    }

    @Test
    @Order(5)
    @DisplayName("Should throw an error when trying to create a loan when has no register for customer or book")
    void shouldThrowError_whenTryingCreateLoanWithoutCustomerOrBook() {
        TestDataFactory.createCustomer("hellen.doe@test.com");
        TestDataFactory.createBook("344-89-27240-40-1");

        var customer = TestDataFactory.getCustomer("hellen.doe@test.com");

        String firstPayload = """
                {
                    "customerId": "%s",
                    "ISBN": "invalid-isbn"
                }
                """.formatted(customer.id());

        given()
                .contentType(ContentType.JSON)
                .body(firstPayload)
                .when()
                .post("/loans")
                .then()
                .statusCode(404)
                .body("message", equalTo("Book not Found!"));

        String secondPayload = """
                {
                    "customerId": "invalid-customer-id",
                    "ISBN": "344-89-27240-40-1"
                }
                """;

        given()
            .contentType(ContentType.JSON)
            .body(secondPayload)
        .when()
            .post("/loans")
        .then()
            .statusCode(500); // TODO: No have customer validation yet
    }

    private static class TestDataFactory {

        static void createCustomer(String email) {
            String payload = """
                    {
                        "name": "John Doe",
                        "email": "%s"
                    }
                    """.formatted(email);

            given()
                .contentType(ContentType.JSON)
            .header("X-Trace-Id", "e84830f5-a167-4748-9a31-7c3fb0f99165")
                .body(payload)
            .when()
                .post("/customers")
            .then()
                .statusCode(201);
        }

        static CustomerDto getCustomer(String email) {
            return get("/customers/search?email=" + email).as(CustomerDto.class);
        }

        static void createBook(String isbn) {
            String payload = """
                    {
                        "ISBN": "%s",
                        "title": "Clean Architecture Modern Java",
                        "author": "John Doe",
                        "year": 2026,
                        "status": "AVAILABLE"
                    }
                    """.formatted(isbn);

            given()
                .contentType(ContentType.JSON)
                .header("X-Trace-Id", "7278e119-b087-4528-87bd-a5e8809b9c49")
                .body(payload)
            .when()
                .post("/books")
            .then()
                .statusCode(201);
        }

        static BookDto getBook(String isbn) {
            return get("books/{isbn}", isbn).as(BookDto.class);
        }

        static OutputPaginationDto<LoanDto> getPagedLoans(String... parameterValues) {
            String[] parameterNames = {"ISBN", "customerId", "statuses"};

            var request = given()
                .contentType(ContentType.JSON);

            int limit = Math.min(parameterValues.length, parameterNames.length);

            for (var i = 0; i < limit; i++) {
                request.param(parameterNames[i], parameterValues[i]);
            }

            return request.when()
                .get("/loans")
            .then()
                .statusCode(200)
                .extract()
                .body().as(new TypeRef<>() {});
        }

        static void createOverdueLoan(String loanId, String customerId, String isbn) {
            String sql = """
                INSERT INTO tb_loan (id, customer_id, isbn, status, start_date, end_date) 
                VALUES (?, ?, ?, 'OVERDUE', NOW() - INTERVAL '20 days', NOW() - INTERVAL '5 days')
                """;

            String jdbcUrl = postgres.getJdbcUrl();
            String dbUser = postgres.getUsername();
            String dbPassword = postgres.getPassword();

            try (var conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
                 var stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, loanId);
                stmt.setString(2, customerId);
                stmt.setString(3, isbn);
                stmt.executeUpdate();

            } catch (java.sql.SQLException e) {
                throw new RuntimeException("Test execution collapse seeding overdue loan simulation record into container baseline", e);
            }
        }
    }
}
