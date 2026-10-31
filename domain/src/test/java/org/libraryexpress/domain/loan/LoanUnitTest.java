package org.libraryexpress.domain.loan;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.domain.UnitTest;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;
import org.libraryexpress.domain.loan.entity.Loan;
import org.libraryexpress.domain.loan.enums.LoanStatus;
import org.libraryexpress.domain.loan.exception.DateOutOfBoundException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@DisplayName("Loan Entity - Unit Test")
public class LoanUnitTest {

    private static final String VALID_ISBN = "978-43-12345-67-8";
    private static final String CUSTOMER_ID = "customer-uuid-123";

    @Test
    @DisplayName("Should reject a null endDate because the entity requires a non-null due date")
    void shouldThrowException_whenEndDateIsNull() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);

        assertThrows(NullPointerException.class, () ->
                new Loan.Builder()
                        .setId("loan-1")
                        .setISBN(VALID_ISBN)
                        .setCustomerId(CUSTOMER_ID)
                        .setStatus(LoanStatus.ACTIVE)
                        .setStartDate(startDate)
                        .build()
        );
    }

    @Test
    @DisplayName("Should use provided endDate when it is explicitly given to the builder")
    void shouldUseCaseProvidedEndDate_whenItIsGiven() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate customEndDate = LocalDate.of(2026, 8, 5);

        Loan loan = new Loan.Builder()
                .setId("loan-1")
                .setISBN(VALID_ISBN)
                .setCustomerId(CUSTOMER_ID)
                .setStatus(LoanStatus.ACTIVE)
                .setStartDate(startDate)
                .setEndDate(customEndDate)
                .build();

        assertEquals(customEndDate, loan.getEndDate());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when mandatory fields are missing, empty, or blank")
    void shouldThrowException_whenMandatoryFieldsAreInvalid() {
        LocalDate now = LocalDate.of(2026, 8, 12);

        assertThrows(IllegalArgumentException.class, () ->
                        new Loan.Builder()
                                .setId(null)
                                .setISBN(VALID_ISBN)
                                .setCustomerId(CUSTOMER_ID)
                                .setStatus(LoanStatus.ACTIVE)
                                .setStartDate(now)
                                .setEndDate(now.plusDays(15))
                                .build(),
                "Should throw exception for null Loan ID"
        );
        assertThrows(IllegalArgumentException.class, () ->
                        new Loan.Builder()
                                .setId("").setISBN(VALID_ISBN)
                                .setCustomerId(CUSTOMER_ID)
                                .setStatus(LoanStatus.ACTIVE)
                                .setStartDate(now)
                                .setEndDate(now.plusDays(15))
                                .build(),
                "Should throw exception for empty Loan ID"
        );
        assertThrows(IllegalArgumentException.class, () ->
                        new Loan.Builder()
                                .setId("   ")
                                .setISBN(VALID_ISBN)
                                .setCustomerId(CUSTOMER_ID)
                                .setStatus(LoanStatus.ACTIVE)
                                .setStartDate(now)
                                .setEndDate(now.plusDays(15))
                                .build(),
                "Should throw exception for blank Loan ID"
        );

        assertThrows(IllegalArgumentException.class, () ->
                        new Loan.Builder()
                                .setId("loan-1")
                                .setISBN(VALID_ISBN)
                                .setCustomerId(null)
                                .setStatus(LoanStatus.ACTIVE)
                                .setStartDate(now)
                                .setEndDate(now.plusDays(15))
                                .build(),
                "Should throw exception for null Customer ID"
        );
        assertThrows(IllegalArgumentException.class, () ->
                        new Loan.Builder()
                                .setId("loan-1")
                                .setISBN(VALID_ISBN)
                                .setCustomerId("")
                                .setStatus(LoanStatus.ACTIVE)
                                .setStartDate(now)
                                .setEndDate(now.plusDays(15))
                                .build(),
                "Should throw exception for empty Customer ID"
        );
        assertThrows(IllegalArgumentException.class, () ->
                        new Loan.Builder()
                                .setId("loan-1")
                                .setISBN(VALID_ISBN)
                                .setCustomerId("   ")
                                .setStatus(LoanStatus.ACTIVE)
                                .setStartDate(now)
                                .setEndDate(now.plusDays(15))
                                .build(),
                "Should throw exception for blank Customer ID"
        );

        assertThrows(NullPointerException.class, () ->
                        new Loan.Builder()
                                .setId("loan-1")
                                .setISBN(VALID_ISBN)
                                .setCustomerId(CUSTOMER_ID)
                                .setStatus(null)
                                .setStartDate(now)
                                .setEndDate(now.plusDays(15))
                                .build(),
                "Should throw exception for null Loan Status"
        );

        assertThrows(InvalidArgumentException.class, () ->
                        new Loan.Builder()
                                .setId("loan-1")
                                .setISBN(null)
                                .setCustomerId(CUSTOMER_ID)
                                .setStatus(LoanStatus.ACTIVE)
                                .setStartDate(now)
                                .setEndDate(now.plusDays(15))
                                .build(),
                "Should throw exception for null ISBN"
        );

        assertThrows(NullPointerException.class, () ->
                        new Loan.Builder()
                                .setId("loan-1")
                                .setISBN(VALID_ISBN)
                                .setCustomerId(CUSTOMER_ID)
                                .setStatus(LoanStatus.ACTIVE)
                                .setStartDate(null)
                                .setEndDate(now.plusDays(15))
                                .build(),
                "Should throw exception for null start date"
        );
    }


    @Test
    @DisplayName("Should return false for isOverdue when current date is within the due date")
    void shouldReturnFalseForIsOverdue_whenCurrentDateIsWithinLimit() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        Loan loan = new Loan.Builder()
                .setId("loan-1")
                .setISBN(VALID_ISBN)
                .setCustomerId(CUSTOMER_ID)
                .setStatus(LoanStatus.ACTIVE)
                .setStartDate(startDate)
                .setEndDate(startDate.plusDays(15))
                .build();

        Instant fixedInstant = Instant.parse("2026-08-10T12:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, ZoneId.of("UTC"));

        assertFalse(loan.isOverdue(fixedClock), "Loan should not be overdue on day 10");
    }

    @Test
    @DisplayName("Should return false for isOverdue exactly on the due date")
    void shouldReturnFalseForIsOverdue_whenCurrentDateIsExactlyOnTheLimitDay() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        Loan loan = new Loan.Builder()
                .setId("loan-1")
                .setISBN(VALID_ISBN)
                .setCustomerId(CUSTOMER_ID)
                .setStatus(LoanStatus.ACTIVE)
                .setStartDate(startDate)
                .setEndDate(startDate.plusDays(15))
                .build();

        Instant fixedInstant = Instant.parse("2026-08-16T12:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, ZoneId.of("UTC"));

        assertFalse(loan.isOverdue(fixedClock), "Loan should not be overdue exactly on the due date");
    }

    @Test
    @DisplayName("Should return true for isOverdue when current date is past the due date")
    void shouldReturnTrueForIsOverdue_whenCurrentDateIsPastLimit() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        Loan loan = new Loan.Builder()
                .setId("loan-1")
                .setISBN(VALID_ISBN)
                .setCustomerId(CUSTOMER_ID)
                .setStatus(LoanStatus.ACTIVE)
                .setStartDate(startDate)
                .setEndDate(startDate.plusDays(15))
                .build();

        Instant fixedInstant = Instant.parse("2026-08-17T12:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, ZoneId.of("UTC"));

        assertTrue(loan.isOverdue(fixedClock), "Loan should be overdue on day 16");
    }

    @Test
    @DisplayName("Should sort loans chronologically by their start date")
    void shouldSortLoansByStartDate_whenCompared() {
        Loan earlyLoan = new Loan.Builder()
                .setId("1")
                .setISBN(VALID_ISBN)
                .setCustomerId(CUSTOMER_ID)
                .setStatus(LoanStatus.ACTIVE)
                .setStartDate(LocalDate.of(2026, 8, 1))
                .setEndDate(LocalDate.of(2026, 8, 16))
                .build();

        Loan lateLoan = new Loan.Builder()
                .setId("2")
                .setISBN(VALID_ISBN)
                .setCustomerId(CUSTOMER_ID)
                .setStatus(LoanStatus.ACTIVE)
                .setStartDate(LocalDate.of(2026, 8, 15))
                .setEndDate(LocalDate.of(2026, 8, 30))
                .build();

        assertTrue(earlyLoan.compareTo(lateLoan) < 0, "Early loan should come before late loan");
        assertTrue(lateLoan.compareTo(earlyLoan) > 0, "Late loan should come after early loan");
        assertEquals(0, earlyLoan.compareTo(earlyLoan), "Comparing a loan to itself must return zero");
    }

    @Test
    @DisplayName("Should throw DateOutOfBoundException when provided endDate is before startDate")
    void shouldThrowException_whenEndDateIsBeforeStartDate() {
        LocalDate startDate = LocalDate.of(2026, 8, 10);
        LocalDate invalidBeforeDate = LocalDate.of(2026, 8, 9);

        DateOutOfBoundException exception = assertThrows(DateOutOfBoundException.class, () ->
                new Loan.Builder()
                        .setId("loan-1")
                        .setISBN(VALID_ISBN)
                        .setCustomerId(CUSTOMER_ID)
                        .setStatus(LoanStatus.ACTIVE)
                        .setStartDate(startDate)
                        .setEndDate(invalidBeforeDate)
                        .build()
        );

        assertEquals("End date cannot be before start date", exception.getMessage());
    }

    @Test
    @DisplayName("Should reconstitute a loan whose period has already elapsed without applying creation policy")
    void shouldReconstituteLoan_whenPeriodHasAlreadyElapsed() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 16);

        Loan loan = new Loan.Builder()
                .setId("loan-1")
                .setISBN(VALID_ISBN)
                .setCustomerId(CUSTOMER_ID)
                .setStatus(LoanStatus.OVERDUE)
                .setStartDate(startDate)
                .setEndDate(endDate)
                .build();

        Clock afterDueDate = Clock.fixed(Instant.parse("2026-08-20T12:00:00Z"), ZoneId.of("UTC"));

        assertEquals(endDate, loan.getEndDate());
        assertTrue(loan.isOverdue(afterDueDate));
    }
}
