package org.libraryexpress.domain.loan;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.domain.UnitTest;
import org.libraryexpress.domain.book.valueobject.Isbn;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;
import org.libraryexpress.domain.loan.entity.Loan;
import org.libraryexpress.domain.loan.enums.LoanStatus;
import org.libraryexpress.domain.loan.exception.DateOutOfBoundException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

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
                Loan.restore(
                        "loan-1",
                        VALID_ISBN,
                        CUSTOMER_ID,
                        LoanStatus.ACTIVE,
                        startDate,
                        null,
                        null
                )
        );
    }

    @Test
    @DisplayName("Should use provided endDate when it is explicitly given to the factory")
    void shouldUseCaseProvidedEndDate_whenItIsGiven() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate customEndDate = LocalDate.of(2026, 8, 5);

        Loan loan = Loan.restore(
                "loan-1",
                VALID_ISBN,
                CUSTOMER_ID,
                LoanStatus.ACTIVE,
                startDate,
                customEndDate,
                null
        );

        assertEquals(customEndDate, loan.getEndDate());
    }

    @Test
    @DisplayName("Should throw InvalidArgumentException when mandatory fields are missing, empty, or blank")
    void shouldThrowException_whenMandatoryFieldsAreInvalid() {
        LocalDate now = LocalDate.of(2026, 8, 12);

        assertThrows(InvalidArgumentException.class, () ->
                        Loan.restore(
                                null,
                                VALID_ISBN,
                                CUSTOMER_ID,
                                LoanStatus.ACTIVE,
                                now,
                                now.plusDays(15),
                                null
                        ),
                "Should throw exception for null Loan ID"
        );
        assertThrows(InvalidArgumentException.class, () ->
                        Loan.restore(
                                "",
                                VALID_ISBN,
                                CUSTOMER_ID,
                                LoanStatus.ACTIVE,
                                now,
                                now.plusDays(15),
                                null
                        ),
                "Should throw exception for empty Loan ID"
        );
        assertThrows(InvalidArgumentException.class, () ->
                        Loan.restore(
                                "   ",
                                VALID_ISBN,
                                CUSTOMER_ID,
                                LoanStatus.ACTIVE,
                                now,
                                now.plusDays(15),
                                null
                        ),
                "Should throw exception for blank Loan ID"
        );

        assertThrows(InvalidArgumentException.class, () ->
                        Loan.restore(
                                "loan-1",
                                VALID_ISBN,
                                null,
                                LoanStatus.ACTIVE,
                                now,
                                now.plusDays(15),
                                null
                        ),
                "Should throw exception for null Customer ID"
        );
        assertThrows(InvalidArgumentException.class, () ->
                        Loan.restore(
                                "loan-1",
                                VALID_ISBN,
                                "",
                                LoanStatus.ACTIVE,
                                now,
                                now.plusDays(15),
                                null
                        ),
                "Should throw exception for empty Customer ID"
        );
        assertThrows(InvalidArgumentException.class, () ->
                        Loan.restore(
                                "loan-1",
                                VALID_ISBN,
                                "   ",
                                LoanStatus.ACTIVE,
                                now,
                                now.plusDays(15),
                                null
                        ),
                "Should throw exception for blank Customer ID"
        );

        assertThrows(NullPointerException.class, () ->
                        Loan.restore(
                                "loan-1",
                                VALID_ISBN,
                                CUSTOMER_ID,
                                null,
                                now,
                                now.plusDays(15),
                                null
                        ),
                "Should throw exception for null Loan Status"
        );

        assertThrows(InvalidArgumentException.class, () ->
                        Loan.restore(
                                "loan-1",
                                null,
                                CUSTOMER_ID,
                                LoanStatus.ACTIVE,
                                now,
                                now.plusDays(15),
                                null
                        ),
                "Should throw exception for null ISBN"
        );

        assertThrows(NullPointerException.class, () ->
                        Loan.restore(
                                "loan-1",
                                VALID_ISBN,
                                CUSTOMER_ID,
                                LoanStatus.ACTIVE,
                                null,
                                now.plusDays(15),
                                null
                        ),
                "Should throw exception for null start date"
        );
    }


    @Test
    @DisplayName("Should return false for isOverdue when current date is within the due date")
    void shouldReturnFalseForIsOverdue_whenCurrentDateIsWithinLimit() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        Loan loan = Loan.restore(
                "loan-1",
                VALID_ISBN,
                CUSTOMER_ID,
                LoanStatus.ACTIVE,
                startDate,
                startDate.plusDays(15),
                null
        );

        Instant fixedInstant = Instant.parse("2026-08-10T12:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, ZoneId.of("UTC"));

        assertFalse(loan.isOverdue(fixedClock), "Loan should not be overdue on day 10");
    }

    @Test
    @DisplayName("Should return false for isOverdue exactly on the due date")
    void shouldReturnFalseForIsOverdue_whenCurrentDateIsExactlyOnTheLimitDay() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        Loan loan = Loan.restore(
                "loan-1",
                VALID_ISBN,
                CUSTOMER_ID,
                LoanStatus.ACTIVE,
                startDate,
                startDate.plusDays(15),
                null
        );

        Instant fixedInstant = Instant.parse("2026-08-16T12:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, ZoneId.of("UTC"));

        assertFalse(loan.isOverdue(fixedClock), "Loan should not be overdue exactly on the due date");
    }

    @Test
    @DisplayName("Should return true for isOverdue when current date is past the due date")
    void shouldReturnTrueForIsOverdue_whenCurrentDateIsPastLimit() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        Loan loan = Loan.restore(
                "loan-1",
                VALID_ISBN,
                CUSTOMER_ID,
                LoanStatus.ACTIVE,
                startDate,
                startDate.plusDays(15),
                null
        );

        Instant fixedInstant = Instant.parse("2026-08-17T12:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, ZoneId.of("UTC"));

        assertTrue(loan.isOverdue(fixedClock), "Loan should be overdue on day 16");
    }

    @Test
    @DisplayName("Should sort loans chronologically by their start date")
    void shouldSortLoansByStartDate_whenCompared() {
        Loan earlyLoan = Loan.restore(
                "1",
                VALID_ISBN,
                CUSTOMER_ID,
                LoanStatus.ACTIVE,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 16),
                null
        );

        Loan lateLoan = Loan.restore(
                "2",
                VALID_ISBN,
                CUSTOMER_ID,
                LoanStatus.ACTIVE,
                LocalDate.of(2026, 8, 15),
                LocalDate.of(2026, 8, 30),
                null
        );

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
                Loan.restore(
                        "loan-1",
                        VALID_ISBN,
                        CUSTOMER_ID,
                        LoanStatus.ACTIVE,
                        startDate,
                        invalidBeforeDate,
                        null
                )
        );

        assertEquals("End date cannot be before start date", exception.getMessage());
    }

    @Test
    @DisplayName("Should reconstitute a loan whose period has already elapsed without applying creation policy")
    void shouldReconstituteLoan_whenPeriodHasAlreadyElapsed() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 16);

        Loan loan = Loan.restore(
                "loan-1",
                VALID_ISBN,
                CUSTOMER_ID,
                LoanStatus.OVERDUE,
                startDate,
                endDate,
                null
        );

        Clock afterDueDate = Clock.fixed(Instant.parse("2026-08-20T12:00:00Z"), ZoneId.of("UTC"));

        assertEquals(endDate, loan.getEndDate());
        assertTrue(loan.isOverdue(afterDueDate));
    }

    @Test
    @DisplayName("Should set closedAt to current timestamp when loan is returned on time")
    void shouldSetClosedAt_whenLoanIsReturnedOnTime() {
        LocalDate timestamp = LocalDate.now();

        Loan loan = Loan.restore(
                "sdasdasdasd",
                VALID_ISBN,
                CUSTOMER_ID,
                LoanStatus.ACTIVE,
                timestamp,
                timestamp.plusDays(15),
                null
        );

        Clock clock = Clock.fixed(Instant.now().plus(5, ChronoUnit.DAYS), ZoneId.of("UTC"));
        loan.processReturn(clock, false);

        assertFalse(loan.isOverdue(clock));
        assertEquals(timestamp, loan.getStartDate());
        assertEquals(timestamp.plusDays(15), loan.getEndDate());
        assertEquals(LoanStatus.FINISHED, loan.getStatus());
        assertNotNull(loan.getClosedAt());
    }

    @Test
    @DisplayName("Should set closedAt to current timestamp when loan is returned overdue")
    void shouldSetClosedAt_whenLoanIsReturnedOverdue() {
        LocalDate timestamp = LocalDate.now();
        LocalDate startDate = timestamp.minusDays(20);
        LocalDate endDate = startDate.plusDays(15);
        Instant instant = Instant.now().plus(20, ChronoUnit.DAYS);

        Loan loan = Loan.restore(
                "sdasdasdasd",
                VALID_ISBN,
                CUSTOMER_ID,
                LoanStatus.OVERDUE,
                startDate,
                endDate,
                null
        );

        Clock clock = Clock.fixed(instant, ZoneId.of("UTC"));
        loan.processReturn(clock, true);

        assertTrue(loan.isOverdue(clock));
        assertEquals(startDate, loan.getStartDate());
        assertEquals(endDate, loan.getEndDate());
        assertEquals(LoanStatus.FINISHED, loan.getStatus());
        assertNotNull(loan.getClosedAt());
    }

    @Test
    @DisplayName("Should not set closedAt to current timestamp when loan is returned overdue")
    void shouldNotSetClosedAt_whenLoanIsReturnedOverdue() {
        LocalDate timestamp = LocalDate.now();
        LocalDate startDate = timestamp.minusDays(20);
        LocalDate endDate = startDate.plusDays(15);
        Instant instant = Instant.now().plus(20, ChronoUnit.DAYS);

        Loan loan = Loan.restore(
                "sdasdasdasd",
                VALID_ISBN,
                CUSTOMER_ID,
                LoanStatus.OVERDUE,
                startDate,
                endDate,
                null
        );

        Clock clock = Clock.fixed(instant, ZoneId.of("UTC"));
        loan.processReturn(clock, false);

        assertTrue(loan.isOverdue(clock));
        assertEquals(startDate, loan.getStartDate());
        assertEquals(endDate, loan.getEndDate());
        assertEquals(LoanStatus.OVERDUE, loan.getStatus());
        assertNull(loan.getClosedAt());
    }
}
