package org.libraryexpress.application.loan.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.libraryexpress.application.UnitTest;
import org.libraryexpress.application.loan.dto.request.FilterLoansDto;
import org.libraryexpress.domain.loan.enums.LoanStatus;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@DisplayName("SearchLoanValidator - Unit Test")
class SearchLoanValidatorTest {

    private final SearchLoanValidator validator = new SearchLoanValidator();

    @Test
    @DisplayName("Should validate successfully when at least one search criteria is provided")
    void shouldValidateSuccessfully_whenAtLeastOneCriteriaIsPresent() {
        // Arrange
        FilterLoansDto filterWithStatus = new FilterLoansDto(null, null, Set.of(LoanStatus.ACTIVE), null);
        FilterLoansDto filterWithCustomer = new FilterLoansDto("customer-123", null, null, null);
        FilterLoansDto filterWithIsbn = new FilterLoansDto(null, "978-43-12345-67-8", null, null);

        // Act & Assert - Verifying each condition independently to satisfy the OR (||) logic
        assertDoesNotThrow(() -> validator.validate(filterWithStatus));
        assertDoesNotThrow(() -> validator.validate(filterWithCustomer));
        assertDoesNotThrow(() -> validator.validate(filterWithIsbn));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidFilters")
    @DisplayName("Should throw IllegalArgumentException when filter has no valid search criteria")
    void shouldThrowIllegalArgumentException_whenFilterIsEmptyOrBlank(FilterLoansDto invalidFilter) {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                validator.validate(invalidFilter)
        );

        assertEquals("At least one search criteria must be provided", exception.getMessage());
    }

    // Advanced Java: Method source factory supplying diverse edge cases for branch coverage
    private static Stream<Arguments> provideInvalidFilters() {
        return Stream.of(
                Arguments.of(new FilterLoansDto("", null, null, null)),
                Arguments.of(new FilterLoansDto(null, "   ", null, null)),
                Arguments.of(new FilterLoansDto("   ", null, null, null))
        );
    }
}
