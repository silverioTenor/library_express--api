package org.libraryexpress.application.customer.dto.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.libraryexpress.application.UnitTest;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@UnitTest
@DisplayName("CreateCustomerDTOTest - Unit Test")
class CreateCustomerDTOTest {

    @Test
    @DisplayName("Should create instance when data is valid")
    void shouldCreateInstance_whenDataIsValid() {
        // Arrange & Act
        CreateCustomerDTO dto = new CreateCustomerDTO("John Doe", "john.doe@email.com");

        // Assert
        assertNotNull(dto);
        assertAll("Verifying record properties",
                () -> assertEquals("John Doe", dto.name()),
                () -> assertEquals("john.doe@email.com", dto.email())
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("Should throw exception with name error when name is invalid and email is valid")
    void shouldThrowExceptionWithNameError_whenNameIsInvalidAndEmailIsValid(String invalidName) {
        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class, () ->
                new CreateCustomerDTO(invalidName, "john.doe@email.com")
        );

        assertEquals("[The 'name' field cannot be null or empty]", exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("Should throw exception with email error when email is invalid and name is valid")
    void shouldThrowExceptionWithEmailError_whenEmailIsInvalidAndNameIsValid(String invalidEmail) {
        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class, () ->
                new CreateCustomerDTO("John Doe", invalidEmail)
        );

        assertEquals("[The 'email' field cannot be null or empty]", exception.getMessage());
    }

    @Test
    @DisplayName("Should accumulate and throw both errors when name and email are invalid")
    void shouldAccumulateAndThrowBothErrors_whenNameAndEmailAreInvalid() {
        String expectedMessage = "[The 'name' field cannot be null or empty, The 'email' field cannot be null or empty]";

        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class, () ->
                new CreateCustomerDTO(null, " ")
        );

        assertEquals(expectedMessage, exception.getMessage());
    }
}

