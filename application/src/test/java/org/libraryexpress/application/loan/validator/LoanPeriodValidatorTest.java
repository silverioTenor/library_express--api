package org.libraryexpress.application.loan.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.application.UnitTest;
import org.libraryexpress.domain.loan.exception.DateOutOfBoundException;
import org.libraryexpress.domain.loan.validator.LoanPeriodValidator;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@UnitTest
@DisplayName("LoanPeriodValidator - Unit Test")
class LoanPeriodValidatorTest {

    private final LoanPeriodValidator validator = new LoanPeriodValidator();

    @Test
    @DisplayName("Should reject a due date beyond the 15-day creation limit")
    void shouldRejectDueDate_whenItExceeds15DayLimit() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate dueDate = startDate.plusDays(LoanPeriodValidator.MAX_LOAN_DAYS + 1);

        DateOutOfBoundException exception = assertThrows(
                DateOutOfBoundException.class,
                () -> validator.validate(startDate, dueDate)
        );

        assertEquals("End date cannot exceed the 15-day limit from start date", exception.getMessage());
    }

    @Test
    @DisplayName("Should accept a due date exactly on the 15-day creation limit")
    void shouldAcceptDueDate_whenItIsExactlyOnTheLimit() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate dueDate = startDate.plusDays(LoanPeriodValidator.MAX_LOAN_DAYS);

        assertDoesNotThrow(() -> validator.validate(startDate, dueDate));
    }

    @Test
    @DisplayName("Should accept a due date shorter than the 15-day creation limit")
    void shouldAcceptDueDate_whenItIsWithinTheLimit() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate dueDate = startDate.plusDays(5);

        assertDoesNotThrow(() -> validator.validate(startDate, dueDate));
    }
}
