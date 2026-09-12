package org.libraryexpress.domain.loan;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.domain.UnitTest;
import org.libraryexpress.domain.book.exception.InvalidIsbnException;
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
    @DisplayName("Should automatically calculate dueDate when endDate is not provided")
    void shouldCalculateDueDate_whenEndDateIsNull() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 8, 1);

        // Act
        Loan loan = new Loan.Builder()
                .setId("loan-1")
                .setISBN(VALID_ISBN)
                .setCustomerId(CUSTOMER_ID)
                .setStatus(LoanStatus.ACTIVE)
                .setStartDate(startDate)
                .build();

        // Assert
        LocalDate expectedEndDate = startDate.plusDays(15);
        assertEquals(expectedEndDate, loan.getEndDate(), "End date should be automatically set to startDate + 15 days");
    }

    @Test
    @DisplayName("Should use provided endDate when it is explicitly given to the builder")
    void shouldUseCaseProvidedEndDate_whenItIsGiven() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate customEndDate = LocalDate.of(2026, 8, 5);

        // Act
        Loan loan = new Loan.Builder()
                .setId("loan-1")
                .setISBN(VALID_ISBN)
                .setCustomerId(CUSTOMER_ID)
                .setStatus(LoanStatus.ACTIVE)
                .setStartDate(startDate)
                .setEndDate(customEndDate)
                .build();

        // Assert
        assertEquals(customEndDate, loan.getEndDate());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when mandatory fields are missing, empty, or blank")
    void shouldThrowException_whenMandatoryFieldsAreInvalid() {
        LocalDate now = LocalDate.of(2026, 8, 12);

        // 1. Validating Loan ID (Null, Empty, and Blank)
        assertThrows(IllegalArgumentException.class, () ->
                        new Loan.Builder()
                                .setId(null)
                                .setISBN(VALID_ISBN)
                                .setCustomerId(CUSTOMER_ID)
                                .setStatus(LoanStatus.ACTIVE)
                                .setStartDate(now)
                                .build(),
                "Should throw exception for null Loan ID"
        );
        assertThrows(IllegalArgumentException.class, () ->
                        new Loan.Builder()
                                .setId("").setISBN(VALID_ISBN)
                                .setCustomerId(CUSTOMER_ID)
                                .setStatus(LoanStatus.ACTIVE)
                                .setStartDate(now)
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
                                .build(),
                "Should throw exception for blank Loan ID"
        );

        // 2. Validating Customer ID (Null, Empty, and Blank)
        assertThrows(IllegalArgumentException.class, () ->
                        new Loan.Builder()
                                .setId("loan-1")
                                .setISBN(VALID_ISBN)
                                .setCustomerId(null)
                                .setStatus(LoanStatus.ACTIVE)
                                .setStartDate(now)
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
                                .build(),
                "Should throw exception for blank Customer ID"
        );

        // 3. Validating Loan Status (Null)
        assertThrows(NullPointerException.class, () ->
                        new Loan.Builder()
                                .setId("loan-1")
                                .setISBN(VALID_ISBN)
                                .setCustomerId(CUSTOMER_ID)
                                .setStatus(null)
                                .setStartDate(now)
                                .build(),
                "Should throw exception for null Loan Status"
        );

        // 4. Validating Book ISBN (Null)
        assertThrows(InvalidIsbnException.class, () ->
                        new Loan.Builder()
                                .setId("loan-1")
                                .setISBN(null)
                                .setCustomerId(CUSTOMER_ID)
                                .setStatus(LoanStatus.ACTIVE)
                                .setStartDate(now)
                                .build(),
                "Should throw exception for null ISBN"
        );

        // 5. Validating startDate (Null)
        assertThrows(NullPointerException.class, () ->
                        new Loan.Builder()
                                .setId("loan-1")
                                .setISBN(VALID_ISBN)
                                .setCustomerId(CUSTOMER_ID)
                                .setStatus(LoanStatus.ACTIVE)
                                .setStartDate(null)
                                .build(),
                "Should throw exception for null ISBN"
        );
    }


    @Test
    @DisplayName("Should return false for isOverdue when current date is within the 15 days limit")
    void shouldReturnFalseForIsOverdue_whenCurrentDateIsWithinLimit() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        Loan loan = new Loan.Builder()
                .setId("loan-1")
                .setISBN(VALID_ISBN)
                .setCustomerId(CUSTOMER_ID)
                .setStatus(LoanStatus.ACTIVE)
                .setStartDate(startDate)
                .build();

        // Aprofundando em Java: Simulando que "hoje" é dia 10 de Agosto de 2026 (9 dias após o início)
        Instant fixedInstant = Instant.parse("2026-08-10T12:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, ZoneId.of("UTC"));

        // Act & Assert
        assertFalse(loan.isOverdue(fixedClock), "Loan should not be overdue on day 10");
    }

    @Test
    @DisplayName("Should return false for isOverdue exactly on the 15th day limit")
    void shouldReturnFalseForIsOverdue_whenCurrentDateIsExactlyOnTheLimitDay() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        Loan loan = new Loan.Builder()
                .setId("loan-1")
                .setISBN(VALID_ISBN)
                .setCustomerId(CUSTOMER_ID)
                .setStatus(LoanStatus.ACTIVE)
                .setStartDate(startDate)
                .build();

        // Simulando o 15º dia (16 de Agosto de 2026) -> 16 menos 1 é igual a 15 dias de diferença
        Instant fixedInstant = Instant.parse("2026-08-16T12:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, ZoneId.of("UTC"));

        // Act & Assert
        assertFalse(loan.isOverdue(fixedClock), "Loan should not be overdue exactly on the 15th day");
    }

    @Test
    @DisplayName("Should return true for isOverdue when current date is past the 15 days limit")
    void shouldReturnTrueForIsOverdue_whenCurrentDateIsPastLimit() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        Loan loan = new Loan.Builder()
                .setId("loan-1")
                .setISBN(VALID_ISBN)
                .setCustomerId(CUSTOMER_ID)
                .setStatus(LoanStatus.ACTIVE)
                .setStartDate(startDate)
                .build();

        // Simulando o 16º dia (17 de Agosto de 2026) -> 16 dias de diferença (passou de 15)
        Instant fixedInstant = Instant.parse("2026-08-17T12:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, ZoneId.of("UTC"));

        // Act & Assert
        assertTrue(loan.isOverdue(fixedClock), "Loan should be overdue on day 16");
    }

    @Test
    @DisplayName("Should sort loans chronologically by their start date")
    void shouldSortLoansByStartDate_whenCompared() {
        // Arrange
        Loan earlyLoan = new Loan.Builder()
                .setId("1")
                .setISBN(VALID_ISBN)
                .setCustomerId(CUSTOMER_ID)
                .setStatus(LoanStatus.ACTIVE)
                .setStartDate(LocalDate.of(2026, 8, 1))
                .build();

        Loan lateLoan = new Loan.Builder()
                .setId("2")
                .setISBN(VALID_ISBN)
                .setCustomerId(CUSTOMER_ID)
                .setStatus(LoanStatus.ACTIVE)
                .setStartDate(LocalDate.of(2026, 8, 15))
                .build();

        // Act & Assert
        assertTrue(earlyLoan.compareTo(lateLoan) < 0, "Early loan should come before late loan");
        assertTrue(lateLoan.compareTo(earlyLoan) > 0, "Late loan should come after early loan");
        assertEquals(0, earlyLoan.compareTo(earlyLoan), "Comparing a loan to itself must return zero");
    }

    @Test
    @DisplayName("Should throw DateOutOfBoundException when provided endDate is before startDate")
    void shouldThrowException_whenEndDateIsBeforeStartDate() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 8, 10);
        LocalDate invalidBeforeDate = LocalDate.of(2026, 8, 9); // 1 day before

        // Act & Assert
        DateOutOfBoundException exception = assertThrows(DateOutOfBoundException.class, () ->
                new Loan.Builder()
                        .setId("loan-1")
                        .setISBN(VALID_ISBN)
                        .setCustomerId(CUSTOMER_ID)
                        .setStatus(LoanStatus.ACTIVE)
                        .setStartDate(startDate)
                        .setEndDate(invalidBeforeDate) // invalid attempt
                        .build()
        );

        assertEquals("End date cannot be before start date", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw DateOutOfBoundException when provided endDate exceeds the 15-day limit")
    void shouldThrowException_whenEndDateExceeds15DaysLimit() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate invalidFutureDate = LocalDate.of(2026, 8, 17); // 16 days after (limit é 16/08)

        // Act & Assert
        DateOutOfBoundException exception = assertThrows(DateOutOfBoundException.class, () ->
                new Loan.Builder()
                        .setId("loan-1")
                        .setISBN(VALID_ISBN)
                        .setCustomerId(CUSTOMER_ID)
                        .setStatus(LoanStatus.ACTIVE)
                        .setStartDate(startDate)
                        .setEndDate(invalidFutureDate) // invalid attempt
                        .build()
        );

        assertEquals("End date cannot exceed the 15-day limit from start date", exception.getMessage());
    }

    @Test
    @DisplayName("Should build successfully when provided endDate is exactly on the 15-day limit")
    void shouldBuildSuccessfully_whenEndDateIsExactlyOnTheLimit() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate exactLimitDate = LocalDate.of(2026, 8, 16); // Exactly 15 days later

        // Act
        Loan loan = new Loan.Builder()
                .setId("loan-1")
                .setISBN(VALID_ISBN)
                .setCustomerId(CUSTOMER_ID)
                .setStatus(LoanStatus.ACTIVE)
                .setStartDate(startDate)
                .setEndDate(exactLimitDate)
                .build();

        // Assert
        assertEquals(exactLimitDate, loan.getEndDate(), "Should allow end date exactly at the limit");
    }

}
