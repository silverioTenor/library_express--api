package org.libraryexpress.application.user.dto.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.libraryexpress.application.UnitTest;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;

@UnitTest
@DisplayName("CreateSuperAdminDTO")
class CreateSuperAdminDTOTest {

    @Test
    @DisplayName("Should create a super admin request when all fields are valid")
    void shouldCreateRequest_whenAllFieldsAreValid() {
        CreateSuperAdminDTO request = new CreateSuperAdminDTO("Library Admin", "admin@example.com", "valid-password");

        assertEquals("Library Admin", request.name());
        assertEquals("admin@example.com", request.email());
        assertEquals("valid-password", request.password());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("Should reject a request when the name is blank")
    void shouldThrowException_whenNameIsBlank(String invalidName) {
        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> new CreateSuperAdminDTO(invalidName, "admin@example.com", "valid-password"));

        assertTrue(exception.getMessage().contains("Name is required."));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("Should reject a request when the email is blank")
    void shouldThrowException_whenEmailIsBlank(String invalidEmail) {
        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> new CreateSuperAdminDTO("Library Admin", invalidEmail, "valid-password"));

        assertTrue(exception.getMessage().contains("Email is required."));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("Should reject a request when the password is blank")
    void shouldThrowException_whenPasswordIsBlank(String invalidPassword) {
        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> new CreateSuperAdminDTO("Library Admin", "admin@example.com", invalidPassword));

        assertTrue(exception.getMessage().contains("Password is required."));
    }

    @Test
    @DisplayName("Should accumulate all validation errors when every required field is missing")
    void shouldAccumulateErrors_whenEveryRequiredFieldIsMissing() {
        InvalidArgumentException exception =
                assertThrows(InvalidArgumentException.class, () -> new CreateSuperAdminDTO(null, null, null));

        assertEquals("[Name is required., Email is required., Password is required.]", exception.getMessage());
    }

    @Test
    @DisplayName("Should hide the password when the request is converted to a string")
    void shouldHidePassword_whenConvertedToString() {
        CreateSuperAdminDTO request =
                new CreateSuperAdminDTO("Library Admin", "admin@example.com", "top-secret-password");

        String representation = request.toString();

        assertFalse(representation.contains("top-secret-password"));
    }
}
