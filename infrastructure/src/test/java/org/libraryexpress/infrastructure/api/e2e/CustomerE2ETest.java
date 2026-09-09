package org.libraryexpress.infrastructure.api.e2e;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.libraryexpress.application.customer.dto.response.CustomerDto;
import org.libraryexpress.infrastructure.E2ETest;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@E2ETest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Customer Aggregate Endpoints - E2E Test")
class CustomerE2ETest extends E2EBaseConfig {

    @Test
    @Order(1)
    @DisplayName("Should create successfully a new customer when provided a valid data")
    void shouldCreateCustomer_whenProvidedValidData() {
        String customerPayload = """
                {
                    "name": "John Doe",
                    "email": "j.doe@test.com"
                }
                """;

        given()
            .contentType(ContentType.JSON)
            .header("X-Trace-Id", "e84830f5-a167-4748-9a31-7c3fb0f99165")
            .body(customerPayload)
        .when()
            .post("/customers")
        .then()
            .statusCode(201);
    }

    @Test
    @Order(2)
    @DisplayName("Should find a registered customer when provided a valid email")
    void shouldFindCustomer_whenProvidedValidEmail() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("email", "j.doe@test.com")
        .when()
            .get("/customers/search")
        .then()
            .statusCode(200)
            .body("id", is(notNullValue()))
            .body("name", is("John Doe"))
            .body("email", is("j.doe@test.com"));
    }

    @Test
    @Order(3)
    @DisplayName("Should update email customer when provided a non-used email")
    void shouldUpdateCustomerEmail_whenProvidedANonUsedEmail() {
        // Get an exist customer
        CustomerDto customerDto = given()
                .contentType(ContentType.JSON)
                .queryParam("email", "j.doe@test.com")
                .when()
                .get("/customers/search").as(CustomerDto.class);

        String payload = """
                    {
                        "email": "another@test.com"
                    }
                """;

        String path = "/customers/" + customerDto.id() +"/update-email";

        // Update email
        given()
        .contentType(ContentType.JSON)
            .param("id", customerDto.id())
            .body(payload)
        .when()
            .patch(path)
        .then()
            .statusCode(204);

        // Get customer with email updated
        given()
            .contentType(ContentType.JSON)
            .queryParam("email", "another@test.com")
        .when()
            .get("/customers/search")
        .then()
            .statusCode(200)
            .body("id", is(customerDto.id()))
            .body("name", is(customerDto.name()));
    }

    @Test
    @Order(4)
    @DisplayName("Should list customers when previous registered")
    void shouldListCustomers_whenPreviousRegistered() {
        // No provided page and limit
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/customers")
        .then()
            .statusCode(200)
            .body("items", hasSize(1))
            .body("page", is(0))
            .body("limit", is(1))
            .body("totalPages", is(1))
            .body("total", is(1));

        // Provided page and limit
        given()
            .contentType(ContentType.JSON)
                .queryParam("page", 1)
                .queryParam("limit", 5)
        .when()
            .get("/customers")
        .then()
            .statusCode(200)
            .body("items", hasSize(1))
            .body("page", is(1))
            .body("limit", is(5))
            .body("totalPages", is(0))
            .body("total", is(1));
    }

    @Test
    @Order(5)
    @DisplayName("Should throw an error when create a customer with email previously registered")
    void shouldThrowError_whenCreateCustomerWithEmailPreviouslyRegistered() {
        String customerPayload = """
                {
                    "name": "User Test",
                    "email": "another@test.com"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .header("X-Trace-Id", "c0c20fcc-6ad1-4ec2-a48d-1d960933e266")
                .body(customerPayload)
                .when()
                .post("/customers")
                .then()
                .statusCode(409)
                .body("message", is("E-mail must be unique."));
    }

    @Test
    @Order(6)
    @DisplayName("Should throw an error when provided an invalid payload")
    void shouldThrowError_whenProvidedAnInvalidPayload() {
        String customerPayload = """
                {
                    "name": "User Test",
                    "email": "another@test.com"
                """;

        given()
            .contentType(ContentType.JSON)
            .header("X-Trace-Id", "16ca5c23-f9dc-4f0a-832c-4ebc54dabb5d")
            .body(customerPayload)
        .when()
            .post("/customers")
        .then()
            .statusCode(500)
            .body("message", is(notNullValue()));
    }

    @Test
    @Order(7)
    @DisplayName("Should throw an error when has no parameters provided")
    void shouldThrowError_whenHasNoParametersProvided() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/customers/search")
                .then()
                .statusCode(400)
                .body("message", is("Provide at least one parameter"));
    }

    @Test
    @Order(8)
    @DisplayName("Should throw an error when provided an invalid parameter")
    void shouldThrowError_whenProvidedAnInvalidParameter() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("id", "invalid-uuid")
        .when()
            .get("/customers/search")
        .then()
            .statusCode(404)
            .body("message", is("Customer not found!"));
    }

    @Test
    @Order(9)
    @DisplayName("Should throw an error when trying update customer with already used email")
    void shouldThrowError_whenTryingUpdateCustomerWithAlreadyUsedEmail() {
        String customerPayload = """
                {
                    "name": "Customer Test",
                    "email": "test.customer@test.com"
                }
                """;

        given()
            .contentType(ContentType.JSON)
            .header("X-Trace-Id", "e84830f5-a167-4748-9a31-7c3fb0f99165")
            .body(customerPayload)
        .when()
            .post("/customers")
        .then()
            .statusCode(201);

        CustomerDto customerDto = given()
            .contentType(ContentType.JSON)
            .queryParam("email", "another@test.com")
        .when()
            .get("/customers/search").as(CustomerDto.class);

        String payload = """
                    {
                        "email": "test.customer@test.com"
                    }
                """;

        String path = "/customers/" + customerDto.id() +"/update-email";

        given()
            .contentType(ContentType.JSON)
            .param("id", customerDto.id())
            .body(payload)
        .when()
            .patch(path)
        .then()
            .statusCode(409)
            .body("message", is("The email address is not permitted"));
    }

    @Test
    @Order(9)
    @DisplayName("Should throw an error when trying update customer with invalid ID")
    void shouldThrowError_whenTryingUpdateCustomerWithInvalidId() {
        String payload = """
                    {
                        "email": "test.customer@test.com"
                    }
                """;

        String path = "/customers/invalid-uuid/update-email";

        given()
                .contentType(ContentType.JSON)
                .param("id", "invalid-uuid")
                .body(payload)
                .when()
                .patch(path)
                .then()
                .statusCode(404)
                .body("message", is("Customer not found!"));
    }
}
